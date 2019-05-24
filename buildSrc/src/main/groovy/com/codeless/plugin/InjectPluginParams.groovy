package com.codeless.plugin

class InjectPluginParams {
    String pluginName = ''
    boolean enableModify = true
    boolean watchTimeConsume = false
    boolean keepQuiet = false
    boolean showHelp = true
    boolean enableTransform = false
    HashSet<String> targetPackages = []
}