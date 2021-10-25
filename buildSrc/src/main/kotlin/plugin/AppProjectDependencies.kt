package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import plugin.utils.DependenciesHelper

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class AppProjectDependencies(var project: Project, var android: AppExtension) :
    DependencyResolutionListener {
    var mProjectDependenciesList = arrayListOf<ChildProjectDependencies>()
    var mAllProject = mutableMapOf<String, Project>()
    lateinit var mDependenciesHelper: DependenciesHelper

    init {
        project.gradle.addListener(this)
    }

    override fun beforeResolve(p0: ResolvableDependencies) {
        project.gradle.removeListener(this)
        project.rootProject.allprojects.onEach {
            //剔除 app 和 rootProject
            if (hasAndroidPlugin(it)) {
                //保存所有的 project
                mAllProject.put(it.name, it)
                //每一个 project 的依赖，都在 ProjectDependencies 里面解决
                val project = ChildProjectDependencies(it, android)
                mProjectDependenciesList.add(project)
            }

        }
        //生成拥有整个依赖图的工具类（只能在此处才能生成）
        mDependenciesHelper = DependenciesHelper(mProjectDependenciesList)
        mProjectDependenciesList.onEach {
            it.doDependencies(mDependenciesHelper)
        }
    }

    override fun afterResolve(p0: ResolvableDependencies) {
    }


    //判断是否子 project 的
    fun hasAndroidPlugin(curProject: Project): Boolean {
        return curProject.plugins.hasPlugin("com.android.library")
    }

}