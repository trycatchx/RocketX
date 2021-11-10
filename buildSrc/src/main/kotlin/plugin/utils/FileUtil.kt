package plugin.utils

import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.ivy.util.FileUtil
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
            return if (files.size > 0) files[0].absolutePath else null
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
            return if (files.size > 0) files[0].absolutePath else null
        }
        return null
    }


    internal fun getLocalMavenCacheDir(): String {
        return sProject.rootProject.rootDir.absolutePath + File.separator + ".gradle" + File.separator + ".rocketxcache" + File.separator
    }
}



