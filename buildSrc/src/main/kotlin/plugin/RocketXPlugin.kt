package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.*
import plugin.bean.RocketXBean
import plugin.listener.RocketXBuildListener
import plugin.utils.*
import java.util.*


/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 *
 *
 * mac debug 插件命令 ：export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 * window debug 插件命令 ：set GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 */
open class RocketXPlugin : Plugin<Project> {

    companion object {
        const val ASSEMBLE = "assemble"
    }

    lateinit var mProject: Project
    lateinit var mAppExtension: AppExtension
    lateinit var mRocketXBean: RocketXBean
    lateinit var mAppProjectDependencies: AppProjectDependencies

    private val mAllChangedProject by lazy {
        ChangeModuleUtils.getChangeModuleMap(mProject)
    }

    private val mFlavorBuildType by lazy {
        getFlavorBuildType(mProject)
    }

    override fun apply(project: Project) {
        //应用在 主 project 上，也就是 app module
        mRocketXBean = project.extensions.create("RocketX", RocketXBean::class.java)

//        if (!isEnable(project) || hasAndroidPlugin(project) || !isCurProjectRun(project)){
//            return
//        }

        this.mProject = project

        //禁止 release 使用加速插件
        if (mFlavorBuildType.toLowerCase(Locale.ROOT).contains("release")) {
            return
        }

        this.mAppExtension = project.extensions.getByType(AppExtension::class.java)

        FileUtil.attach(project)

        flatDirs(mProject)

        openSpeedBuildByOption(mProject, mAppExtension)

        mProject.gradle.addBuildListener(RocketXBuildListener(this, mRocketXBean, mProject, mAllChangedProject))

        // 这个类
        mAppProjectDependencies = AppProjectDependencies(project, mAppExtension, mRocketXBean, mAllChangedProject) {
            LogUtil.pritlnDependencyGraph(mAppProjectDependencies)
        }

    }
}