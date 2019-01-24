

[全局监控 click事件的四种方式](https://www.jianshu.com/p/1c672083f301) 

## 1. MainActivity 示例反射机制实现插桩。
1. 必须先设置listener，最后启动hook。


## 2. Gradle plugin 方式
示例： `SecondActivity`

优点：
    不存在效率问题，只会牺牲一些编译速度.
    而且可以对第三方Library注入字节码实现。


---

原理及用途可以参考：
[公众号：一起玩转Android项目中的字节码](https://mp.weixin.qq.com/s/s4WgLFN0A-vO0ko0wi25mA)

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

    a. https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
    
##### 2. command not found ,etc['ls','vim'] and so on
    重新安装 .oh-my-zsh。
    a. rm -rf ~/.oh-my-zsh
    b. open 'https://github.com/robbyrussell/oh-my-zsh' to find the way to install oh-my-zsh
    
##### 2. gradle not found
    brew install gradle

##### 3. lambda表达式不支持注入bytecode问题
在gradle.properties文件下增加如下配置：
android.enableD8.desugaring=false

[**desugar**](https://developer.android.com/studio/write/java8-support?hl=zh-cn)
The desugar step is executed normally after javac to optimize the bytecode. Enabling desugaring in D8 will remove that step entirely, and execute desugar as part of D8, making it faster and more optimized.

##### 4. [To many file open 限制](https://superuser.com/questions/433746/is-there-a-fix-for-the-too-many-open-files-in-system-error-on-os-x-10-7-1/443168#443168)


##### 积累
1. [Class#defineClass](https://paper.seebug.org/572/)
2. 如果需要理解asm解析class的机制需要了解class相关的结构。相关链接可以参考：
    A. [掘金-JVM-TAG](https://juejin.im/tag/JVM)
        a. [Java Class文件结构实例分析（上）](https://juejin.im/post/5bc361fce51d450e3d2d2ede)

    
##### Tools:
**1. [plugin-ASM bytecode outLine](https://plugins.jetbrains.com/plugin/5918-asm-bytecode-outline)**
- [AMS-bytecode 工具操作字节码介绍](https://plugins.jetbrains.com/plugin/5918-asm-bytecode-outline)

- - -

## Thanks To:
1. [通过Gradle的Transform配合ASM实战路由框架和统计方法耗时](https://blog.csdn.net/Neacy_Zz/article/details/78546237)
2. [巴巴巴巴巴巴掌-博客关于plugin的解析](http://www.wangyuwei.me/)
3. [project-demo-CostTime](https://github.com/JeasonWong/CostTime)