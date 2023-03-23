package plugin.utils

import com.google.gson.Gson
import org.gradle.api.Project
import plugin.bean.ModuleChangeTime
import plugin.bean.ModuleChangeTimeList
import java.io.File
import java.io.FilenameFilter

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/1
 * copyright TCL+
 */
object FileUtil {
    lateinit var sProject: Project

    internal fun attach(p: Project) {
        sProject = p
    }

    internal fun findFirstLevelAarPath(project: Project): String? {
        val dir = File(project.buildDir.absolutePath + "/outputs/aar/")
        if (dir.exists()) {
            val files = dir.listFiles(object : FilenameFilter {
                override fun accept(dir: File?, name: String?): Boolean {
                    return name?.endsWith(".aar") ?: false
                }
            })
            return if (!files.isNullOrEmpty()) files[0].absolutePath else null
        }
        return null
    }

    internal fun findFirstLevelJarPath(project: Project): String? {
        val dir = File(project.buildDir.absolutePath + "/libs/")
        if (dir.exists()) {
            val files = dir.listFiles(object : FilenameFilter {
                override fun accept(dir: File?, name: String?): Boolean {
                    return name?.endsWith(".jar") ?: false
                }
            })
            return if (!files.isNullOrEmpty()) files[0].absolutePath else null
        }
        return null
    }


    internal fun getLocalMavenCacheDir(): String {
        val appFolder = "." + getFlatAarName(sProject)
        return sProject.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + ".rocketxcache" + File.separator + appFolder + File.separator
    }

    internal fun getApkLocalPath(): String {
        var filepath = ""
        File(sProject.buildDir.absolutePath + File.separator).walkTopDown().forEach {
            if (it.absolutePath.endsWith(".apk")) {
                filepath = it.absolutePath
                return@forEach
            }
        }
        return filepath
    }

    /**
     * 将有变动的module信息写入文件
     */
    fun File.writeFileToModuleJson(moduleChangeList: MutableList<ModuleChangeTime>) {
        val newJsonTxt = Gson().toJson(ModuleChangeTimeList(moduleChangeList))
        this.writeText(newJsonTxt)
        LogUtil.d("writeFileToModuleJson success!")
    }

    /**
     * 获取已经存储的module
     */
    fun getLocalModuleChange(): File? {
        val dir = File(getLocalMavenCacheDir())
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val jsonFile = File(dir, Contants.MODULE_CHANGE_TIME)
        return if (jsonFile.exists()) {
            jsonFile
        } else {
            null
        }
    }


    /**
     * 文件遍历
     */
    fun File.eachFileRecurse(closure: ((File) -> Boolean)?) {
        listFiles()?.let {
            for(file in it) {
                if (file.isDirectory) {
                    val continueRecursion = closure?.invoke(file) ?: true
                    if (continueRecursion) {
                        file.eachFileRecurse(closure)
                    }
                } else {
                    closure?.invoke(file)
                }
            }
        }
    }

}



