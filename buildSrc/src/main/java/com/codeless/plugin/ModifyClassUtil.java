package com.codeless.plugin;

import com.android.annotations.NonNull;
import com.codeless.plugin.item.MethodCell;
import com.codeless.plugin.util.Log;
import com.codeless.plugin.util.MethodLogVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ;
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.;
 * Modified by nailperry on 2017/3/2.;
 * ;
 */
public class ModifyClassUtil {

    @NonNull
    public static byte[] modifyClasses(String className, byte[] srcByteCode) {
        byte[] classBytesCode = null;
        try {
            Log.info("====start modifying " + className + "====");
            classBytesCode = modifyClass(srcByteCode);
            Log.info("====revisit modified " + className + "====");
            onlyVisitClassMethod(classBytesCode);
            Log.info("====finish modifying " + className + "====");
            return classBytesCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode;
        }
        return classBytesCode;
    }


    private static byte[] modifyClass(byte[] srcClass) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor methodFilterCV = new MethodFilterClassVisitor(classWriter);
        ClassReader cr = new ClassReader(srcClass);
        cr.accept(methodFilterCV, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

    private static void onlyVisitClassMethod(byte[] srcClass) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodFilterClassVisitor methodFilterCV = new MethodFilterClassVisitor(classWriter);
        methodFilterCV.onlyVisit = true;
        ClassReader cr = new ClassReader(srcClass);
        cr.accept(methodFilterCV, ClassReader.EXPAND_FRAMES);
    }

    private static boolean instanceOfFragment(String superName) {
        return "android/app/Fragment".equals(superName) || "android/support/v4/app/Fragment".equals(superName);
    }

    private static boolean instanceOfActivity(String superName) {
        return "android/app/Activity".equals(superName) || "android/support/v7/app/AppCompatActivity".equals(superName);
    }

    /**
     * ;
     * ;
     *
     * @param opcode;      the opcode of the type instruction to be visited. This opcode;
     *                     is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or;
     *                     INVOKEINTERFACE.;
     * @param owner;       the internal name of the method's owner class (see;
     *                     {@link org.objectweb.asm.Type#getInternalName() getInternalName}).;
     * @param methodName   the method's name.
     * @param methodDesc   the method's descriptor (see {@link org.objectweb.asm.Type Type}).;
     * @param start        方法参数起始索引（ 0：this，1+：普通参数 ）;
     *                     ;
     * @param count        方法参数个数;
     *                     ;
     * @param paramOpcodes 参数类型对应的ASM指令;
     *                     ;
     */
    private static void visitMethodWithLoadedParams(MethodVisitor methodVisitor, int opcode,
                                                    String owner, String methodName, String methodDesc,
                                                    int start, int count, List<Integer> paramOpcodes) {
        for (int i = start; i < start + count; i++) {
            methodVisitor.visitVarInsn(paramOpcodes.get(i - start), i);
        }
        methodVisitor.visitMethodInsn(opcode, owner, methodName, methodDesc, false);
    }

    static class MethodFilterClassVisitor extends ClassVisitor {
        boolean onlyVisit = false;
        HashSet<String> visitedFragMethods = new HashSet<>();
        private String name;
        private String superName;
        private String[] interfaces;

        MethodFilterClassVisitor(final ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            Log.i("* visit *", com.codeless.plugin.util.Log.accCode2String(access), name, signature, superName, interfaces);
            this.name = name;
            this.superName = superName;
            this.interfaces = interfaces;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            MethodVisitor mv = null;

            if (!onlyVisit) {
                Log.i("* visitMethod *", com.codeless.plugin.util.Log.accCode2String(access), name, desc, signature, exceptions);
            }
            MethodCell methodCell = ReWriterConfig.sInterfaceMethods.get(name + desc);
            if (methodCell != null) {
                if (onlyVisit) {
                    mv = new MethodLogVisitor(cv.visitMethod(access, name, desc, signature, exceptions));
                } else {
                    try {
                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                        mv = new MethodLogVisitor(methodVisitor) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                visitMethodWithLoadedParams(methodVisitor, Opcodes.INVOKESTATIC,
                                        ReWriterConfig.sAgentClassName, methodCell.getAgentName(),
                                        methodCell.getAgentDesc(), methodCell.getParamsStart(),
                                        methodCell.getParamsCount(), methodCell.getOpcodes());
                            }
                        };
                    } catch (Exception e) {
                        e.printStackTrace();
                        mv = null;
                    }
                }
            }

