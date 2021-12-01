package plugin.utils

import org.gradle.api.Project

/**
 * @author QuincyJiang
 * Created at 2021/12/1.
 */
object MavenIdHelper {
    fun Project.getMavenGroupId(): String {
        return "com." + this.path.removePrefix(":").replace(":", ".")
    }

    fun Project.getMavenArtifactId(): String {
        return project.name
    }
}