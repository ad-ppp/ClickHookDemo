package com.codeless.plugin

import com.android.build.gradle.BaseExtension
import com.codeless.plugin.utils.DataHelper
import com.codeless.plugin.utils.Log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class InjectPluginImpl implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('codelessConfig', InjectPluginParams)

//        printProjectInfo(project)       //todo : test,useless, why?
        Log.setQuiet(project.codelessConfig.keepQuiet)
        Log.setShowHelp(project.codelessConfig.showHelp)
        Log.logHelp()

        registerTransform(project)
        initDir(project)
        project.afterEvaluate {
            Log.info "project afterEvaluate ====="
            if (project.codelessConfig.watchTimeConsume) {
                project.gradle.addListener(new TimeListener())
            }
        }
    }

    def static registerTransform(Project project) {
        // caution:这边必须要这样写
        final enableInnerTransform = project.getProperties().get("enableInnerTransform").toString().toBoolean()
        Log.info "===enableInnerTransform===${enableInnerTransform}"
        if (enableInnerTransform) {
            BaseExtension android = project.extensions.getByType(BaseExtension)
            InjectTransform transform = new InjectTransform(project)
            android.registerTransform(transform)
        }
    }

    static void initDir(Project project) {
        File pluginTmpDir = new File(project.buildDir, 'LazierTracker')
        Log.info("plugin work dir:" + pluginTmpDir.getAbsolutePath())
        if (!pluginTmpDir.exists()) {
            pluginTmpDir.mkdir()
        }
        DataHelper.ext.pluginTmpDir = pluginTmpDir
    }

    // just to see test info
    static void printProjectInfo(Project project) {
        Task task = project.getTasks().getByName("assemble")
        println "task by name(assemble): $task"
        println ":applied plugin name =  ${project.codelessConfig.pluginName}"
        println "read params: [keepQuiet: ${project.codelessConfig.keepQuiet}" +
                ", showHelp: ${project.codelessConfig.showHelp}" +
                ", watchTimeConsume: ${project.codelessConfig.watchTimeConsume}" +
                ", enableTransform: ${project.codelessConfig.enableTransform}" +
                ", targetPackages: ${project.codelessConfig.targetPackages}" +
                ", hasApplicationPlugin: ${project.plugins.hasPlugin("com.android.application")}" +
                "]"

        //==========properties===========
//        final def properties = project.getProperties()
//        properties.forEach(new BiConsumer<String, Object>() {
//            @Override
//            void accept(String s, Object o) {
//                Log.info "property, key = ${s},value = ${o}"
//            }
//        })
//        Log.info "status = ${project.status}"
    }
}