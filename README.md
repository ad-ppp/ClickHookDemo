

[全局监控 click事件的四种方式](https://www.jianshu.com/p/1c672083f301) 

## 1. MainActivity 示例反射机制实现插桩。
1. 必须先设置listener，最后启动hook。


## 2. Gradle plugin 方式
示例： `SecondActivity`

优点：
    不存在效率问题，只会牺牲一些编译速度.
    而且可以对第三方Library注入字节码实现。
    
原理：
---
##### 1. 首先需要了解apk的打包流程

![](https://user-gold-cdn.xitu.io/2017/3/2/35a4d886bc51ec6be29456eadd4b1fd2.png?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)

从上面的流程图，我们可以看出apk打包流程可以分为以下七步
1. 通过aapt打包res资源文件，生成R.java、resources.arsc和res文件（二进制 & 非二进制如res/raw和pic保持原样）
2. 处理.aidl文件，生成对应的Java接口文件
3. 通过Java Compiler编译R.java、Java接口文件、Java源文件，生成.class文件
4. 通过dex命令，将.class文件和第三方库中的.class文件处理生成classes.dex
5. 通过apkbuilder工具，将aapt生成的resources.arsc和res文件、assets文件和classes.dex一起打包生成apk
6. 通过Jarsigner工具，对上面的apk进行debug或release签名
7. 通过zipalign工具，将签名后的apk进行对齐处理。

---


参考： 
- [异常简单的demo-任何自定义 gradle plugin](https://github.com/jacky1234/SimplePluginDemo)
- [Android无埋点数据收集SDK关键技术](https://www.jianshu.com/p/b5ffe845fe2d)
- [应用于Android无埋点的Gradle插件解析](https://www.jianshu.com/p/250c83449dc0)
- [base on the project-hibeaver](https://github.com/BryanSharp/hibeaver)

---

遇到的问题：
##### 1. java sdk版本过高； Could not determine java version from '10.0.1'. 导致不能debug gradle plugin
    rm sdk高版本，安装低版本。
    
##### 2. gradle not found
    brew install gradle

##### 3. lambda表达式不支持注入bytecode问题
在gradle.properties文件下增加如下配置：
android.enableD8.desugaring=false

**desugar**
The desugar step is executed normally after javac to optimize the bytecode. Enabling desugaring in D8 will remove that step entirely, and execute desugar as part of D8, making it faster and more optimized.
And the link is [DX vs D8](https://android-developers.googleblog.com/2017/08/next-generation-dex-compiler-now-in.html)

##### 积累
1. [Class#defineClass](https://paper.seebug.org/572/)


##### 展望
1. 通过AOP技术，实现耗时操作统计，并列举出UI操作比较耗时的方法。
    
##### tools:
**1. [plugin-ASM bytecode outLine](https://plugins.jetbrains.com/plugin/5918-asm-bytecode-outline)**
- [AMS-bytecode 工具操作字节码介绍](https://plugins.jetbrains.com/plugin/5918-asm-bytecode-outline)

- - -

## Thanks To:
1. [通过Gradle的Transform配合ASM实战路由框架和统计方法耗时](https://blog.csdn.net/Neacy_Zz/article/details/78546237)
2. [巴巴巴巴巴巴掌-博客关于plugin的解析](http://www.wangyuwei.me/)
3. [project-demo-CostTime](https://github.com/JeasonWong/CostTime)