package plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.errors.DeprecationReporter
import com.android.builder.model.SigningConfig
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 */
open class RockectXPlugin : Plugin<Project> {


    lateinit var project: Project
    lateinit var android: AppExtension

    companion object {
        const val  IMPLEMENTATION_NAME = "implementation"
    }

    override fun apply(project: Project) {
        //TODO
        this.project = project
        //应用在 主 project 上，也就是 app module
        if (checkAndroidPlugin()) return;

        createConfigurations()


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


