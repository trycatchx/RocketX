package plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.*
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.utils.findFirstAarPath
import plugin.utils.getChangeModuleMap
import java.io.File

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
        getChangeModuleMap(appProject.rootProject)
    }

    override fun apply(project: Project) {
        this.appProject = project
        //应用在 主 project 上，也就是 app module
        if (hasAndroidPlugin(project)) return
        android = project.extensions.getByType(AppExtension::class.java)
        mAppProjectDependencies = AppProjectDependencies(project, android, mAllChangedProject) {
            pritlnDependencyGraph()
        }

        appProject.gradle.projectsEvaluated {
            doAfterEvaluated()
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
//                    if (mAllChangedProject?.contains(childProject.name) ?: false) {
                    if (true) {
                        getBundleTask(childProject, buildType.name.capitalize())?.apply {
                            task.configure {
                                it.finalizedBy(this)
                            }
                        }
                        var localMavenTask =
                            childProject.tasks.create("uploadLocalMaven"  + buildType.name.capitalize(),
                                LocalMavenTask::class.java)

                        //找到 aar
                        findFirstAarPath(childProject)?.let { inputPath ->
                            localMavenTask.setPath(inputPath,
                                childProject.rootProject.rootDir.absolutePath + "/.rocketxcache/")
                            task.configure {
                                it.finalizedBy(localMavenTask)
                            }
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
                                getBundleTask(childProject,
                                    flavor.name.capitalize() + buildType.name.capitalize())?.apply {
                                    task.finalizedBy(this)
                                }
                                var localMavenTask =
                                    childProject.tasks.create("uploadLocalMaven" + flavor.name.capitalize() + buildType.name,
                                        LocalMavenTask::class.java)
                                findFirstAarPath(childProject)?.let { inputPath ->
                                    localMavenTask.setPath(inputPath,
                                        childProject.rootProject.rootDir.absolutePath + "/.rocketxcache/")
                                    task.finalizedBy(localMavenTask)
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
            File(outputFile, getProject().name+".aar").also { file->
                if(file.exists()) {
                    file.delete()
                }
            }

            getProject().copy {
                it.from(inputPath)
                it.into(outputDir)
                it.rename {
                    it.replace(inputFile.name, getProject().name+".aar")
                }
            }
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


    //判断是否子 project 的
    fun hasAndroidPlugin(curProject: Project): Boolean {
        return curProject.plugins.hasPlugin("com.android.library")
    }
}


