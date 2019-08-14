package com.codeless.plugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;

public class Config {
    private static final String[] UN_TRACE_CLASS = {"R.class", "R\\$", "Manifest", "BuildConfig"};
    private static boolean scanAllPackages;
    private static HashSet<String> targetPackages = new HashSet<>();

    public static void set(boolean scanAllPackages, @NotNull HashSet<String> targetPackages) {
        Config.scanAllPackages = scanAllPackages;
        Config.targetPackages = targetPackages;
    }

    static boolean shouldModifyClass(String classPath) {
        if (!classPath.endsWith(".class")) {
            return false;
        }

        if (scanAllPackages) {
            return true;
        }

        if (targetPackages != null) {
            for (String packageName : targetPackages) {
                if (classPath.contains(packageName)) {
                    for (String s : UN_TRACE_CLASS) {
                        if (classPath.contains(s)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

//    public static String path2ClassnameWithSuffix(String path) {
//        return path.replace(File.separator, ".");
//    }
//
//    /**
//     * ;
//     * ;
//     * ;
//     * turn "com.bryansharp.util" to "com/bryansharp/util"
//     *
//     * @param classname full class name
//     * @return class path
//     */
//    public static String className2Path(String classname) {
//        return classname.replace(".", "/");
//    }
}
