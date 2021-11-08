package plugin.localmaven

import org.gradle.api.Project
import plugin.utils.FileUtil
import java.io.File

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/3
 * copyright TCL+
 */
class JarFlatLocalMaven(
    var childProject: Project,
    var appProject: Project,
    var allChangedProject: MutableMap<String, Project>? = null) : LocalMaven() {

    companion object {
        const val JAR = "jar"
    }

    override fun uploadLocalMaven() {
        var localMavenTask = childProject.task("uploadLocalMaven") {
            it.doLast {
                var inputPath = FileUtil.findFirstLevelJarPath(childProject)
                var outputFile = File(FileUtil.getLocalMavenCacheDir(), childProject.name + ".jar")

                inputPath?.let {
                    if (outputFile.exists()) {
                        outputFile.delete()
                    }
                    File(it).copyTo(outputFile, true)
                    putIntoLocalMaven(childProject.name,childProject.name + ".jar")
                }
            }
        }

        // publish local maven
        try {
            val publishTask = childProject.project.tasks.named("publishMavenJavaPublicationToLocalRepository").orNull
            childProject.tasks.findByPath(JAR)?.let { task ->
                if (publishTask != null) {
                    task.finalizedBy(localMavenTask, publishTask)
                } else {
                    task.finalizedBy(localMavenTask)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


}