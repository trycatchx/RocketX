package plugin.utils

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact
import plugin.ChildProjectDependencies
import java.io.File

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
        //找到当前所有通过 artifacts.add("default", file('xxx.aar')) 依赖进来的 aar,并构建local mave
        val artifactAarList = getAarByArtifacts(projectWapper.project)
        //可能有多个父依赖，所以需要遍历
        map.forEach { parentProject ->

            artifactAarList.forEach {
                addAarDependencyToProject(it,parentProject.key.configurations.maybeCreate("api").name,parentProject.key)
            }

            //父依赖的 configuration 添加 当前的 project 对应的aar
            parentProject.value.forEach { parentConfig ->
                // 剔除原有的依赖
                parentConfig.dependencies.removeAll { dependency ->
                    dependency is DefaultProjectDependency && dependency.name.equals(projectWapper.project.name)
                }

                addAarDependencyToProject(projectWapper.project.name,parentConfig.name,parentProject.key)

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


    fun addAarDependencyToProject(aarName:String, configName:String,project:Project) {
        //添加 aar 依赖 以下代码等同于 api/implementation/xxx (name: 'libaccount-2.0.0', ext: 'aar'),源码使用 linkedMap
        val map = linkedMapOf<String, String>()
        map.put("name", aarName)
        map.put("ext", "aar")
        project.dependencies.add(configName, map)
    }


    fun getAarByArtifacts(childProject: Project):MutableList<String> {
        //找到当前所有通过 artifacts.add("default", file('xxx.aar')) 依赖进来的 aar
        var listArtifact = mutableListOf<DefaultPublishArtifact>()
        var aarList = mutableListOf<String>()
        childProject.configurations.maybeCreate("default").artifacts?.forEach {
            if(it is DefaultPublishArtifact && "aar".equals(it.type)) {
                listArtifact.add(it)
            }
        }

        //拷贝一份到 localMaven
        listArtifact.forEach {
            it.file.copyTo(File(FileUtil.getLocalMavenCacheDir(),  it.file.name), true)
            //剔除后缀 （.aar）
            aarList.add(FilenameUtils.removeExtension(it.file.name))
        }

        return aarList
    }


    /**
     * 解决各个 project 变动之后需要打成 aar 包,算法 V2
     */
    fun modifyDependenciesV2(projectWapper: ChildProjectDependencies) {
        //todo

    }


}