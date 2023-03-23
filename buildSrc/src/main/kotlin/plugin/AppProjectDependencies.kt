package plugin

import com.android.build.gradle.AppExtension
import org.gradle.BuildListener
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.internal.dispatch.MethodInvocation
import org.gradle.internal.event.BroadcastDispatch
import org.gradle.internal.event.ListenerBroadcast
import org.gradle.invocation.DefaultGradle
import plugin.bean.RocketXBean
import plugin.utils.DependenciesHelper
import java.util.*

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class AppProjectDependencies(
    private val project: Project,
    private val android: AppExtension,
    private val rocketXBean: RocketXBean?,
    private val mAllChangedProject: MutableMap<String, Project>? = null,
    private val listener: ((finish: Boolean) -> Unit)? = null) {

    var isFirst = true

    val mAllChildProjectDependenciesList = arrayListOf<ChildProjectDependencies>()

    private lateinit var mDependenciesHelper: DependenciesHelper

    init {
        val projectsEvaluatedList = hookProjectsEvaluatedAction()
        project.gradle.projectsEvaluated {
            // 调整依赖时机
            if (isFirst) {
                isFirst = false
                //先执行重依赖
                resolveDependency()
                //后执行移除的监听（主要调整执行顺序，重依赖才能生效和不报错，可能有AGP 版本兼容问题）
                val clazz = Class.forName("org.gradle.api.invocation.Gradle")
                val method = clazz.getDeclaredMethod("projectsEvaluated", Action::class.java)
                val mMethodInvocation = MethodInvocation(method, arrayOf(it))
                projectsEvaluatedList.forEach {
                    it.dispatch(mMethodInvocation)
                }
            }
        }
    }


    //把所有 监听了 projectsEvaluated 的匿名内部类移除
    fun hookProjectsEvaluatedAction(): List<BroadcastDispatch<BuildListener>> {
        val removeDispatch = mutableListOf<BroadcastDispatch<BuildListener>>()
        try {
            var buildListenerBroadcast: ListenerBroadcast<BuildListener>? = null
            val fBuildListenerBroadcast =
                DefaultGradle::class.java.getDeclaredField("buildListenerBroadcast")
            fBuildListenerBroadcast.isAccessible = true
            buildListenerBroadcast =
                fBuildListenerBroadcast.get(project.gradle) as? ListenerBroadcast<BuildListener>

            val fBroadcast = ListenerBroadcast::class.java.getDeclaredField("broadcast")
            fBroadcast.isAccessible = true
            val broadcast: BroadcastDispatch<BuildListener>? =
                fBroadcast.get(buildListenerBroadcast) as? BroadcastDispatch<BuildListener>
            val fDispatchers = broadcast?.javaClass?.getDeclaredField("dispatchers")
            fDispatchers?.isAccessible = true
            val dispatchers: ArrayList<BroadcastDispatch<BuildListener>>? =
                fDispatchers?.get(broadcast) as? ArrayList<BroadcastDispatch<BuildListener>>

            val clazz =
                Class.forName("org.gradle.internal.event.BroadcastDispatch\$ActionInvocationHandler")
            val iterator = dispatchers?.iterator()
            iterator?.let {
                while (iterator.hasNext()) {
                    try {
                        val next = iterator.next()
                        val fDispatch = next.javaClass.getDeclaredField("dispatch")
                        fDispatch.isAccessible = true
                        val dispatch: Any? = fDispatch.get(next)
                        val fMethodName = clazz.getDeclaredField("methodName")
                        fMethodName.isAccessible = true
                        val methodName = fMethodName.get(dispatch) as? String
                        if (methodName?.contains("projectsEvaluated") == true) {
                            removeDispatch.add(next)
                            iterator.remove()
                        }
                    } catch (ignore: Exception) {
                    }
                }
            }
        } catch (ignore: Exception) {
        }
        return removeDispatch
    }

    private fun resolveDependency() {
        //剔除 rootProject 和 有多级目录的 parent folder
        project.rootProject.allprojects.filter { it != project.rootProject && it.childProjects.isEmpty() }.forEach {
            //每一个 project 的依赖，都在 ProjectDependencies 里面解决
            val project = ChildProjectDependencies(it, android, mAllChangedProject)
            mAllChildProjectDependenciesList.add(project)
        }

        //生成拥有整个依赖图的工具类（只能在此处才能生成）
        mDependenciesHelper = DependenciesHelper(rocketXBean, mAllChildProjectDependenciesList)

        mAllChildProjectDependenciesList.forEach {
            it.doDependencies(mDependenciesHelper)
        }
        listener?.invoke(true)
    }

}