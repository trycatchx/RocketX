package plugin.utils

/**
 * description:   Log工具类，为了统一log输出
 * author: Louis
 * data: 2021/11/16
 * copyright
 */
object LogUtil {
    private var mTag = "Undefine"

    private var enable = false

    fun init(tag:String) { mTag = tag}

    fun clear(){ mTag = "Undefine" }

    fun enableLog(enable: Boolean) {
        this.enable = enable
    }

    fun d(msg: String) {
        if (!enable) return
        println("$mTag ： $msg")
    }

}