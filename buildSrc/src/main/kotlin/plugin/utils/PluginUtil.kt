package plugin.utils

import com.sun.org.apache.xpath.internal.operations.Bool
import org.gradle.api.Project
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/2
 * copyright TCL+
 */


//判断是否子 project 的
fun hasAndroidPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("com.android.library")
}

//判断是否子 project 的
fun hasAppPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("com.android.application")
}

//判断是否java project 的
fun hasJavaPlugin(curProject: Project): Boolean {
    return curProject.plugins.hasPlugin("java-library")
}


fun isEnable(curProject: Project): Boolean {
    val enableFile =
        File(curProject.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + "rocketXEnable")
    return enableFile.exists()
}