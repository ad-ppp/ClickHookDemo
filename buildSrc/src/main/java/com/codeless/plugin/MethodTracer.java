package com.codeless.plugin;

import com.codeless.plugin.util.Log;
import com.codeless.plugin.util.Util;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class MethodTracer {
    private final static String TAG = "MethodTracer";

    public void trace(Map<File, File> srcFolderList, Map<File, File> dependencyJarList) {
        traceMethodFromSrc(srcFolderList);
        traceMethodFromJar(dependencyJarList);
    }

    private void traceMethodFromSrc(Map<File, File> srcMap) {
        if (null != srcMap) {
            for (Map.Entry<File, File> entry : srcMap.entrySet()) {
                innerTraceMethodFromSrc(entry.getKey(), entry.getValue());
            }
        }
    }

    private void traceMethodFromJar(Map<File, File> dependencyMap) {
        if (null != dependencyMap) {
            for (Map.Entry<File, File> entry : dependencyMap.entrySet()) {
                innerTraceMethodFromJar(entry.getKey(), entry.getValue());
            }
        }
    }

    private void innerTraceMethodFromSrc(File input, File output) {
        List<File> classFileList = new ArrayList<>();
        if (input.isDirectory()) {
            listClassFiles(classFileList, input);
        } else {
            classFileList.add(input);
        }

        for (File classFile : classFileList) {
            InputStream is = null;
            FileOutputStream os = null;
            final String changedFileInputFullPath = classFile.getAbsolutePath();
            final File changedFileOutput = new File(changedFileInputFullPath.replace(input.getAbsolutePath(), output.getAbsolutePath()));
            if (!changedFileOutput.exists()) {
                changedFileOutput.getParentFile().mkdirs();
                changedFileOutput.delete();
            }

            try {
                changedFileOutput.createNewFile();
                final String absolutePath = classFile.getAbsolutePath();
                String classPkg = absolutePath.replaceAll(input.getAbsolutePath() + File.separator, "").replaceAll(File.separator, ".");
                Log.i(TAG, "[I:%s]\n[classPkg:%s]\n", absolutePath, classPkg);
                if (Config.shouldModifyClass(classPkg)) {
                    is = new FileInputStream(classFile);
                    final byte[] bytes = ModifyClassUtil.modifyClasses(classFile.getName(), IOUtils.toByteArray(is));

                    if (output.isDirectory()) {
                        os = new FileOutputStream(changedFileOutput);
                    } else {
                        os = new FileOutputStream(output);
                    }

                    os.write(bytes);
                    Log.i(TAG, "modify class [I:%s]\n[O:%s]\n"
                            , classFile.getAbsolutePath(), changedFileOutput.getAbsolutePath());
                } else {
                    Log.i(TAG, "copy class [I:%s]\n[O:%s]\n"
                            , classFile.getAbsolutePath(), changedFileOutput.getAbsolutePath());
                    Util.copyFileUsingStream(classFile, changedFileOutput);
                }
            } catch (IOException e) {
                Log.e(TAG, "innerTraceMethodFromSrc exception: [e:%s]", String.valueOf(e.getMessage()));
            } finally {
                Util.closeQuietly(os);
                Util.closeQuietly(is);
            }
        }
    }

    private void innerTraceMethodFromJar(File input, File output) {
        JarOutputStream jarOutputStream = null;
        JarFile jarFile = null;
        try {
            jarOutputStream = new JarOutputStream(new FileOutputStream(output));
            jarFile = new JarFile(input);
            Enumeration enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                String entryName = jarEntry.getName();
                // entryName => com/alibaba/android/arouter/routes/ARouter$$Group$$module_material.class
                final String classPkg = entryName.replaceAll(File.separator, ".");
                byte[] bytes = null;
                final byte[] srcByteCode = IOUtils.toByteArray(inputStream);
                if (Config.shouldModifyClass(classPkg)) {
                    bytes = ModifyClassUtil
                            .modifyClasses(classPkg, srcByteCode);
                }
                if (bytes != null) {
                    inputStream = new ByteArrayInputStream(bytes);
                } else {
                    inputStream = new ByteArrayInputStream(srcByteCode);
                }
                Log.i(TAG, "modify class in jar [%s]\n[I:%s]\n[o:%s]\n\n"
                        , classPkg, input.getAbsolutePath(), output.getAbsolutePath());
                Util.addZipEntry(jarOutputStream, new ZipEntry(entryName), inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(jarFile);
            Util.closeQuietly(jarOutputStream);
        }
    }

    private void listClassFiles(List<File> classFiles, File folder) {
        File[] files = folder.listFiles();
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file == null) {
                continue;
            }
            if (file.isDirectory()) {
                listClassFiles(classFiles, file);
            } else {
                if (file.isFile()) {
                    classFiles.add(file);
                }
            }
        }
    }
}
