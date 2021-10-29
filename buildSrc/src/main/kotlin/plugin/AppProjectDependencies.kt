package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import plugin.utils.DependenciesHelper
import plugin.utils.getChangeModuleMap

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class AppProjectDependencies(
    var project: Project,
    var android: AppExtension,
    val mAllChangedProject: MutableMap<String, Project>?= null,
    var listener: ((finish: Boolean) -> Unit)? = null) : DependencyResolutionListener {


    var mAllChildProjectDependenciesList = arrayListOf<ChildProjectDependencies>()
    lateinit var mDependenciesHelper: DependenciesHelper

    init {
        project.gradle.addListener(this)
    }

    override fun beforeResolve(p0: ResolvableDependencies) {
        project.gradle.removeListener(this)
        project.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (hasAndroidPlugin(it)) {
                //每一个 project 的依赖，都在 ProjectDependencies 里面解决
                val project = ChildProjectDependencies(it, android, mAllChangedProject)
                mAllChildProjectDependenciesList.add(project)
            }
        }
        //生成拥有整个依赖图的工具类（只能在此处才能生成）
        mDependenciesHelper = DependenciesHelper(mAllChildProjectDependenciesList)
        mAllChildProjectDependenciesList.forEach {
            it.doDependencies(mDependenciesHelper)
        }
        listener?.invoke(true)
    }

    override fun afterResolve(p0: ResolvableDependencies) {

    }


    //判断是否子 project 的
    fun hasAndroidPlugin(curProject: Project): Boolean {
        return curProject.plugins.hasPlugin("com.android.library")
    }

}