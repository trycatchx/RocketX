package plugin.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.SubStream
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/5
 * copyright TCL+
 *
 * 清除一些第三方使用的 tranform ，导致jar 重复问题
 */
open class CleanDuplicateJarJob(
        var appProject: Project,
        val mAllChangedProject: MutableMap<String, Project>? = null
) {

    companion object {
        const val TRANSFORMS = "/intermediates/transforms/"
        const val PRE = "pre"
        const val Build = "Build"
    }


    fun runCleanAction() {
        val android = appProject.extensions.getByType(AppExtension::class.java)

        android.buildTypes.all { buildType ->
            getTaskProvider(PRE + buildType.name.capitalize()+Build)?.let { task ->
                innerRunCleanAction(task, buildType.name)
            }
        }


        android.productFlavors.all { flavor ->
            android.buildTypes.all { buildType ->
                getTaskProvider(PRE + flavor.name.capitalize() + buildType.name.capitalize()+Build)
                        ?.let { task ->
                            innerRunCleanAction(task, buildType.name, flavor.name)
                        }
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


    private fun innerRunCleanAction(task: TaskProvider<Task>, buildType: String, flavor: String? = null) {
        //清理 jar
        task.configure {
           it.doFirst {
               CleanDuplicateAction().let {
                   it.job = this@CleanDuplicateJarJob
                   it.flavor = flavor
                   it.buildType = buildType
                   it.clean()
               }
           }
        }
    }


    open class CleanDuplicateAction  {
        lateinit var job: CleanDuplicateJarJob
        var flavor: String? = null
        lateinit var buildType: String

        fun clean() {
            val destDir = File(job.appProject.buildDir.absolutePath, TRANSFORMS)
            if (destDir.exists()) {
                val allThirdPg = destDir.listFiles()
                allThirdPg.forEach {
                    val jarDir = File(it.absolutePath,
                            if (flavor != null) flavor + File.separator + buildType else buildType)
                    if (jarDir.exists()) {
                        val contentFile = File(jarDir.absolutePath, SubStream.FN_FOLDER_CONTENT)
                        if (contentFile.exists()) {
                            //开始清理
                            cleanJarByContentJson(jarDir)
                        }
                    }
                }
            }
        }


        fun cleanJarByContentJson(jarDir: File) {
            val subStreams = SubStream.loadSubStreams(jarDir)
            val iterator = subStreams.iterator()
            while (iterator.hasNext()) {
                val subStream = iterator.next()

                val moduleNameArray = subStream.name.split(":")
                var moduleName :String? = null
                if(moduleNameArray.size > 1){
                    moduleName = moduleNameArray[1]
                }
                moduleName?.let {
                    if(moduleIsChange(it)) {
                        File(jarDir.absolutePath,subStream.filename).delete()
                        iterator.remove()
                    }
                }
            }
            SubStream.save(subStreams,jarDir)
        }


        //模块是否改动
        fun moduleIsChange(name: String):Boolean {
            var ret = job.mAllChangedProject?.get(name) != null
            return ret
        }
    }


}