            MethodCell methodCell2 = null;
            if (instanceOfFragment(superName)) {
                methodCell2 = ReWriterConfig.sFragmentMethods.get(name + desc);
            } else if (instanceOfActivity(superName)) {
                methodCell2 = ReWriterConfig.sActivityMethods.get(name + desc);
            }

            if (methodCell2 != null) {
                // 记录该方法已存在;
                visitedFragMethods.add(name + desc);
                if (onlyVisit) {
                    mv = new MethodLogVisitor(mv == null ? cv.visitMethod(access, name, desc, signature, exceptions) : mv);
                } else {
                    try {
                        MethodVisitor methodVisitor = mv == null ? cv.visitMethod(access, name, desc, signature, exceptions) : mv;
                        MethodCell finalMethodCell = methodCell2;
                        mv = new MethodLogVisitor(methodVisitor) {

                            @Override
                            public void visitInsn(int opcode) {
                                // 确保super.onHiddenChanged(hidden);等先被调用;
                                if (opcode == Opcodes.RETURN) { //在返回之前安插代码;
                                    visitMethodWithLoadedParams(methodVisitor, Opcodes.INVOKESTATIC,
                                            ReWriterConfig.sAgentClassName, finalMethodCell.getAgentName(),
                                            finalMethodCell.getAgentDesc(), finalMethodCell.getParamsStart(),
                                            finalMethodCell.getParamsCount(), finalMethodCell.getOpcodes());
                                }
                                super.visitInsn(opcode);
                            }

                        };
                    } catch (Exception e) {
                        e.printStackTrace();
                        mv = null;
                    }
                }
            }

            if (mv != null) {
                return mv;
            }

            return cv.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            MethodVisitor mv;
            Iterator<Map.Entry<String, MethodCell>> iterator = null;
            if (instanceOfFragment(superName)) {
                // 添加剩下的方法，确保super.onHiddenChanged(hidden);等先被调用;
                iterator = ReWriterConfig.sFragmentMethods.entrySet().iterator();
            } else if (instanceOfActivity(superName)) {
                iterator = ReWriterConfig.sActivityMethods.entrySet().iterator();
            }

            while (iterator != null && iterator.hasNext()) {
                Map.Entry<String, MethodCell> entry = iterator.next();
                String key = entry.getKey();
                MethodCell methodCell = entry.getValue();

                if (visitedFragMethods.contains(key)) {
                    // todo visitor;
                    com.codeless.plugin.util.Log.i("ModifyClassUtil", "visitedFragMethods contains $key");
                    continue;
                }
                com.codeless.plugin.util.Log.i("ModifyClassUtil", "visitedFragMethods does not contains $key");

                mv = cv.visitMethod(Opcodes.ACC_PUBLIC, methodCell.getName(), methodCell.getDesc(), null, null);
                mv.visitCode();
                // call super
                visitMethodWithLoadedParams(mv, Opcodes.INVOKESPECIAL, superName, methodCell.getName(),
                        methodCell.getDesc(), methodCell.getParamsStart(), methodCell.getParamsCount(), methodCell.getOpcodes());
                // call injected method
                visitMethodWithLoadedParams(mv, Opcodes.INVOKESTATIC, ReWriterConfig.sAgentClassName,
                        methodCell.getAgentName(), methodCell.getAgentDesc(), methodCell.getParamsStart(),
                        methodCell.getParamsCount(), methodCell.getOpcodes());
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(methodCell.getParamsCount(), methodCell.getParamsCount());
                mv.visitEnd();
            }
            super.visitEnd();
        }
    }
}
