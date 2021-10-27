package plugin.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.internal.impldep.bsh.commands.dir
import plugin.ChildProjectDependencies

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/25
 * copyright TCL+
 */
class DependenciesHelper(var mProjectDependenciesList: MutableList<ChildProjectDependencies>) {

    //获取第一层 parent 依赖当前 project
    fun getFirstLevelParentDependencies(project: Project): MutableMap<Project, MutableList<Configuration>> {
        var parentProjectList = mutableMapOf<Project, MutableList<Configuration>>()
        mProjectDependenciesList.forEach {
            var parentProject = it.project
            //子project 所有的 config
            it.allConfigList.forEach { config ->
                //每一个config 所有依赖
                run loop@{
                    config.dependencies.forEach { dependency ->
                        //项目依赖
                        if (dependency is DefaultProjectDependency && dependency.name.equals(project.name)) {
                            parentProjectList.get(parentProject)?.apply {
                                this.add(config)
                            } ?: let {
                                val configList = mutableListOf<Configuration>()
                                configList.add(config)
                                parentProjectList.put(parentProject, configList)
                            }
                            //每一个 config 对同个 project 重复依赖是无意义，可直接 return
                            return@loop
                        }
                    }
                }
            }
        }
        return parentProjectList
    }

    /**
     * 解决各个 project 变动之后需要打成 aar 包，算法V1
     */
    fun modifyDependencies(projectWapper: ChildProjectDependencies) {
        //找到所有的父依赖
        val map = getFirstLevelParentDependencies(projectWapper.project)
        //可能有多个父依赖，所以需要遍历
        map.forEach { parentProject ->
            //父依赖的 configuration 添加 当前的 project 对应的aar
            parentProject.value.forEach { parentConfig ->
                // 剔除原有的依赖
                parentConfig.dependencies.removeAll{ dependency->
                    dependency is DefaultProjectDependency && dependency.name.equals(projectWapper.project.name)
                }
                //添加 aar 依赖
                parentProject.key.dependencies.add(parentConfig.name,
                    projectWapper.project.rootProject.files("/.rocketxcache/lib-aar-local.aar"))
                println("Testst:"+ projectWapper.project.rootProject.files("/.rocketxcache/lib-aar-local.aar"))


                // 把子 project 自身的依赖全部 给到 父 project
                projectWapper.allConfigList.forEach { childConfig ->
                    childConfig.dependencies.forEach { childDepency ->
                        if (childDepency is DefaultProjectDependency) {
                            if (childDepency.targetConfiguration == null) {
                                childDepency.targetConfiguration = "default"
                            }
                            // Android Studio 4.0.0 索引
                            val dependencyClone = childDepency.copy()
                            dependencyClone.targetConfiguration = null
                            // parent 铁定有 childConfig.name 的 config
                            parentProject.key.dependencies.add(childConfig.name, dependencyClone)
                        } else {
                            parentProject.key.dependencies.add(childConfig.name, childDepency)
                        }
                    }
                }
            }
        }
    }

    /**
     * 解决各个 project 变动之后需要打成 aar 包,算法 V2
     */
    fun modifyDependenciesV2(projectWapper: ChildProjectDependencies) {
        //todo

    }


}