# RocketXPlugin
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![java](https://img.shields.io/badge/language-kotlin-Borange.svg)


## 描述
本插件加速 Android 项目的全量编译速度

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
buildscript {
    dependencies {
        classpath 'io.github.trycatchx:rocketx:1.0.1'
    }
}
```

* 依赖 AS 插件 android studio setting->plugins-> marketplace 搜索 RocketX 安装（搜索不到使用本地安装）

#### 方式二：本地依赖

下载项目中的 lib 文件夹两个 jar：

* rocketX-studio-plugin.jar 通过 android studio setting->plugins->install plugin from disk 进行安装
* rocketx.jar 通过在项目工程根目录新建文件夹Plugin，置放进去 Plugin文件夹，在根目录的build.gradle 写入 classpath fileTree(dir: './Plugin', include: ['*.jar'])

####  使用点击小火箭至喷火icon （enable 状态），点击编译器原有的 run 按钮进行编译

## 问题
* 第一次的加速，是最慢的因为需要全量编译后，打出 aar 上传到 LocalMaven
* 目前如果编译出错，请重新再 run 一次，出现的问题 欢迎提 issue


[Blog讲解](https://www.jianshu.com/p/59b95b5a7fab)


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
