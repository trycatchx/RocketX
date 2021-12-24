package plugin.localmaven

import com.android.build.gradle.AppExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.RocketXPlugin
import plugin.utils.FileUtil
import plugin.utils.LogUtil
import plugin.utils.getFlatAarName
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
        // 创建一个线程池
//        val threadPoolExecutor = initThread()
        //先 hook bundleXXaar task 打出包
        val android = appProject.extensions.getByType(AppExtension::class.java)
        android.applicationVariants.forEach {
//            threadPoolExecutor.execute {
            LogUtil.d("thread_ ${it.name}  ${Thread.currentThread().id}")
            getAppAssembleTask(ASSEMBLE + it.flavorName.capitalize() + it.buildType.name.capitalize())?.let { task ->
                    hookBundleAarTask(task, it.buildType.name)
                }
//            }
        }

    }

    private fun initThread(): ThreadPoolExecutor {
        /** DES: DES：取CPU核心数-1 代码来自协程内部 [kotlinx.coroutines.CommonPool.createPlainPool] */
        val corePoolSize = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
        val threadPoolExecutor = ThreadPoolExecutor(corePoolSize,
            corePoolSize,
            5L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>())
        // DES：让核心线程也可以回收
        threadPoolExecutor.allowCoreThreadTimeOut(true)
        return threadPoolExecutor
    }



    fun getAppAssembleTask(name: String): TaskProvider<Task>? {
        var taskProvider: TaskProvider<Task>? = null
        try {
            taskProvider = appProject.tasks.named(name)
        } catch (ignore: Exception) {
        }
        return taskProvider
    }

    @Synchronized
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
                        FlatTask::class.java)
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
    open class FlatTask : DefaultTask() {
        @Internal
        var inputPath: String? = null
        @Internal
        var inputFile: File? = null
        @Internal
        var outputPath: String? = null
        @Internal
        var outputDir: File? = null
        @Internal
        lateinit var localMaven: AarFlatLocalMaven

        @TaskAction
        fun uploadLocalMaven() {
            val flatAarName = getFlatAarName(project)
            this.inputPath = FileUtil.findFirstLevelAarPath(project)
            this.outputPath = FileUtil.getLocalMavenCacheDir()
            inputFile = inputPath?.let { File(it) }
            outputDir = File(this.outputPath)

            inputFile?.let {
                File(outputDir, flatAarName + ".aar").let { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                it.copyTo(File(outputDir, flatAarName + ".aar"), true)
                localMaven.putIntoLocalMaven(flatAarName, flatAarName + ".aar")
            }
        }
    }


}




