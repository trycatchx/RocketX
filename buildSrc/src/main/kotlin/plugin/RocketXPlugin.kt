package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import plugin.utils.getChangeModuleMap

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
    }

    lateinit var project: Project
    lateinit var android: AppExtension
    lateinit var mAppProjectDependencies: AppProjectDependencies

    override fun apply(project: Project) {
        this.project = project
        //应用在 主 project 上，也就是 app module
        if (checkAndroidPlugin()) return
        val android = project.extensions.getByType(AppExtension::class.java)
        mAppProjectDependencies = AppProjectDependencies(project, android) {
            pritlnDependencyGraph()
        }

        pritlnProjectChanged()
    }


    //打印所有改动的模块

    fun pritlnProjectChanged() {
        val changeMap = getChangeModuleMap(project.rootProject)
        changeMap?.forEach {
            println(TAG + "check changed project: " + it.key)
        }
    }

    //打印处理完的整个依赖图
    fun pritlnDependencyGraph() {
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach {
            println(TAG + "project name:" + it.project.name)
            it.allConfigList.forEach {
                it.dependencies.forEach {
                    println(TAG + "dependency:"+it.toString())
                }
            }
        }
    }


    //判断是否子 project 的
    fun checkAndroidPlugin(): Boolean {
        return project.plugins.hasPlugin("com.android.library")
    }
}


