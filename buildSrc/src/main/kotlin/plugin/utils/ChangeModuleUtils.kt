package plugin.utils

import com.google.gson.Gson
import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.internal.impldep.bsh.commands.dir
import plugin.bean.ModuleChangeTime
import plugin.bean.ModuleChangeTimeList
import java.io.File

object ChangeModuleUtils {

    var newModuleList: ArrayList<ModuleChangeTime>? = null
    lateinit var rootProject: Project

    /**
     * 获取发生变动的module信息
     *
     */
    fun getChangeModuleMap(appProject: Project): MutableMap<String, Project>? {
        var hasChangeMap: MutableMap<String, Project>? = null
        val startTime = System.currentTimeMillis()
        var sum = 0
        var moduleCount = 0
        newModuleList = ArrayList<ModuleChangeTime>()
        rootProject = appProject.rootProject
        rootProject.allprojects.onEach { child ->
            if (child == rootProject || child.childProjects.size > 0) return@onEach
            moduleCount++
            var countTime = 0L
            child.projectDir.eachFileRecurse { file ->
                // 过滤掉build目录及该目录下的所有文件
                if (!(file.isDirectory && "build" == file.name) && !file.absolutePath.contains("build/")) {
                    countTime += file.lastModified()
                    sum++
                }
            }
            newModuleList?.add(ModuleChangeTime(child.path, countTime))
            println("module name==>${child.path}; countTime=$countTime")
        }

        val dir = File(FileUtil.getLocalMavenCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, "moduleChangeTime.json")
        if (jsonFile.exists()) {
            try {
                val oldModuleChangeTimeList =
                    Gson().fromJson(jsonFile.readText(), ModuleChangeTimeList::class.java)
                hasChangeMap = mutableMapOf<String, Project>()
                if (oldModuleChangeTimeList?.list.isNullOrEmpty()) {
                    // 返回null, 代表之前没有编译过，要重新编译
                    return null
                } else {
                    newModuleList?.onEach { newModule ->
                        oldModuleChangeTimeList?.list?.firstOrNull { oldModule ->
                            newModule.moduleName == oldModule.moduleName
                        }.also { moduleChange ->
                            if (moduleChange == null) {
                                // 为null, 代表这个module是新创建的
                                rootProject.allprojects.firstOrNull { pt ->
                                    pt?.path == newModule.moduleName
                                }?.let { pro ->
                                    hasChangeMap!![newModule.moduleName] = pro
                                }
                            } else if (moduleChange.changeTag != newModule.changeTag) {
                                // 已有的module 文件发生改变
                                hasChangeMap!![newModule.moduleName] =
                                    rootProject.allprojects.first { pt ->
                                        pt?.path == newModule.moduleName
                                    }
                            }
                        }
                    }
                }
                println("hasChangeMap $hasChangeMap")
//                newModuleList?.let {
//                    jsonFile.writeFileToModuleJson(it)
//                }

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        } else {
//            newModuleList?.let {
//                jsonFile.writeFileToModuleJson(it)
//            }
            //如果没有这个文件的话，认为整个模块都做了改动
            hasChangeMap = mutableMapOf<String, Project>()
            rootProject.allprojects.onEach { it ->
                if (it != rootProject && it.childProjects.size <= 0) {
                    hasChangeMap.put(it.path, it)
                }
            }
        }

        //最后补一个 app 的 module，app 是认为做了改变，不打成 aar
        hasChangeMap?.put(appProject.path, appProject)
        println("count time====>>>> ${System.currentTimeMillis() - startTime}")
        return hasChangeMap
    }


    fun flushJsonFile() {
        val dir = File(FileUtil.getLocalMavenCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, "moduleChangeTime.json")
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
        }
        newModuleList?.let {
            jsonFile.writeFileToModuleJson(it)
        }
    }


    /**
     * 文件遍历
     */
    private fun File.eachFileRecurse(
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
                    file.eachFileRecurse(fileType, closure)
                } else if (fileType != FileType.DIRECTORIES) {
                    closure?.invoke(file)
                }
            }
        }
    }

    /**
     * 将有变动的module信息写入文件
     */
    private fun File.writeFileToModuleJson(moduleChangeList: MutableList<ModuleChangeTime>) {
        val newJsonTxt = Gson().toJson(ModuleChangeTimeList(moduleChangeList))
        this.writeText(newJsonTxt)
        println("writeFileToModuleJson success!")
    }

}



