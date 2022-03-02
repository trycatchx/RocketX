package plugin.utils

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

}