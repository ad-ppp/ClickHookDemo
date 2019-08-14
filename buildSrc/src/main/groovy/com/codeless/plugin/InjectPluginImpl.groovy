package com.codeless.plugin

import com.codeless.plugin.extension.CodelessExtension
import com.codeless.plugin.transform.InjectTransform
import com.codeless.plugin.util.Log
import com.google.common.base.Strings
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectPluginImpl implements Plugin<Project> {
    final String DEFAULT_PLUGIN_NAME = "LazierTracker"
    final String TAG = "InjectPluginImpl"

    @Override
    void apply(Project project) {
        project.extensions.create('codelessConfig', CodelessExtension)
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('xhb-codeless-plugin, Android Application plugin required')
        }

        project.afterEvaluate {
            def android = project.extensions.android
            def configuration = project.codelessConfig


            Log.setEnable(configuration.logable)
            Log.setLearn(configuration.learnMode)
            Log.setShowHelp(configuration.showHelp)
            Log.i(TAG, "[enable log:${configuration.logable}]" +
                    "[learn more:${configuration.learnMode}]" +
                    "[showHelp:${configuration.showHelp}]")

            android.applicationVariants.all { variant ->
                if (Strings.isNullOrEmpty(configuration.pluginName)) {
                    configuration.pluginName = DEFAULT_PLUGIN_NAME
                }

//                project.gradle.addListener(new TimeListener())

                def enable = project.getProperties().get("enableInnerTransform").toString().trim().toBoolean()
                if (enable) {
                    Log.i(TAG, "enable InjectPluginImpl,start...")
                    InjectTransform.inject(project, variant)
                }
            }
        }
    }
}