package plugin.localmaven

import com.android.build.gradle.LibraryExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.RocketXPlugin
import plugin.utils.FileUtil
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
    var childAndroid: LibraryExtension,
    var appProject: Project,
    var mAllChangedProject: MutableMap<String, Project>? = null) : LocalMaven() {

    companion object {
        const val ASSEMBLE = "assemble"
    }


    override fun uploadLocalMaven() {
        //先 hook bundleXXaar task 打出包
        childAndroid.buildTypes.all { buildType ->
            appProject.tasks.named(ASSEMBLE + buildType.name.capitalize())?.let { task ->
                //如果当前模块是改动模块，需要打 aar
                if (mAllChangedProject?.contains(childProject.name) ?: false) {
                    val bundleTask =
                        getBundleTask(childProject, buildType.name.capitalize())?.apply {
                            task.configure {
                                it.finalizedBy(this)
                            }
                        }

                    /**
                     * 上传 aar
                     */
                    var localMavenTask =
                        childProject.tasks.create("uploadLocalMaven" + buildType.name.capitalize(),
                            LocalMavenTask::class.java)
                    //找到 aar
                    FileUtil.findFirstLevelAarPath(childProject)?.let { inputPath ->
                        localMavenTask.setPath(this@AarFlatLocalMaven,inputPath, FileUtil.getLocalMavenCacheDir())
                        bundleTask?.finalizedBy(localMavenTask)
                    }

                }
            }
        }

        childAndroid.productFlavors.all { flavor ->
            childAndroid.buildTypes.all { buildType ->
                appProject.tasks.findByPath(ASSEMBLE + flavor.name.capitalize() + buildType.name.capitalize())
                    ?.let { task ->
                        //如果当前模块是改动模块，需要打 aar
                        if (mAllChangedProject?.contains(appProject.name) ?: false) {
                            val bundleTask = getBundleTask(childProject,
                                flavor.name.capitalize() + buildType.name.capitalize())?.apply {
                                task.finalizedBy(this)
                            }

                            var localMavenTask =
                                childProject.tasks.create("uploadLocalMaven" + flavor.name.capitalize() + buildType.name,
                                    LocalMavenTask::class.java)
                            FileUtil.findFirstLevelAarPath(childProject)?.let { inputPath ->
                                localMavenTask.setPath(this@AarFlatLocalMaven,inputPath, FileUtil.getLocalMavenCacheDir())
                                bundleTask?.finalizedBy(localMavenTask)
                            }
                        }
                    }
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
        lateinit var inputPath: String
        lateinit var outputDir: String
        lateinit var inputFile: File
        lateinit var outputFile: File
        lateinit var localMaven: LocalMaven

        fun setPath(localMaven:LocalMaven,inputPath: String, outputDir: String) {
            this.inputPath = inputPath
            this.outputDir = outputDir
            this.localMaven = localMaven
            inputFile = File(this.inputPath)
            outputFile = File(this.outputDir)
        }

        @TaskAction
        fun uploadLocalMaven() {
            //todo  upload
            println(RocketXPlugin.TAG + "uploadLocalMaven inputPath:" + inputPath)
            println(RocketXPlugin.TAG + "uploadLocalMaven outputDir:" + outputDir)
            File(outputFile, getProject().name + ".aar").let { file ->
                if (file.exists()) {
                    println(RocketXPlugin.TAG + "uploadLocalMaven delete")
                    file.delete()
                }
            }
            inputFile.copyTo(File(outputFile, getProject().name + ".aar"), true)

           localMaven.putIntoLocalMaven(getProject().name,getProject().name + ".aar")
        }
    }




}




