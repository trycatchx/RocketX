package plugin.utils

import org.gradle.api.Project
import java.io.File
import java.io.FilenameFilter

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/1
 * copyright TCL+
 */


internal fun findFirstAarPath(project: Project): String? {
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

