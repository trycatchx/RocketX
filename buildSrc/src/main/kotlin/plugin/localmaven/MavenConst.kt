import org.gradle.api.Project
import java.io.File
import java.net.URI

/**
 * Maven 发布用的常量
 */

val Project.MAVEN_LOCAL: URI
    get() = File(rootDir, "repos").toURI()

fun Project.getMavenGroupId(): String {
    return "com.${this.path.removePrefix(":").replace(":", ".")}"
}

fun Project.getMavenArtifactId(): String {
    return this.name
}

const val MAVEN_LOCAL_NAME: String = "rocketXLocalMaven"

const val POM_LICENCE_NAME = "Apache-2.0 License"
const val POM_LICENCE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"

