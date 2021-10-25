package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class AppProjectDependencies(var project: Project, var android: AppExtension) :
    DependencyResolutionListener {
    var mProjectDependenciesList = arrayListOf<ChildProjectDependencies>()
    init {
        project.gradle.addListener(this)
    }

    override fun beforeResolve(p0: ResolvableDependencies) {
        project.gradle.removeListener(this)
        project.rootProject.allprojects.onEach {
            //剔除 app 和 rootProject
            if (hasAndroidPlugin(it)) {
                //每一个 project 的依赖，都在 ProjectDependencies 里面解决
                val project = ChildProjectDependencies(it, android)
                mProjectDependenciesList.add(project)
            }
        }

        mProjectDependenciesList.onEach {
            it.doDependencies()
        }
    }

    override fun afterResolve(p0: ResolvableDependencies) {
    }


    //判断是否子 project 的
    fun hasAndroidPlugin(curProject:Project): Boolean {
        return curProject.plugins.hasPlugin("com.android.library")
    }


}