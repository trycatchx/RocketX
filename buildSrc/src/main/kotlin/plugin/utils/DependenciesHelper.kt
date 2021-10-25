package plugin.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import plugin.ChildProjectDependencies

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/25
 * copyright TCL+
 */
class DependenciesHelper(var mProjectDependenciesList: MutableList<ChildProjectDependencies>) {

    //获取第一层 parent 依赖当前 project
    fun getFirstLevelParentDependencies(project: Project): HashMap<Project, MutableList<Configuration>> {
        var parentProjectList = hashMapOf<Project, MutableList<Configuration>>()
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

}