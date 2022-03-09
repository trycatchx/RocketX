package plugin.utils

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import plugin.RocketXPlugin
import java.io.File
import java.util.*
import kotlin.reflect.jvm.isAccessible

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
    val enableFile = File(curProject.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + "rocketXEnable")
    return enableFile.exists()
}

//通过 startParameter 获取  FlavorBuildType
fun getFlavorBuildType(appProject: Project): String {
    var flavorBuildType = ""
    val arg = appProject.gradle.startParameter.taskRequests.getOrNull(0)?.args?.getOrNull(0)
    if (!arg.isNullOrEmpty()) {
        var index = arg.indexOf(RocketXPlugin.ASSEMBLE)
        index = if (index > -1) index + RocketXPlugin.ASSEMBLE.length else 0
        flavorBuildType = arg.substring(index, arg.length)
    }
    if (flavorBuildType.isNotEmpty()) {
        flavorBuildType = flavorBuildType.substring(0, 1).toLowerCase(Locale.ROOT) + flavorBuildType.substring(1)
    }
    return flavorBuildType
}

//不能通过name ，需要通过 path ，有可能有多级目录(: 作为aar名字会有冲突不能用)
fun getFlatAarName(project: Project): String {
    return project.path.substring(1).replace(":", "-")
}

fun isCurProjectRun(appProject: Project): Boolean {
    var ret = false
    var projectPath = ""
    val arg = appProject.gradle.startParameter.taskRequests.getOrNull(0)?.args?.getOrNull(0)
    if (!arg.isNullOrEmpty()) {
        var index = arg.indexOf(RocketXPlugin.ASSEMBLE)
        index = if (index > 0) index - 1 else 0
        projectPath = arg.substring(0, index)
    }
    if (projectPath.isNotEmpty()) {
        //使用 app 直接 run，currentDir 为项目目录没法使用，只能通过 截取 arg
        ret = appProject.path.equals(projectPath)
    }
    // 使用 assembledebug 命令需要这么区分
    if (appProject.gradle.startParameter.currentDir.absolutePath.equals(appProject.projectDir.absolutePath)) {
        ret = true
    }


    return ret
}


fun boostGradleOption(appProject: Project) {
    //并行运行task

    if (!appProject.hasProperty("org.gradle.daemon")) {
        appProject.rootProject.extensions.extraProperties.set("org.gradle.daemon", "true")
    }
    if (!appProject.hasProperty("kotlin.incremental")) {
        appProject.rootProject.extensions.extraProperties.set("kotlin.incremental", true)
    }
    if (!appProject.hasProperty("kotlin.incremental.java")) {
        appProject.rootProject.extensions.extraProperties.set("kotlin.incremental.java", "true")
    }
    if (!appProject.hasProperty("kotlin.incremental.js")) {
        appProject.rootProject.extensions.extraProperties.set("kotlin.incremental.js", "true")
    }
    if (!appProject.hasProperty("kotlin.caching.enabled")) {
        appProject.rootProject.extensions.extraProperties.set("kotlin.caching.enabled", "true")
    }

    if (!appProject.hasProperty("org.gradle.parallel")) {
        appProject.rootProject.extensions.extraProperties.set("org.gradle.parallel", "true")
    }
    if (!appProject.hasProperty("kotlin.parallel.tasks.in.project")) {
        appProject.rootProject.extensions.extraProperties.set("kotlin.parallel.tasks.in.project", "true")
    }
    if (!appProject.hasProperty("kapt.use.worker.api")) {
        appProject.rootProject.extensions.extraProperties.set("kapt.use.worker.api", "true")
    }
    if (!appProject.hasProperty("kapt.incremental.apt")) {
        appProject.rootProject.extensions.extraProperties.set("kapt.incremental.apt", "true")
    }
    if (!appProject.hasProperty("kapt.classloaders.cache.size")) {
        appProject.rootProject.extensions.extraProperties.set("kapt.classloaders.cache.size", "5")
    }
    if (!appProject.hasProperty("kapt.include.compile.classpath")) {
        appProject.rootProject.extensions.extraProperties.set("kapt.include.compile.classpath", "false")
    }
    if (!appProject.hasProperty("org.gradle.caching")) {
        appProject.rootProject.extensions.extraProperties.set("org.gradle.caching", "true")
    }
    if (!appProject.hasProperty("android.enableBuildCache")) {
        appProject.rootProject.extensions.extraProperties.set("android.enableBuildCache", "true")
    }

    appProject.gradle.startParameter.isParallelProjectExecutionEnabled = true
    appProject.gradle.startParameter.maxWorkerCount += 4
    var android = appProject.extensions.getByType(AppExtension::class.java)
    android.aaptOptions.cruncherEnabled = false
    android.aaptOptions.cruncherProcesses = 0

}

fun speedBuildByOption(appProject: Project, appExtension: AppExtension) {
    //禁用 arouter transform,不影响 app 运行
    val transformsFiled = BaseExtension::class.members.firstOrNull { it.name == "_transforms" }
    var excludeTransForms: List<String>? = null
    try {
        excludeTransForms = (appProject.property("excludeTransForms") as? String)?.split(" ")
    } catch (ignore: Exception) {
    }

    if (transformsFiled != null) {
        transformsFiled.isAccessible = true
        val xValue = transformsFiled.call(appExtension) as? MutableList<Transform>
        xValue?.removeAll {
            TransformsConstans.TRANSFORM.contains(it.name) || (excludeTransForms?.contains(it.name) ?: false)
        }

        if (xValue?.size ?: 0 > 0) {
            println("RocketXPlugin : the following transform were detected : ")
            xValue?.forEach {
                println("transform: " + it.name)
            }
            println("RocketXPlugin : you can disable it to speed up by this way：")
            println("transFormList = [\"" + xValue!![0].name + "\"]")
        }
    }

    boostGradleOption(appProject)
}

fun flatDirs(appProject: Project) {
    val map = mutableMapOf<String, File>()
    map["dirs"] = File(FileUtil.getLocalMavenCacheDir())
    appProject.rootProject.allprojects {
        it.repositories.flatDir(map)
    }
}