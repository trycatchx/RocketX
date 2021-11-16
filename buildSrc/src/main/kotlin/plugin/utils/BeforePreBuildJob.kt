package plugin.utils

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.SubStream
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import plugin.localmaven.AarFlatLocalMaven
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/5
 * copyright TCL+
 *
 * 清除一些第三方使用的 tranform ，导致jar 重复问题,譬如 alibaba
 */
open class BeforePreBuildJob(
    var appProject: Project,
    val mAllChangedProject: MutableMap<String, Project>? = null,
    val mLastChangeProject: MutableSet<String>? = null) {

    companion object {
        const val TRANSFORMS = "/intermediates/transforms/"
        const val DATABIND_DENPEDENCY =
            "/intermediates/data_binding_base_class_logs_dependency_artifacts/"
        const val PRE = "pre"
        const val Build = "Build"
    }

    fun runCleanAction() {
        val android = appProject.extensions.getByType(AppExtension::class.java)

        android.applicationVariants.forEach {
            getTaskProvider(PRE + it.flavorName.capitalize() +  it.buildType.name.capitalize() + Build)?.let { task ->
                innerRunCleanAction(task, it.buildType.name, it.flavorName)
            }
        }

    }


    fun getTaskProvider(taskname: String): TaskProvider<Task>? {

        var bundleTask: TaskProvider<Task>? = null
        try {
            bundleTask = appProject.tasks.named(taskname)
        } catch (ignored: Exception) {
        }
        return bundleTask
    }


    private fun innerRunCleanAction(
        task: TaskProvider<Task>, buildType: String, flavor: String? = null) {
        //清理 jar
        task.configure {
            it.doFirst {
                CleanDuplicateAction().let {
                    it.job = this@BeforePreBuildJob
                    it.flavor = flavor
                    it.buildType = buildType
                    it.clean()
                }
                FilePermissionAction().let {
                    it.job = this@BeforePreBuildJob
                    it.flavor = flavor
                    it.buildType = buildType
                    it.modify()
                }
            }
        }
    }


    open class CleanDuplicateAction {
        lateinit var job: BeforePreBuildJob
        var flavor: String? = null
        lateinit var buildType: String

        fun clean() {
            val destDir = File(job.appProject.buildDir.absolutePath, TRANSFORMS)
            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
        }
    }

    open class FilePermissionAction {
        lateinit var job: BeforePreBuildJob
        var flavor: String? = null
        lateinit var buildType: String

        fun modify() {
            val variant = (flavor ?: "") + buildType.capitalize()
            val destDir = File(job.appProject.buildDir.absolutePath,
                DATABIND_DENPEDENCY + variant + File.separator + "out")
            if (destDir.exists()) {
                destDir.deleteRecursively()
            }
        }
    }


}