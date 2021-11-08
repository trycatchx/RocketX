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

    var ALL_SUFFIX = arrayOf("implementation", "api", "compileOnly")
    var allConfigList = arrayListOf<Configuration>()

    init {
        //生成所有的 config ，project 的所有依赖分散到 各个 config 中去
        ALL_SUFFIX.forEach {
            val configuration = project.configurations.maybeCreate(it)
            allConfigList.add(configuration)
        }

        android.buildTypes.all { buildType ->
            ALL_SUFFIX.forEach {
                val configName = buildType.name + it.capitalize()
                val configuration = project.configurations.maybeCreate(configName)
                allConfigList.add(configuration)
            }
        }

        android.productFlavors.all { flavor ->
            ALL_SUFFIX.forEach {
                val configName = flavor.name + it.capitalize()
                val configuration = project.configurations.maybeCreate(configName)
                allConfigList.add(configuration)
            }

            android.buildTypes.all { buildType ->
                ALL_SUFFIX.forEach {
                    val variantName = flavor.name + buildType.name.capitalize()
                    val variantConfigName = variantName + it.capitalize()
                    val variantConfiguration = project.configurations.maybeCreate(variantConfigName)
                    allConfigList.add(variantConfiguration)
                }
            }
        }
    }

    // 开始处理依赖关系
    open fun doDependencies(dependenciesHelper: DependenciesHelper) {
        //当前的 project 是否为改变的
        var isCurProjectChanged: Boolean =
            if (mAllChangedProject?.get(project.path) != null) true else false
        //如果当前project 没有做改动，需要把自身变成 aar 给到 parent project
        if (!isCurProjectChanged) {
            dependenciesHelper.modifyDependencies(this)
        }
    }

}