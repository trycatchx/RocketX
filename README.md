
<p align="center">
  <a href="https://github.com/trycatchx/RocketXPlugin">
    <img width="200" src="https://github.com/trycatchx/RocketXPlugin/blob/master/rocketX-studio-plugin/resources/META-INF/pluginIcon.svg">
  </a>
</p>

<h1 align="center">RocketXPlugin</h1>
<div align="center">
  
本插件自动识别未改动 module 并在编译流程中替换为 aar ，只编译改动模块，加速 Android 项目的编译速度
  

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![java](https://img.shields.io/badge/language-kotlin-Borange.svg)
</div>

[English Document](https://github.com/trycatchx/RocketXPlugin/blob/master/README-EN.md)


## 编译速度对比
![build-speed.png](https://github.com/trycatchx/RocketXPlugin/blob/master/IMG/build-speed.png)

## AGP 版本兼容
Plugin version | Gradle version
---|---
4.0.0+ | 6.1.1+
4.1.0+ | 6.5+
4.2.0+ |6.7.1+
7.0    |7.0+

## 如何使用

#### 方式一：网络依赖

* 依赖 gradle 插件

```
// app module 的 build.gradle 加入
apply plugin: 'com.rocketx'

// 在根目录的 build.gradle 加入
buildscript {
    dependencies {
        classpath 'io.github.trycatchx:rocketx:1.0.12'
    }
}
```


* 依赖 AS 插件 android studio setting->plugins-> marketplace 搜索 RocketX 安装（搜索不到使用本地安装）

![assembleDebug](https://github.com/trycatchx/RocketXPlugin/blob/master/IMG/asplugin.png)

#### 方式二：本地依赖(实时使用最新的版本，网络依赖需要上传 maven 有所滞后)

下载项目中的 lib 文件夹两个 jar：

* rocketX-studio-plugin.jar 通过 android studio setting->plugins->install plugin from disk 进行安装
* rocketx.jar 通过在项目工程根目录新建文件夹Plugin，置放进去 Plugin文件夹，在根目录的build.gradle 写入 ```classpath fileTree(dir: './Plugin', include: ['*.jar'])```

* 同样在 app module 的 build.gradle 加入：```apply plugin: 'com.rocketx'```

##

######  使用点击小火箭至喷火icon （enable 状态）,点击编译器 run 按钮 :
![assembleDebug](https://github.com/trycatchx/RocketXPlugin/blob/master/IMG/assembleDebug.jpeg)

######  如果你有多个 app module 也可选择 Assemble${flavor}${buildType} task 进行 run


## 配置（可选）
* openLog ：打开 log
* transFormList：debug 阶段可以禁用的 transform ，速度更快（可通过build 的 log 搜索关键字 transFormList 查看自己项目引用了哪些 transform，并手动配置）

```
  //配置插件编译项
    RocketX {
        openLog = true
        //加速模式 禁用可禁用的 transform ，速度更快
        transFormList = ["sensorsAnalyticsAutoTrack","..xx"]
        //指定哪些模块不打成 aar ，字符串为 module.path
        excludeModule = [":module_common"]
    }
```

## 问题
* 第一次的加速，是最慢的因为需要全量编译后，打出 aar 上传到 LocalMaven
* 目前如果编译出错，请重新再 run 一次，出现的问题 欢迎提 issue


[Blog讲解](https://www.jianshu.com/p/59b95b5a7fab)


## 开发维护者
 名单 | 留言
--- | ---
[JustAClamber](https://github.com/JustAClamber) | 知者不惑
[louis](https://github.com/louis-lzt)| louis-lzt
[TryCatch ](https://github.com/trycatchx)   |日落西来，月向东
[FamilyCYZ](https://github.com/FamilyCYZ) | 什么也没有留下


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
