package plugin.utils

import com.google.gson.Gson
import org.gradle.api.Project
import plugin.bean.ModuleChangeTime
import plugin.bean.ModuleChangeTimeList
import plugin.utils.FileUtil.eachFileRecurse
import plugin.utils.FileUtil.writeFileToModuleJson
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/5
 * copyright TCL+
 *
 *  module 变动计算
 */
object ChangeModuleUtils {
    //Gradle 静态变量会被保留
    private val newModuleList: MutableList<ModuleChangeTime> = mutableListOf()

    /**
     * 获取发生变动的module信息
     */
    fun getChangeModuleMap(project: Project): MutableMap<String, Project>? {
        val changeMap: MutableMap<String, Project> = mutableMapOf()
        val startTime = System.currentTimeMillis()

        getNewModuleList(project)

        val localModuleList = FileUtil.getLocalModuleChange()

        localModuleList?.let { localFile ->
            try {
                val oldModuleList = Gson().fromJson(localFile.readText(), ModuleChangeTimeList::class.java)
                // 返回null, 代表之前没有编译过，要重新编译
                if (oldModuleList.list.isNullOrEmpty()) {
                    return null
                } else {
                    newModuleList.forEach { newModule ->
                        oldModuleList.list.firstOrNull { newModule.moduleName == it.moduleName }.also { moduleChange ->
                            // 为null, 代表这个module是新创建的
                            if (moduleChange == null) {
                                project.rootProject.allprojects.firstOrNull { pt ->
                                    pt?.path == newModule.moduleName
                                }?.let {
                                    changeMap[newModule.moduleName] = it
                                    LogUtil.d(" 你添加了=${newModule.moduleName}        ")
                                }
                            }
                            // 已有的module 文件发生改变
                            else if (moduleChange.changeTag != newModule.changeTag) {
                                changeMap[newModule.moduleName] = project.rootProject.allprojects.first { it?.path == newModule.moduleName }
                                LogUtil.d(" 你修改了=${newModule.moduleName}      ")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        } ?: run {
            allProjectsChange(project,changeMap)
        }

        //最后补一个 app 的 module，app 是认为做了改变，不打成 aar
        changeMap[project.path] = project
        LogUtil.d("count time====>>>> ${System.currentTimeMillis() - startTime}ms   "+changeMap.toString())
        return changeMap
    }

    /**
     * 如果没有这个文件的话，认为整个模块都做了改动
     */
    private fun allProjectsChange(project: Project,changeMap: MutableMap<String, Project>) {
        project.rootProject.allprojects.filter { it != project.rootProject && it.childProjects.isEmpty() }.forEach {
            changeMap[it.path] = it
        }
    }

    /**
     *  获取当前module和文件时间戳
     */
    private fun getNewModuleList(project: Project) {
        newModuleList.clear()
        project.rootProject.allprojects.onEach {
            if (it == project.rootProject || it.childProjects.isNotEmpty()) {
                return@onEach
            }
            var countTime = 0L
            it.projectDir.eachFileRecurse { file ->
                // 过滤掉build目录及该目录下的所有文件
                if (!(file.isDirectory && Contants.BUILD == file.name) && !file.absolutePath.contains("build/")) {
                    countTime += file.lastModified()
                }
            }
            newModuleList.add(ModuleChangeTime(it.path, countTime))
        }
    }


    fun flushJsonFile() {
        val dir = File(FileUtil.getLocalMavenCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, Contants.MODULE_CHANGE_TIME)
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
        }
        jsonFile.writeFileToModuleJson(newModuleList)
    }

}



