package plugin.utils

import plugin.AppProjectDependencies

/**
 * description:   Log工具类，为了统一log输出
 * author: Louis
 * data: 2021/11/16
 * copyright
 */
object LogUtil {
    private var mTag = "RocketXPlugin"

    private var enable = false

    fun init(tag:String) { mTag = tag}

    fun clear(){ mTag = "RocketXPlugin" }

    fun enableLog(enable: Boolean) {
        this.enable = enable
    }

    fun d(msg: String) {
        if (!enable) return
        println(">>>>  $msg")
    }
    //打印处理完的整个依赖图
    fun pritlnDependencyGraph(mAppProjectDependencies: AppProjectDependencies) {
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach { it ->
            d("======project name: ${it.project.name}========== start")
            it.allConfigList.filter { it.dependencies.isNotEmpty() }.forEach { configuration ->
                d("======Config name:${configuration.name}")
                configuration.dependencies.forEach {
                    d("dependency:   $it    ${it.hashCode()}")
                }
            }
            d("======project name: ${it.project.name}========== end \n")
        }
    }
}