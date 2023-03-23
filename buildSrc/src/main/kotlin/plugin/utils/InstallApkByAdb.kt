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
import java.util.concurrent.TimeUnit

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
            val installTask = appProject.tasks.maybeCreate("rocketxInstallTask", InstallApkTask::class.java)
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

    private fun getAppAssembleTask(name: String): TaskProvider<Task>? {
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
                val bridge = AndroidDebugBridge.createBridge(android.adbExecutable.path, false,
                    Long.MAX_VALUE,
                    TimeUnit.MILLISECONDS)
                var firstLocalDeviceSerialNum = ""
                run loop@{
                    bridge?.devices?.forEach {
                        if (!it.serialNumber.isNullOrEmpty()) {
                            firstLocalDeviceSerialNum = it.serialNumber
                            return@loop
                        }
                    }
                }
                if (firstLocalDeviceSerialNum.isEmpty().not()) {

                    project.exec {
                        it.commandLine(adb, "-s", firstLocalDeviceSerialNum, "install", "-r", FileUtil.getApkLocalPath())
                    }
                    // adb -s <ip:port> install -r <app.apk>
                    // adb -s <ip:port> shell monkey -p <包名> -c android.intent.category.LAUNCHER 1
                    project.exec {
                        it.commandLine(
                            adb,
                            "-s",
                            firstLocalDeviceSerialNum,
                            "shell",
                            "monkey",
                            "-p",
                            android.defaultConfig.applicationId,
                            "-c",
                            "android.intent.category.LAUNCHER",
                            "1"
                        )
                    }
                }
            } catch (e: Exception) {
                LogUtil.d("install fail:" + e.toString())
            }
        }
    }


}