package com.codeless.plugin.transform

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.codeless.plugin.util.Log
import com.codeless.plugin.util.ReflectUtil
import com.google.common.base.Charsets
import com.google.common.hash.Hashing

import java.lang.reflect.Field

public abstract class BaseProxyTransform extends Transform {
    private final String TAG = "BaseProxyTransform"
    protected final Transform origTransform

    public BaseProxyTransform(Transform origTransform) {
        this.origTransform = origTransform
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return origTransform.getInputTypes()
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return origTransform.getScopes()
    }

    @Override
    boolean isIncremental() {
        return origTransform.isIncremental()
    }

    protected String getUniqueJarName(File jarFile) {
        final String origJarName = jarFile.getName()
        final String hashing = Hashing.sha1().hashString(jarFile.getPath(), Charsets.UTF_16LE).toString()
        final int dotPos = origJarName.lastIndexOf('.')
        if (dotPos < 0) {
            return "${origJarName}_${hashing}"
        } else {
            final String nameWithoutDotExt = origJarName.substring(0, dotPos)
            final String dotExt = origJarName.substring(dotPos)
            return "${nameWithoutDotExt}_${hashing}${dotExt}"
        }
    }

    protected void replaceFile(QualifiedContent input, File newFile) {
        def iF = input.getFile()

        final Field fileField = ReflectUtil.getDeclaredFieldRecursive(input.getClass(), 'file')
        fileField.set(input, newFile)

        def oF = input.getFile()
        Log.i("replaceFile", "[I:${iF.getAbsolutePath()}][O:${oF.getAbsolutePath()}]\n")
    }

    protected void replaceChangedFile(DirectoryInput dirInput, Map<File, Status> changedFiles) {
        final Field changedFilesField = ReflectUtil.getDeclaredFieldRecursive(dirInput.getClass(), 'changedFiles')
        changedFilesField.set(dirInput, changedFiles)
        Log.i("replaceChangedFile", changedFiles.toString())
    }
}