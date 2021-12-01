package plugin.utils

import org.gradle.api.Project
import plugin.RocketXPlugin
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


fun isRunAssembleTask(curProject: Project): Boolean {
    return curProject.projectDir.absolutePath.equals(curProject.gradle.startParameter.currentDir.absolutePath)
}


fun isEnable(curProject: Project): Boolean {
    val enableFile =
        File(curProject.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + "rocketXEnable")
    return enableFile.exists()
}

//通过 startParameter 获取  FlavorBuildType
fun getFlavorBuildType(appProject: Project):String {
    var flavorBuildType = ""
    val arg = appProject.gradle.startParameter?.taskRequests?.getOrNull(0)?.args?.getOrNull(0)
    if(!arg.isNullOrEmpty()) {
        var index = arg.indexOf(RocketXPlugin.ASSEMBLE)
        index = if(index > -1) index + RocketXPlugin.ASSEMBLE.length else 0
        flavorBuildType = arg.substring(index,arg.length)
    }
    if (flavorBuildType.length > 0) {
        flavorBuildType =
            flavorBuildType.substring(0, 1).toLowerCase() + flavorBuildType.substring(1)
    }

    return flavorBuildType
}