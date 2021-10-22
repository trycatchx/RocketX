package plugin

import com.android.build.gradle.AppExtension
import com.google.gson.Gson
import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 */
open class RocketXPlugin : Plugin<Project> {


    lateinit var project: Project
    lateinit var android: AppExtension

    companion object {
        const val  IMPLEMENTATION_NAME = "implementation"
    }

    override fun apply(project: Project) {
        //TODO
        this.project = project
        //应用在 主 project 上，也就是 app module
//        if (!checkAndroidPlugin()) return
        if (checkAndroidPlugin()) return;

        createConfigurations()


        project.task("preGetChangeModule") {
            val changeMap = getChangeModuleMap(project.rootProject)
        }


    }

    fun createConfigurations() {
        var embedConf : Configuration = project.configurations.maybeCreate(IMPLEMENTATION_NAME)

        project.extensions.findByType(AppExtension::class.java)?.run {
            android = this
        }

        android.buildTypes.all {
            it.name
        }

    }


    //判断是否子 project 的
    fun checkAndroidPlugin(): Boolean {
        return project.plugins.hasPlugin("com.android.library")
    }
}


