package plugin.utils

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.SubStream
import groovy.io.FileType
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
 * 清除一些第三方使用的 tranform ，导致jar 重复问题,譬如 alibaba
 */
open class CleanDuplicateJarJob(
    var appProject: Project,
    val mAllChangedProject: MutableMap<String, Project>? = null,
    val mLastChangeProject: MutableSet<String>? = null) {

    companion object {
        const val TRANSFORMS = "/intermediates/transforms/"
        const val PRE = "pre"
        const val Build = "Build"
    }

    fun runCleanAction() {
        val android = appProject.extensions.getByType(AppExtension::class.java)

        android.buildTypes.all { buildType ->
            getTaskProvider(PRE + buildType.name.capitalize() + Build)?.let { task ->
                innerRunCleanAction(task, buildType.name)
            }
        }


        android.productFlavors.all { flavor ->
            android.buildTypes.all { buildType ->
                getTaskProvider(PRE + flavor.name.capitalize() + buildType.name.capitalize() + Build)?.let { task ->
                    innerRunCleanAction(task, buildType.name, flavor.name)
                }
            }
        }

        // 修改build 目录下的文件的读写属性为可读可写
        appProject.rootProject.allprojects {
            val startTime = System.currentTimeMillis()
            println("startTime===>>>$startTime")
            var fileCount = 0
            var readCount = 0
            var writeCount = 0
            it.buildDir.eachFilesRecurse { file ->
                try {
                    fileCount++
                    val canRead = file.setReadable(true, true)
                    if (canRead) readCount++
                    val canWrite = file.setWritable(true)
                    if (canWrite) writeCount++
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            val endTime = System.currentTimeMillis()
            println("endTime=$endTime")
            println("Time consumed=${endTime - startTime}, fileCount=$fileCount, readCount=$readCount, writeCount=$writeCount")
        }
    }

    /**
     * 遍历文件
     */
    private fun File.eachFilesRecurse(
        fileType: FileType = FileType.ANY,
        closure: ((File) -> Unit)?
    ) {
        val files = this.listFiles()
        if (files != null) {
            val fileSize = files.size
            for (i in 0 until fileSize) {
                val file = files[i]
                if (file.isDirectory) {
                    if (fileType != FileType.FILES) {
                        closure?.invoke(file)
                    }
                    file.eachFilesRecurse(fileType, closure)
                } else if (fileType != FileType.DIRECTORIES) {
                    closure?.invoke(file)
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


    private fun innerRunCleanAction(
        task: TaskProvider<Task>, buildType: String, flavor: String? = null) {
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


    open class CleanDuplicateAction {
        lateinit var job: CleanDuplicateJarJob
        var flavor: String? = null
        lateinit var buildType: String

        fun clean() {
            val destDir = File(job.appProject.buildDir.absolutePath, TRANSFORMS)
            if (destDir.exists()) {
                destDir.deleteRecursively()
//                val allThirdPg = destDir.listFiles()
//                allThirdPg.forEach {
//                    val jarDir = File(it.absolutePath,
//                        if (flavor != null) flavor + File.separator + buildType else buildType)
//                    if (jarDir.exists()) {
//                        val contentFile = File(jarDir.absolutePath, SubStream.FN_FOLDER_CONTENT)
//                        if (contentFile.exists()) {
//                            //开始清理
//                            cleanJarByContentJson(jarDir)
//                        }
//                    }
//                }
            }
        }


        fun cleanJarByContentJson(jarDir: File) {
            val subStreams = SubStream.loadSubStreams(jarDir)
            if (subStreams == null) return
            val iterator = subStreams.iterator()
            //记录所有的jar
            val duplicateMap = mutableMapOf<String, String>()
            //记录需要移除的 jar，N 个相同，移除前面的 n-1 个，保留最后一个
            val needToRemoveSet = mutableSetOf<String>()
            //移除变成 aar的 project
            val needToRemoveProject = hasChangetoAar()
            while (iterator.hasNext()) {
                val subStream = iterator.next()
                val scope = subStream.scopes?.toList()?.get(0) as? QualifiedContent.Scope
                val index = subStream.name.indexOf("_")
                var moduleName: String? = null
                if (index > 0) {
                    moduleName = subStream.name.substring(0, index)
                    if (moduleName.endsWith(":")) {
                        moduleName = moduleName.substring(0, moduleName.length - 1)
                    }
                    if (duplicateMap.contains(moduleName)) {
                        //记录重复，缓存剔除
                        duplicateMap.get(moduleName)?.run {
                            needToRemoveSet.add(this)
                        }
                    }
                    duplicateMap.put(moduleName, subStream.name)

                    // project 变成 aar，project 的缓存剔除
                    if (needToRemoveProject.contains(moduleName)
                        && (scope == QualifiedContent.Scope.SUB_PROJECTS || scope == QualifiedContent.Scope.PROJECT)) {
                        needToRemoveSet.add(subStream.name)
                    }
                    moduleName?.let {
                        // aar 变成 project，aar 的缓存剔除
                        if (moduleIsChange(it) && scope == QualifiedContent.Scope.EXTERNAL_LIBRARIES) {
                            needToRemoveSet.add(subStream.name)
                        }
                    }
                }
            }

            subStreams.removeAll {
                if (needToRemoveSet.contains(it.name)) {
                    File(jarDir.absolutePath, it.filename).delete()
                    true
                } else {
                    false
                }
            }


            SubStream.save(subStreams, jarDir)
        }


        //模块是否改动,从aar 变成 project
        fun moduleIsChange(name: String): Boolean {
            var ret = job.mAllChangedProject?.get(name) != null
            return ret
        }

        //从 project 变成aar
        fun hasChangetoAar(): MutableSet<String> {
            val hasChangeList = mutableSetOf<String>()
            job.mLastChangeProject?.forEach { last ->
                if (job.mAllChangedProject?.keys?.contains(last)?.not() ?: false) {
                    hasChangeList.add(last)
                }
            }
            return hasChangeList
        }
    }


}