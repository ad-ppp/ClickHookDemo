package com.codeless.plugin.transform

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformTask
import com.codeless.plugin.Config
import com.codeless.plugin.MethodTracer
import com.codeless.plugin.util.Log
import com.codeless.plugin.util.Util
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener

import java.lang.reflect.Field

public class InjectTransform extends BaseProxyTransform {
    private Project project
    def variant

    private InjectTransform(Project project, def variant, Transform origTransform) {
        super(origTransform)
        this.project = project
        this.variant = variant

        Config.set(project.codelessConfig.scanAllPackages, getTargetPackage())
    }

    public static void inject(Project project, def variant) {
        String hackTransformTaskName = getTransformTaskName(
                "",
                "", variant.name
        )

        String hackTransformTaskNameForWrapper = getTransformTaskName(
                "",
                "Builder", variant.name
        )

        project.logger.info("prepare inject dex transform :" + hackTransformTaskName + " hackTransformTaskNameForWrapper:" + hackTransformTaskNameForWrapper)

        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            void graphPopulated(TaskExecutionGraph taskExecutionGraph) {
                for (Task task : taskExecutionGraph.getAllTasks()) {
                    if ((task.name.equalsIgnoreCase(hackTransformTaskName) || task.name.equalsIgnoreCase(hackTransformTaskNameForWrapper))
                            && !(((TransformTask) task).getTransform() instanceof InjectTransform)) {
                        def transformTask = (TransformTask) task
                        def transform = transformTask.getTransform()
                        Log.i("originTransform"
                                ,"scopes: ${transform.getScopes()}\n"
                                + "inputTypes: ${transform.getInputTypes()}\n"
                                + "isIncremental: ${transform.isIncremental()}"
                        )

                        project.logger.warn("find dex transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                        project.logger.info("variant name: " + variant.name)


                        Field field = TransformTask.class.getDeclaredField("transform")
                        field.setAccessible(true)
                        field.set(task, new InjectTransform(project, variant, task.transform))
                        project.logger.warn("transform class after hook: " + task.transform.getClass())
                        break
                    }
                }
            }
        })
    }

    @Override
    String getName() {
        return project.codelessConfig.pluginName
    }

    @Override
    public void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {

        Log.i(getName(), "[start ${getName()} transform]")
         long start = System.currentTimeMillis()
        final boolean isIncremental = transformInvocation.isIncremental() && this.isIncremental()
        final def context = transformInvocation.getContext()
        final inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider

        final File rootOutput = new File(project.getBuildDir().getAbsolutePath() + File.separator
                + "xhbCodeLessBuilder", "classes/${getName()}/")
        if (!rootOutput.exists()) {
            rootOutput.mkdirs()
        }

        Log.i(getName(), "context info \n"
                + "[SimpleName: ${context.getClass().getSimpleName()}]"
                + "[Path: ${context.getPath()}]"
                + "[TemporaryDir: ${context.getTemporaryDir().getAbsolutePath()}]"
                + "[VariantName: ${context.getVariantName()}]"
        )

        Map<File, File> jarInputMap = new HashMap<>()
        Map<File, File> scrInputMap = new HashMap<>()

        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                collectAndIdentifyDir(scrInputMap, dirInput, rootOutput, isIncremental)
            }
            input.jarInputs.each { JarInput jarInput ->
                if (jarInput.getStatus() != Status.REMOVED) {
                    collectAndIdentifyJar(jarInputMap, scrInputMap, jarInput, rootOutput, isIncremental)
                }
            }
        }
        MethodTracer tracer = new MethodTracer()
        tracer.trace(scrInputMap, jarInputMap)

        origTransform.transform(transformInvocation)
        Log.i("XHB." + getName(), "[end ${getName()} transform] cost time: %dms", System.currentTimeMillis() - start)
    }

    private void collectAndIdentifyDir(Map<File, File> dirInputMap, DirectoryInput directoryInput, File rootOutput, boolean isIncremental) {
        File dirInput = directoryInput.file
        File dirOutput = new File(rootOutput, dirInput.getName())
        if (!dirOutput.exists()) {
            dirOutput.mkdirs()
        }

        Log.i(getName(), "collect class dir："
                + "\n[DirectoryInput:${dirInput.getAbsolutePath()}]"
                + "\n[isIncremental:$isIncremental]"
        )

        if (isIncremental) {
            if (!dirInput.exists()) {
                dirOutput.deleteDir()
            } else {
                final Map<File, Status> obfuscatedChangedFiles = new HashMap<>()
                final String rootInputFullPath = dirInput.getAbsolutePath()
                final String rootOutputFullPath = dirOutput.getAbsolutePath()
                directoryInput.changedFiles.each { Map.Entry<File, Status> entry ->
                    final File changedFileInput = entry.getKey()
                    final String changedFileInputFullPath = changedFileInput.getAbsolutePath()
                    final File changedFileOutput = new File(
                            changedFileInputFullPath.replace(rootInputFullPath, rootOutputFullPath)
                    )
                    final Status status = entry.getValue()
                    switch (status) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED:
                            dirInputMap.put(changedFileInput, changedFileOutput)
                            break
                        case Status.REMOVED:
                            changedFileOutput.delete()
                            break
                    }
                    obfuscatedChangedFiles.put(changedFileOutput, status)
                }
                replaceChangedFile(directoryInput, obfuscatedChangedFiles)
            }
        } else {
            Log.i("InjectTransform." + getName(), "DirectoryInput collector," +
                    "[directoryInput: ${dirInput.getAbsolutePath()}\n outputDir: ${dirOutput.getAbsolutePath()}]"
            )
            dirInputMap.put(dirInput, dirOutput)
        }
        replaceFile(directoryInput, dirOutput)
    }

    private void collectAndIdentifyJar(Map<File, File> jarInputMaps, Map<File, File> dirInputMaps, JarInput input, File rootOutput, boolean isIncremental) {
        final File jarInput = input.file
        final File jarOutput = new File(rootOutput, getUniqueJarName(jarInput))


        Log.i(getName(), "collect jar path："
                + "\n[JarInput:${jarInput.getAbsolutePath()}]"
                + "\n[isIncremental:$isIncremental]"
        )
        if (Util.isRealZipOrJar(jarInput)) {
            Log.i("InjectTransform.${getName()}", "observe ZipOrJar file: \n[I]${jarInput.getAbsolutePath()},\n[O]${jarOutput.getAbsolutePath()}\n")
            switch (input.status) {
                case Status.NOTCHANGED:
                    if (isIncremental) {
                        break
                    }
                case Status.ADDED:
                case Status.CHANGED:
                    jarInputMaps.put(jarInput, jarOutput)
                    break
                case Status.REMOVED:
                    break
            }
        } else {
            Log.i("InjectTransform.${getName()}", "observe N-ZipOrJar file: \n[I]${jarInput.getAbsolutePath()},\n[O]${jarOutput.getAbsolutePath()}\n")

            // Special case for WeChat AutoDex. Its rootInput jar file is actually
            // a txt file contains path list.
            BufferedReader br = null
            BufferedWriter bw = null
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(jarInput)))
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jarOutput)))
                String realJarInputFullPath
                while ((realJarInputFullPath = br.readLine()) != null) {
                    // src jar.
                    final File realJarInput = new File(realJarInputFullPath)
                    // dest jar, moved to extraguard intermediate output dir.
                    File realJarOutput = new File(rootOutput, getUniqueJarName(realJarInput))

                    if (realJarInput.exists() && Util.isRealZipOrJar(realJarInput)) {
                        jarInputMaps.put(realJarInput, realJarOutput)
                    } else {
                        realJarOutput.delete()
                        if (realJarInput.exists() && realJarInput.isDirectory()) {
                            realJarOutput = new File(rootOutput, realJarInput.getName())
                            if (!realJarOutput.exists()) {
                                realJarOutput.mkdirs()
                            }
                            dirInputMaps.put(realJarInput, realJarOutput)
                        }

                    }
                    // write real output full path to the fake jar at rootOutput.
                    final String realJarOutputFullPath = realJarOutput.getAbsolutePath()
                    bw.writeLine(realJarOutputFullPath)
                }
            } catch (FileNotFoundException e) {
                Log.e("InjectTransform." + getName(), "FileNotFoundException:%s", e.toString())
            } finally {
                Util.closeQuietly(br)
                Util.closeQuietly(bw)
            }
            jarInput.delete() // delete raw inputList
        }

        replaceFile(input, jarOutput)
    }

    private Set<String> getTargetPackage() {
        Set<String> set = new HashSet<>()
        String appPackageName = variant.applicationId
        if (appPackageName != null) {
            set.add(appPackageName)
        }

        // 3rd party JAR packages that want our plugin to inject.
        HashSet<String> inputPackages = project.codelessConfig.targetPackages
        if (inputPackages != null) {
            set.addAll(inputPackages)
        }
        return set
    }

    private static String getTransformTaskName(String customDexTransformName, String wrappSuffix, String buildTypeSuffix) {
        if (customDexTransformName != null && customDexTransformName.length() > 0) {
            return customDexTransformName + "For${buildTypeSuffix}"
        }
        return "transformClassesWithDex${wrappSuffix}For${buildTypeSuffix}"
    }
}