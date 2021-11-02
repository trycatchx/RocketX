# RocketXPlugin

## 描述
本插件旨在加速 Android 项目的全量编译速度，并开源。


## 思路分析
1. 通过 gradle plugin 的形式动态修改 implement project，为 implement aar 加快编译速度
2. 构建 local maven 存储未被修改的 project 对应的 aar
3. 未被修改的 module 通过某个接口可以得知，如果不行置换成遍历文件的 lastmodify time 去做判断
4. 要递归遍历每一个 module 的依赖关系进行置换
5. 每次加速编译完成后，需要置换 loacal maven 中被修改的 aar
6. 编译完成之后，需要通 commend line 写入 adb 命令 install 和 launch apk



## 难点和疑问攻关
1.依赖关系哪里可以获取并修改?

已预研部分答案:  
configuration.dependencies.each { dependency -> xx} 可以获取当前 module 的依赖关系图，
 project.dependencies.add(compileOnlyConfigName, dependencyClone) 可以添加/remove 依赖.
 
 延伸疑问：
-  以上作用仅限于当前 module A，假设A依赖B： A->B ，B（自身已置换成 aar） 中的网络依赖（或者其他依赖）需要写入 A ？
- 如果 A -> B -> C ，B做了变动，C 没做变动。C 置换成 aar ，C 的网络依赖写入A ，还是 B？
- 依次类推 这种递归关系并夹杂部分改动的 module 需要出一个技术方案

**疑问解决**

前言：
1. 项目配置阶段可以拿到整个依赖图
2. 由于 project 输出的顺序随机，技术方案需要兼容从任何一个 project 开始解决依赖关系（但 app module 是第一个遍历）

技术方案初稿：
1. 解决 A 模块的依赖，如果 A 模块没变动，逐层找到 A 的第一个变动的依赖parent，并添加A（aar）+ A 的子依赖到 当前parent。并剔除所有 parent 对 A 的Project依赖。
2. 如果 A 模块变动了，保持 parent 对 A 的依赖，A 的子依赖也不动。

流程图如下：
![RocketXPlugin.jpg](https://upload-images.jianshu.io/upload_images/2788235-f369b3ba2bf9d1bc.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


2、依赖图gradle 生命周期（afterEvaluate）回调，这里可以解析完成我们的操作，在编译时app最先收到评估回调，只要这时修改app的依赖关系图是会阻断其余library module的后续配置流程。

延伸疑问：无法一次得知全局的依赖图，只能一个个 project 自己回调自己的依赖关系，对我们实现业务是否有影响？

疑问解决：
配置阶段可以获取所有的 project 的依赖关系，不过需要根据 buildTypes 和 productFlavors 产生的 configuration 获取依赖。因为可以有 debugImplementation xTestDebugImplementation 这种依赖，单个 project 的依赖都是和 project + buildTypes + productFlavors  做了绑定关系。所以单个project 需要遍历所有的 configuration 去做替换。
```
   project.rootProject.getChildProjects().each { entry ->
            entry.value.configurations.maybeCreate("implementation").dependencies.each {
                println "Testste----" + entry.value.name + ":" + it.name
            }
        }
```



3、local maven 存储？

已预研部分答案：前期可以用 指定文件夹作为仓库，需指定   flatDir { dirs {} } 即可

4、未被修改的 module 通过某个接口可以得知？

延伸疑问：目前未知是哪一个接口
问题解决：
通过遍历所有的文件lastmodify得知，中型项目 ：3W 个文件 1.5s 左右具有可行性：
1. 通过遍历当前子project 的所有文件，并把所有的lastmodify 相加，和上次的快照对比，得知当前 project 是否有改动。
2. 做成工具类输出改变的 project 列表



## 知识储备
1. 分析阅读 gradle plugin 源码相关知识点，可找教程导入 gradle 源码
2. 自定义 plugin 流程，目前定 kotlin 语言编写（以前是grovvy），低版本 AGP 不支持，影响不大。
3. 研究 gradle 所有生命周期可 hook 点

欢迎补充!!



## 第一阶段
解决以上疑问点，并预研出相应的接口

##### 截止 2021/10/22 ，以上问题得以初步解决。

## 第二阶段

- 划分模块
- 部分技术方案划分成功能接口实现
- list 出任务清单
- 自由挑选任务
- 开始编码

相应项目工程已经搭建：
[https://github.com/zhangchaojiong/RocketXPlugin](https://github.com/zhangchaojiong/RocketXPlugin/)

### TaskList
1. 依赖替换技术方案实现：
- 获取整个依赖图（完成）
- 获取当前 project 父依赖 （完成）
- 根据解决方案写入依赖替换规则 （完成）
2. local maven
- 上传接口 -- [智勇/刘祥祥]()
- ~~获取依赖接口，通过传入project，输出Denpency对象。可直接使用（生成 Denpency对象，可阅读 DefaultDependencyHandler 源码)~~
- 获取依赖接口，通过传入project,输出 "com.rocketx:modulename:1.0.0@aar" 即可 --  [智勇/刘祥祥]()

3. AS上的按钮如何制作需要预研
- 按钮点击跑 clean Task -- [照田]()
- 按钮点击跑 run task --  [照田]()

## 第三阶段
1. 迁移到 tcl + 项目运行 -- [张超炯]()
2. 继续完成未完成的任务 --  [智勇]() [刘祥祥]()  [照田]()




## License

```
Copyright (C) 2021 tcler@tcl.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
