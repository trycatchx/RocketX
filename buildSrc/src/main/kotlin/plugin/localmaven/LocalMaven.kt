package plugin.localmaven

import org.gradle.api.Project

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/11/3
 * copyright TCL+
 */
abstract class LocalMaven {

    abstract fun uploadLocalMaven()

    companion object {
        // project name 对应 flat aar/jar name
        val localMavenMap = mutableMapOf<String, String>()
    }

    fun putIntoLocalMaven(projectName: String, path: String) {
        localMavenMap.put(projectName, path)
    }



}