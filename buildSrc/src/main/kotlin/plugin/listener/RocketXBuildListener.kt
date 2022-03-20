package plugin.listener

import com.android.build.gradle.LibraryExtension
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import plugin.RocketXPlugin
import plugin.localmaven.AarFlatLocalMaven
import plugin.localmaven.JarFlatLocalMaven
import plugin.localmaven.LocalMaven
import plugin.utils.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class RocketXBuildListener(
    private val rocketXPlugin: RocketXPlugin,
    private val appProject: Project,
    private val mAllChangedProject: MutableMap<String, Project>?
) : BuildListener, TaskExecutionListener {

    private var taskStartTime: Long = 0
    private var buildStartTime: Long = 0

    private val stringBuilder = StringBuilder()

    @Suppress("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd :hh:mm:ss")


    init {
        buildStartTime = System.currentTimeMillis()
        stringBuilder.append("\n")
        stringBuilder.append("构建开始时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
    }

    override fun buildStarted(gradle: Gradle) {
    }

    override fun settingsEvaluated(settings: Settings) {
    }

    override fun projectsLoaded(gradle: Gradle) {
    }

    override fun projectsEvaluated(gradle: Gradle) {
//        BeforePreBuildJob(appProject).runCleanAction()
        appProject.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (hasAppPlugin(it) || it == appProject.rootProject || it.childProjects.isNotEmpty()) {
                return@forEach
            }
            if (mAllChangedProject?.contains(it.path)?.not() != false) {
                return@forEach
            }
            var mLocalMaven: LocalMaven? = null
            val childProject = it.project
            var childAndroid: LibraryExtension? = null
            try {
                childAndroid = it.project.extensions.getByType(LibraryExtension::class.java)
            } catch (ignore: Exception) {
            }
            //android 子 module
            if (childAndroid != null) {
                mLocalMaven = AarFlatLocalMaven(childProject, rocketXPlugin, appProject, mAllChangedProject)
            } else if (hasJavaPlugin(childProject)) {
                //java 子 module
                mLocalMaven = JarFlatLocalMaven(childProject, rocketXPlugin, mAllChangedProject)
            }
            //需要上传到 localMaven
            mLocalMaven?.uploadLocalMaven()
        }

        InstallApkByAdb(appProject).maybeInstallApkByAdb()
    }

    /**
     * 构建完成回调
     */
    override fun buildFinished(result: BuildResult) {
        stringBuilder.append("构建结束时间：" + dateFormat.format(Calendar.getInstance().time) + "\n")
        val totalTime = (System.currentTimeMillis() - buildStartTime)
        stringBuilder.append("构建总耗时：" + totalTime + "ms")
        LogUtil.d("   $stringBuilder=${getNoMoreThanDigits(totalTime / 1000.00)}s")

    }

    private fun getNoMoreThanDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

    /**
     * 任务执行开始
     * This method is called immediately before a task is executed.
     * @param task The task about to be executed. Never null.
     */
    override fun beforeExecute(task: Task) {
        taskStartTime = System.currentTimeMillis()
    }

    /**
     * @param task The task which was executed. Never null.
     * @param state The task state. If the task failed with an exception, the exception is available in this
     * state. Never null.
     */
    override fun afterExecute(task: Task, state: TaskState) {
        LogUtil.d("  任务: ${task.name + "  " + task.path + " 耗时=" + (System.currentTimeMillis() - taskStartTime) + "ms"} ")

        if (task.name.startsWith(RocketXPlugin.ASSEMBLE) && state.failure == null) {
            LogUtil.d("task==>${task.name}, state=${state.failure}")
            ChangeModuleUtils.flushJsonFile()
        }
    }
}