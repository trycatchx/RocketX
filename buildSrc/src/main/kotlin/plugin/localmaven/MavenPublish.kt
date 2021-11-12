package plugin.localmaven

import MAVEN_LOCAL
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.AntBuilder.AntMessagePriority.from
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import plugin.bean.RocketXBean
import plugin.utils.FileUtil
import plugin.utils.hasAndroidPlugin
import plugin.utils.hasAppPlugin
import plugin.utils.hasJavaPlugin


/**
 * maven
 *
 * Created by lzy on 2021/11/4.
 */

const val MAVEN_JAR = "jar"
const val MAVEN_AAR = "aar"

/**
 * 便捷的发布到 maven 仓库
 *
 */
fun Project.mavenPublish(mRocketXBean: RocketXBean?) {
    // 获取 module 中定义的发布信息
    val pomDesc = "library is ${this.name}"
    val pomGroupId = "com.${this.name}"
    val pomAftId = this.name as String
    val pomVersion = "1.0"
    mavenPublish(mRocketXBean, pomGroupId, pomAftId, pomVersion, pomDesc) {
        it.maven { artifactRepository ->
            artifactRepository.name = "local"
            artifactRepository.url = MAVEN_LOCAL
        }
    }
}

/**
 * 便捷的发布到 maven 仓库
 *
 * - [gradle-developers](https://docs.gradle.org/current/userguide/publishing_maven.html)
 * - [android-developers](https://developer.android.google.cn/studio/build/maven-publish-plugin#groovy)
 *
 * @param groupId Sets the groupId for this publication.
 * @param version Sets the version for this publication.
 * @param desc The description for the publication represented by this POM.
 * @param artifactId Sets the artifactId for this publication.
 * @param packaging Sets the packaging for the publication represented by this POM.
 * @param repository 配置 maven 仓库
 */
fun Project.mavenPublish(
    mRocketXBean: RocketXBean?,
    groupId: String,
    artifactId: String,
    version: String,
    desc: String,
    repository: Action<RepositoryHandler>
) = gradle.projectsEvaluated {
    if (mRocketXBean?.localMaven != true) return@projectsEvaluated
    // 判断 module 类型
    val isJava = hasJavaPlugin(project)
    val isAndroidApp = hasAppPlugin(project)
    val isAndroidLib = hasAndroidPlugin(project)
    if (!isAndroidApp && !isJava && !isAndroidLib) {
        // 非这三类的module不让走下面的逻辑
        return@projectsEvaluated
    }

    val isAndroidProject = isAndroidApp || isAndroidLib
    // 添加发布需要的 plugin
    pluginManager.apply("maven-publish")
    //pluginManager.apply("signing")

    // 添加打包源码的任务，这样方便查看 lib 的源码

    @Suppress("UnstableApiUsage")
    if (!isAndroidProject) {
        try {
            extensions.getByType(JavaPluginExtension::class.java).apply {
                withJavadocJar()
                withSourcesJar()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    else {
        tasks.register("androidSourcesJar", Jar::class.java) { jar ->
            jar.archiveClassifier.set("sources")
            jar.from(project.extensions.getByType(LibraryExtension::class.java).sourceSets.getByName("main").java.srcDirs)
        }
    }
    // 配置 maven 发布任务
    extensions.configure<PublishingExtension>("publishing") { publishingExt ->
        publishingExt.publications { publicationContainer ->

            if (isAndroidProject) {
                publicationContainer.create("mavenDebug", MavenPublication::class.java) { maven ->
                    createMavenPom(
                        maven,
                        groupId,
                        artifactId,
                        version,
                        isAndroidProject,
                        desc,
                        true
                    )
                }
                publicationContainer.create("mavenRelease", MavenPublication::class.java) { maven ->
                    createMavenPom(
                        maven,
                        groupId,
                        artifactId,
                        version,
                        isAndroidProject,
                        desc,
                        false
                    )
                }
            } else {
                publicationContainer.create("mavenJava", MavenPublication::class.java) { maven ->
                    createMavenPom(
                        maven,
                        groupId,
                        artifactId,
                        version,
                        isAndroidProject,
                        desc,
                        false
                    )
                }
            }
        }
        publishingExt.repositories(repository)
    }
//    if (JavaVersion.current().isJava9Compatible) tasks.named("javadoc", Javadoc::class.java) { javaDoc ->
//        (javaDoc.options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
//    }
}

private fun Project.createMavenPom(
    maven: MavenPublication,
    groupId: String,
    artifactId: String,
    version: String,
    isAndroidProject: Boolean,
    desc: String,
    isDebug: Boolean
) {
    maven.groupId = groupId
    maven.artifactId = artifactId
    maven.version = version
    if (isAndroidProject) {
        if (isDebug) {
            //"$buildDir/outputs/aar/${project.name}-debug.aar"
            //maven.artifact("$buildDir/outputs/aar/${project.name}-debug.aar")
            maven.from(project.components.getByName("debug"))
        } else {
            //maven.artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            maven.from(project.components.getByName("release"))
        }
    } else {
        maven.from(project.components.getByName("java"))
    }
    // android 的源码打包
    if (isAndroidProject) maven.artifact(LazyPublishArtifact(tasks.named("androidSourcesJar")))
    maven.pom { pom ->
        pom.name.set(artifactId)
        pom.description.set(desc)
        pom.packaging = if (isAndroidProject) MAVEN_AAR else MAVEN_JAR
        pom.url.set(MAVEN_LOCAL.path)
//                    pom.licenses { spec ->
//                        spec.license { license ->
//                            license.name.set(POM_LICENCE_NAME)
//                            license.url.set(POM_LICENCE_URL)
//                        }
//                    }
//                    pom.developers { spec ->
//                        spec.developer { dev ->
//                            dev.id.set(POM_DEVELOPER_ID)
//                            dev.name.set(POM_DEVELOPER_NAME)
//                            dev.email.set(POM_DEVELOPER_EMAIL)
//                        }
//                    }
    }
}