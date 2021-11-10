package plugin.localmaven

import org.gradle.api.Project
import plugin.RocketXPlugin
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
    var rocketXPlugin: RocketXPlugin,
    var allChangedProject: MutableMap<String, Project>? = null) : LocalMaven() {

    companion object {
        const val JAR = "jar"
    }

    val enableLocalMaven by lazy {
        rocketXPlugin.mRocketXBean?.localMaven ?:false
    }

    override fun uploadLocalMaven() {
        if(enableLocalMaven) {
            // publish local maven
            try {
                val publishTask = childProject.project.tasks.named("publishMavenJavaPublicationToLocalRepository").orNull
                childProject.tasks.findByPath(JAR)?.let { task ->
                    if (publishTask != null) {
                        task.finalizedBy(publishTask)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        } else {
            //通过 flat copy 到cache 目录
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

            childProject.tasks.findByPath(JAR)?.let { task ->
                 task.finalizedBy(localMavenTask)
            }
        }



    }


}