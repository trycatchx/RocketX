package plugin.utils

import com.google.gson.Gson
import groovy.io.FileType
import org.gradle.api.Project
import java.io.File

//获取发生变动的module信息
internal fun getChangeModuleMap(rootProject: Project): MutableMap<String, Project>? {
    var hasChangeMap: MutableMap<String, Project>? = null
    val startTime = System.currentTimeMillis()
    var sum = 0
    var moduleCount = 0
    val newModuleList = ArrayList<ModuleChangeTime>()
    rootProject.allprojects.onEach { child ->
        if (child.name == rootProject.name) return@onEach
        moduleCount++
        println("module name==>$child.name")
        var countTime = 0L
        child.projectDir.eachFileRecurse { file ->
            // 过滤掉build目录及该目录下的所有文件
            if (!(file.isDirectory && "build" == file.name) && !file.absolutePath.contains("build/")) {
                println("file name====>>>>$child.name==>> $file.name")
                countTime += file.lastModified()
                sum++
            }
        }
        newModuleList.add(ModuleChangeTime(child.name, countTime))
        println("module name==>$child.name; countTime=$countTime")
    }

    println("moduleCount====>>>> $moduleCount")
    println("sum====>>>> $sum")

    val jsonFile = File(rootProject.projectDir.absolutePath + "/moduleChangeTime.json")
    if (jsonFile.exists()) {
        println("File exists.")
        try {
            val oldModuleChangeTimeList =
                Gson().fromJson(jsonFile.readText(), ModuleChangeTimeList::class.java)
            hasChangeMap = HashMap<String, Project>()
            if (oldModuleChangeTimeList?.list.isNullOrEmpty()) {
                // 返回null, 代表之前没有编译过，要重新编译
                return null
            } else {
                newModuleList.onEach { newModule ->
                    oldModuleChangeTimeList?.list?.firstOrNull { oldModule ->
                        newModule.moduleName == oldModule.moduleName
                    }.also { moduleChange ->
                        if (moduleChange == null) {
                            // 为null, 代表这个module是新创建的
                            rootProject.allprojects.firstOrNull { pt ->
                                pt?.name == newModule.moduleName
                            }?.let { pro ->
                                hasChangeMap[newModule.moduleName] = pro
                            }
                        } else if (moduleChange.changeTag != newModule.changeTag) {
                            // 已有的module 文件发生改变
                            hasChangeMap[newModule.moduleName] =
                                rootProject.allprojects.first { pt ->
                                    pt?.name == newModule.moduleName
                                }
                        }
                    }
                }
            }
            println("hasChangeMap $hasChangeMap")
            jsonFile.writeFileToModuleJson(newModuleList)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    } else {
        println("File not exists.")
        jsonFile.createNewFile()
        jsonFile.writeFileToModuleJson(newModuleList)
    }
    println("count time====>>>> ${System.currentTimeMillis() - startTime}")
    return hasChangeMap
}

/**
 * 文件便利
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
}