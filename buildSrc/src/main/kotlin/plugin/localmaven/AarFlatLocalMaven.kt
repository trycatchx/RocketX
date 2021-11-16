package plugin.localmaven

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.RocketXPlugin
import plugin.utils.FileUtil
import plugin.utils.LogUtil
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/3
 * copyright TCL+
 *
 * 目前先用 flat 实现 localmaven 功能
 */
class AarFlatLocalMaven(
    var childProject: Project,
    var rocketXPlugin: RocketXPlugin,
    var appProject: Project,
    var mAllChangedProject: MutableMap<String, Project>? = null) : LocalMaven() {

    companion object {
        const val ASSEMBLE = "assemble"
    }

    val enableLocalMaven by lazy {
        rocketXPlugin.mRocketXBean?.localMaven ?: false
    }

    override fun uploadLocalMaven() {
        //先 hook bundleXXaar task 打出包
        val android = appProject.extensions.getByType(AppExtension::class.java)
        android.applicationVariants.forEach {
            getAppAssembleTask(ASSEMBLE + it.flavorName.capitalize() + it.buildType.name.capitalize())
                ?.let { task ->
                    hookBundleAarTask(task, it.buildType.name)
                }
        }

    }

    fun getAppAssembleTask(name: String): TaskProvider<Task>? {
        var taskProvider: TaskProvider<Task>? = null
        try {
            taskProvider = appProject.tasks.named(name)
        } catch (ignore: Exception) {
        }
        return taskProvider
    }


    fun hookBundleAarTask(task: TaskProvider<Task>, buildType: String) {
        //如果当前模块是改动模块，需要打 aar
        if (mAllChangedProject?.contains(childProject.path) == true) {
            //打包aar
            val bundleTask = getBundleTask(childProject, buildType.capitalize())?.apply {
                task.configure {
                    it.finalizedBy(this)
                }
            }

            if (enableLocalMaven) {
                // publish local maven
                bundleTask?.let { bTask ->
                    LogUtil.d("bTask=$bTask")
                    val buildType = if (bTask.name.contains("release")) {
                        "Release"
                    } else {
                        "Debug"
                    }
                    try {
                        val publishMavenTask =
                            childProject.project.tasks.named("publishMaven${buildType}PublicationToLocalRepository").orNull
                        publishMavenTask?.let {
                            bTask.finalizedBy(it)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                }
            } else {
                //copy aar
                val localMavenTask =
                    childProject.tasks.maybeCreate("uploadLocalMaven" + buildType.capitalize(),
                        LocalMavenTask::class.java)
                localMavenTask.localMaven = this@AarFlatLocalMaven
                bundleTask?.finalizedBy(localMavenTask)
            }
        }
    }

    //获取 gradle 里的 bundleXXXAar task , 为了就是打包每一个模块的 aar
    fun getBundleTask(project: Project, variantName: String): Task? {
        var taskPath = "bundle" + variantName + "Aar"
        var bundleTask: TaskProvider<Task>? = null
        try {
            bundleTask = project.tasks.named(taskPath)
        } catch (ignored: Exception) {
        }
        return bundleTask?.get()
    }

    //需要构建 local maven
    open class LocalMavenTask : DefaultTask() {
        var inputPath: String? = null
        var inputFile: File? = null
        var outputPath: String? = null
        var outputDir: File? = null
        lateinit var localMaven: AarFlatLocalMaven

        @TaskAction
        fun uploadLocalMaven() {
            this.inputPath = FileUtil.findFirstLevelAarPath(getProject())
            this.outputPath = FileUtil.getLocalMavenCacheDir()
            inputFile = inputPath?.let { File(it) }
            outputDir = File(this.outputPath)

            inputFile?.let {
                File(outputDir, getProject().name + ".aar").let { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                it.copyTo(File(outputDir, getProject().name + ".aar"), true)
                localMaven.putIntoLocalMaven(getProject().name, getProject().name + ".aar")
            }
        }
    }


}




