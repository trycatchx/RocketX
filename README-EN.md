# RocketXPlugin
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![java](https://img.shields.io/badge/language-kotlin-Borange.svg)

[中文文档](https://github.com/trycatchx/RocketXPlugin/blob/master/README-ZH.md)

## Introduction

This plugin automatically recognizes the unchanged module and replaces it with aar in the compilation process to speed up the full compilation speed of Android projects

## Gradle Build Speed
![build-speed.png](https://github.com/trycatchx/RocketXPlugin/blob/master/IMG/build-speed.png)

## AGP Version Compatible
Plugin version | Gradle version
---|---
4.0.0+ | 6.1.1+
4.1.0+ | 6.5+
4.2.0+ |6.7.1+
7.0    |7.0+

## How to use

#### Way 1：Network dependence

* add rocketx plugin 

```
buildscript {
    dependencies {
        classpath 'io.github.trycatchx:rocketx:1.0.5'
    }
}
```

* add android studio plugin by enter android studio setting->plugins-> marketplace,search "RocketX" and install it .

#### Way 2：Local dependence

Download two jars in the lib folder of the project：

* install “rocketX-studio-plugin.jar” by android studio setting->plugins->install plugin from disk 
* Create a new folder Plugin in the root directory of the project, and put rocketx.jar into it，add the following code to build.gradle in the root directory : `classpath fileTree(dir: './Plugin', include: ['*.jar'])`

######  Use to tap the little rocket to the Spitfire icon，select Assemble${flavor}${buildType} task , Click the original run button of the compiler to compile(The advantage of specifying a task is that there are multiple apply plugin: 'com.android.application'  ，only the currently specified application module will be compiled ) :
![assembleDebug](https://github.com/trycatchx/RocketXPlugin/blob/master/IMG/assembleDebug.jpeg)



## Problem:
* The first acceleration is the slowest because it needs to be fully compiled, packaged out aar and uploaded to LocalMaven
* If you use arouter, please use the local arouter in the zPlugin folder to replace it (see how to replace it in the demo), and it will be faster (the later research is whether there is a better way to solve this problem)
* At present, if there is a compilation error, please run it again. Any problems are welcome to raise issues.


[Blog](https://www.jianshu.com/p/59b95b5a7fab)


## Developer and maintainer
 github | message
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
