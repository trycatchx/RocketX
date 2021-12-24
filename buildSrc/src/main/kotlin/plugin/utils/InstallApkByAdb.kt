package plugin.utils

import com.android.build.gradle.AppExtension
import com.android.ddmlib.AndroidDebugBridge
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import plugin.RocketXPlugin

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/27
 * copyright TCL+
 */
class InstallApkByAdb(val appProject: Project) {


    fun maybeInstallApkByAdb() {
        if (isRunAssembleTask(appProject)) {
            val android = appProject.extensions.getByType(AppExtension::class.java)
            val installTask =
                appProject.tasks.maybeCreate("rocketxInstallTask", InstallApkTask::class.java)
            installTask.android = android
            android.applicationVariants.forEach {
                getAppAssembleTask(RocketXPlugin.ASSEMBLE + it.flavorName.capitalize() + it.buildType.name.capitalize())?.let { taskProvider ->
                    taskProvider.configure {
                        it.finalizedBy(installTask)
                    }
                }
            }
        }
    }

    fun getAppAssembleTask(name: String): TaskProvider<Task>? {
        var taskProvider: TaskProvider<Task>? = null
        try {
            taskProvider = appProject.tasks.named(name)
        } catch (ignore: Exception) {
        }
        return taskProvider
    }


    open class InstallApkTask : DefaultTask() {
        @Internal
        lateinit var android: AppExtension

        @TaskAction
        fun installApk() {
            val adb = android.adbExecutable.absolutePath

            try {

                AndroidDebugBridge.initIfNeeded(false)
                val bridge = AndroidDebugBridge.createBridge(android.adbExecutable.path, false)
                var firstLocalDeviceSerinum = ""
                bridge?.devices?.forEach {
                    if (!it.serialNumber.isNullOrEmpty()) {
                        firstLocalDeviceSerinum = it.serialNumber
                        return@forEach
                    }
                }

                if (firstLocalDeviceSerinum.isNullOrEmpty().not()) {
                    project.exec {
                        it.commandLine(adb,
                            "-s",
                            firstLocalDeviceSerinum,
                            "install",
                            "-r",
                            FileUtil.getApkLocalPath())
                    }
                    project.exec {
                        it.commandLine(adb,
                                "shell",
                                "monkey",
                                "-p",
                                android.defaultConfig.applicationId,
                                "-c",
                                "android.intent.category.LAUNCHER",
                                "1")
                    }
                }
            } catch (e: Exception) {
                LogUtil.d("install fail:" + e.toString())
            }

        }
    }


}