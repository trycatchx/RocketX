package plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.*
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import plugin.bean.RocketXBean
import plugin.localmaven.AarFlatLocalMaven
import plugin.localmaven.JarFlatLocalMaven
import plugin.localmaven.LocalMaven
import plugin.localmaven.mavenPublish
import plugin.utils.*
import plugin.utils.FileUtil.getLocalMavenCacheDir
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
    var mRocketXBean: RocketXBean? = null
    lateinit var mAppProjectDependencies: AppProjectDependencies
    val mAllChangedProject by lazy {
        ChangeModuleUtils.getChangeModuleMap(appProject)
    }

    override fun apply(project: Project) {
        //应用在 主 project 上，也就是 app module
        mRocketXBean = project.extensions.create("RocketX", RocketXBean::class.java)
        println("mRocketXBean mavenEnable=${mRocketXBean?.localMaven}")
        if (!isEnable(project) || hasAndroidPlugin(project)) return
        this.appProject = project
        FileUtil.attach(project)
        flatDirs()
        android = project.extensions.getByType(AppExtension::class.java)

        println(TAG + " =============changed project=================")
        mAllChangedProject?.forEach {
            println(TAG + "name: " + it.key)
        }
        println(TAG + " =============changed project================= end")


        appProject.afterEvaluate {
            println("mRocketXBean mavenEnable=${mRocketXBean?.localMaven}")
            if (mRocketXBean?.localMaven == true) {
                appProject.rootProject.allprojects.forEach {
                    if (it.name.equals("app") || it == appProject.rootProject || it.childProjects.isNotEmpty()) {
                        return@forEach
                    }
                    // 配置maven publish
                    it.mavenPublish(mRocketXBean)
                }
            }
            appProject.gradle.projectsEvaluated {
                doAfterEvaluated()
            }
        }

        mAppProjectDependencies = AppProjectDependencies(project, android, mRocketXBean, mAllChangedProject) {
            pritlnDependencyGraph()
        }

        appProject.gradle.taskGraph.addTaskExecutionListener(object : TaskExecutionListener {
            override fun beforeExecute(p0: Task) {
            }

            override fun afterExecute(task: Task, state: TaskState) {
                println("task==>${task.name}, state=${state.failure}")
                if (task.name.startsWith(ASSEMBLE) && state.failure == null) {
                    println("task==>${task.name}, state=${state.failure}")
                    ChangeModuleUtils.flushJsonFile()
                }
            }
        })

        appProject.gradle.buildFinished {
            println("Testset buildFinished")
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
        map.put("dirs", File(getLocalMavenCacheDir()))
        appProject.rootProject.allprojects {
            it.repositories.flatDir(map)
        }
    }


    /**
     * hook projectsEvaluated 加入 bundleaar task 和 localMaven task
     */
    fun doAfterEvaluated() {

        CleanDuplicateJarJob(appProject, mAllChangedProject).runCleanAction()

        appProject.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (it.name.equals("app") || it == appProject.rootProject || it.childProjects.size > 0) return@forEach
            if (mAllChangedProject?.contains(it.path)?.not() != false) return@forEach
            var mLocalMaven: LocalMaven? = null
            val childProject = it.project
            var childAndroid: LibraryExtension? = null
            try {
                childAndroid = it.project.extensions.getByType(LibraryExtension::class.java)
            } catch (ignore: Exception) {
            }
            //android 子 module
            if (childAndroid != null) {
                mLocalMaven = AarFlatLocalMaven(
                    childProject,
                    this@RocketXPlugin,
                    appProject,
                    mAllChangedProject
                )
            } else if (hasJavaPlugin(childProject)) {
                //java 子 module
                mLocalMaven =
                    JarFlatLocalMaven(childProject, this@RocketXPlugin, mAllChangedProject)
            }
            //需要上传到 localMaven
            mLocalMaven?.uploadLocalMaven()
        }

    }


    //打印处理完的整个依赖图
    fun pritlnDependencyGraph() {
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach {
            println(TAG + "======project name: ${it.project.name}==========")
            it.allConfigList.forEach {
                if (it.dependencies.size > 0) {
                    println(TAG + "=====Config name:${it.name} ===== ")
                    it.dependencies.forEach {
                        println(TAG + "dependency:" + it.hashCode())
                        println(TAG + "dependency:" + it)
                    }
                }
            }

            println(TAG + "======project name: ========== end")
        }
    }

}


