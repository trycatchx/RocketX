package plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.*
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.utils.FileUtil
import plugin.utils.getChangeModuleMap
import plugin.utils.hasAndroidPlugin
import java.io.File
import java.nio.file.Files

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 *
 *
 * mac debug 插件命令 ：export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 * window debug 插件命令 ：set GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 */
open class RocketXPlugin : Plugin<Project> {

    companion object {
        const val TAG = "RocketXPlugin:"
        const val ASSEMBLE = "assemble"
    }

    lateinit var appProject: Project
    lateinit var android: AppExtension
    lateinit var mAppProjectDependencies: AppProjectDependencies
    val mAllChangedProject by lazy {
        getChangeModuleMap(appProject)
    }

    override fun apply(project: Project) {
        //应用在 主 project 上，也就是 app module
        if (hasAndroidPlugin(project)) return
        this.appProject = project
        FileUtil.attach(project)
        flatDirs()
        android = project.extensions.getByType(AppExtension::class.java)
        println(TAG + " =============changed project=================")
        mAllChangedProject?.forEach {
            println(TAG + "name: " + it.key)
        }
        println(TAG + " =============changed project================= end")

        mAppProjectDependencies = AppProjectDependencies(project, android, mAllChangedProject) {
            pritlnDependencyGraph()
        }

        appProject.gradle.projectsEvaluated {
            doAfterEvaluated()
        }
    }

    /**
     * 等同效果：
     *  allprojects {
     *     repositories {
     *        flatDir {
     *            dirs getRootProject().file('.rocketxcache')
     *        }
     *     }
     *   }
     */
    fun flatDirs() {
        val map = mutableMapOf<String, File>()
        map.put("dirs", appProject.rootProject.file(".rocketxcache"))
        appProject.rootProject.allprojects {
            it.repositories.flatDir(map)
        }
    }


    /**
     * hook projectsEvaluated 加入 bundleaar task 和 localMaven task
     */
    fun doAfterEvaluated() {
        appProject.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (it.name.equals("app") || it == appProject.rootProject) return@forEach

            val childProject = it.project
            val childAndroid = it.project.extensions.getByType(LibraryExtension::class.java)
            childAndroid.buildTypes.all { buildType ->
                appProject.tasks.named(ASSEMBLE + buildType.name.capitalize())?.let { task ->
                    //如果当前模块是改动模块，需要打 aar
                    if (mAllChangedProject?.contains(childProject.name) ?: false) {
                        val bundleTask = getBundleTask(childProject, buildType.name.capitalize())?.apply {
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
                            localMavenTask.setPath(inputPath,
                                FileUtil.getLocalMavenCacheDir())
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
                                    localMavenTask.setPath(inputPath,
                                        FileUtil.getLocalMavenCacheDir())
                                    bundleTask?.finalizedBy(localMavenTask)
                                }
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

        fun setPath(inputPath: String, outputDir: String) {
            this.inputPath = inputPath
            this.outputDir = outputDir
            inputFile = File(this.inputPath)
            outputFile = File(this.outputDir)
        }

        @TaskAction
        fun uploadLocalMaven() {
            //todo  upload
            println(TAG + "uploadLocalMaven inputPath:" + inputPath)
            println(TAG + "uploadLocalMaven outputDir:" + outputDir)
            File(outputFile, getProject().name + ".aar").let { file ->
                if (file.exists()) {
                    println(TAG + "uploadLocalMaven delete")
                    file.delete()
                }
            }
            inputFile.copyTo(File(outputFile, getProject().name + ".aar"), true)
        }
    }


    //打印处理完的整个依赖图
    fun pritlnDependencyGraph() {
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach {
            println(TAG + "project name:" + it.project.name)
            it.allConfigList.forEach {
                it.dependencies.forEach {
                    println(TAG + "dependency:" + it.toString())
                }
            }
        }
    }

}


