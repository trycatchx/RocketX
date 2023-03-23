package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import plugin.utils.DependenciesHelper

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class ChildProjectDependencies(
    var project: Project,
    var android: AppExtension,
    var mAllChangedProject: MutableMap<String, Project>?) {

    private var ALL_SUFFIX = arrayOf("implementation", "api", "compileOnly")
    var allConfigList = arrayListOf<Configuration>()

    init {
        //生成所有的 config ，project 的所有依赖分散到 各个 config 中去
        ALL_SUFFIX.forEach {
            val configuration = project.configurations.maybeCreate(it)
            allConfigList.add(configuration)
        }

        android.applicationVariants.forEach {
            ALL_SUFFIX.forEach { suffix ->
                //组合的 Config
                val fullConfigName =
                    it.flavorName + it.buildType.name.capitalize() + suffix.capitalize()
                val fullConfiguration = project.configurations.maybeCreate(fullConfigName)
                if (!allConfigList.contains(fullConfiguration)) {
                    allConfigList.add(fullConfiguration)
                }

                //单独的 flavorConfig
                val flavorConfigName = it.flavorName + suffix.capitalize()
                val flavorConfiguration = project.configurations.maybeCreate(flavorConfigName)
                if (!allConfigList.contains(flavorConfiguration)) {
                    allConfigList.add(flavorConfiguration)
                }

                //单独的 buildconfig
                val buildTypeConfigName = it.buildType.name + suffix.capitalize()
                val buildTypeConfiguration = project.configurations.maybeCreate(buildTypeConfigName)
                if (!allConfigList.contains(buildTypeConfiguration)) {
                    allConfigList.add(buildTypeConfiguration)
                }
            }
        }

    }

    // 开始处理依赖关系
    open fun doDependencies(dependenciesHelper: DependenciesHelper) {
        //当前的 project 是否为改变的
        val isCurProjectChanged: Boolean =
            mAllChangedProject?.get(project.path) != null
        //如果当前project 没有做改动，需要把自身变成 aar 给到 parent project
        if (!isCurProjectChanged) {
            dependenciesHelper.modifyDependencies(this)
        }
    }

}