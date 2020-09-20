package com.wyroczen.alphacamera.asm;

import android.util.Log;

import com.wyroczen.alphacamera.stock.ReflectUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ASMHelper {

    public final String TAG = "AlphaCamera-ASM:";

    public void doSomething() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Log.i(TAG,"Gówno");
        String clazzName = Class.forName("android.os.SystemProperties").getName();
        Class sp = Class.forName("android.os.SystemProperties");
        Log.i(TAG,"Class: " + clazzName);
        Method get = ReflectUtils.findMethod(sp, "get", new String[]{"ro.netflix.bsp_rev"});
        Log.i(TAG,"Method: " + get.getName());
        byte[] code = disableMethod(get, sp);
        Log.i(TAG,"code obtained");
        new ClassLoader() {
            Class<?> get() { return defineClass(null, code, 0, code.length); }
        }   .get()
                .getMethod("get").invoke(null);
    }

    private static byte[] disableMethod(Method method, Class c) {
        Class<?> theClass = c;//method.getDeclaringClass();
        ClassReader cr;
        try { // use resource lookup to get the class bytes
            cr = new ClassReader(
                    //theClass.getResourceAsStream(theClass.getSimpleName()+".class"));
                    theClass.getResourceAsStream(theClass.getName().replace('.', '/') + ".class"));

        } catch(IOException ex) {
            throw new IllegalStateException(ex);
        }
        // passing the ClassReader to the writer allows internal optimizations
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(new MethodReplacer(
                cw, method.getName(), Type.getMethodDescriptor(method)), 0);

        byte[] newCode = cw.toByteArray();
        return newCode;
    }

    static class MethodReplacer extends ClassVisitor {
        private final String hotMethodName, hotMethodDesc;

        MethodReplacer(ClassWriter cw, String name, String methodDescriptor) {
            super(Opcodes.ASM5, cw);
            hotMethodName = name;
            hotMethodDesc = methodDescriptor;
        }

        // invoked for every method
        @Override
        public MethodVisitor visitMethod(
                int access, String name, String desc, String signature, String[] exceptions) {

            if(!name.equals(hotMethodName) || !desc.equals(hotMethodDesc))
                // reproduce the methods we're not interested in, unchanged
                return super.visitMethod(access, name, desc, signature, exceptions);

            // alter the behavior for the specific method
            return new ReplaceWithEmptyBody(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    (Type.getArgumentsAndReturnSizes(desc)>>2)-1);
//            return new ReplaceStringConstant(
//                    super.visitMethod(access, name, desc, signature, exceptions),
//                    "This is a test", "This is a replacement");
        }
    }
    static class ReplaceWithEmptyBody extends MethodVisitor {
        private final MethodVisitor targetWriter;
        private final int newMaxLocals;

        ReplaceWithEmptyBody(MethodVisitor writer, int newMaxL) {
            // now, we're not passing the writer to the superclass for our radical changes
            super(Opcodes.ASM5);
            targetWriter = writer;
            newMaxLocals = newMaxL;
        }

        // we're only override the minimum to create a code attribute with a sole RETURN

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            targetWriter.visitMaxs(0, newMaxLocals);
        }

        @Override
        public void visitCode() {
            targetWriter.visitCode();
            targetWriter.visitInsn(Opcodes.RETURN);// our new code
        }

        @Override
        public void visitEnd() {
            targetWriter.visitEnd();
        }

        // the remaining methods just reproduce meta information,
        // annotations & parameter names

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return targetWriter.visitAnnotation(desc, visible);
        }

        @Override
        public void visitParameter(String name, int access) {
            targetWriter.visitParameter(name, access);
        }
    }

    static class ReplaceStringConstant extends MethodVisitor {
        private final String matchString, replaceWith;

        ReplaceStringConstant(MethodVisitor writer, String match, String replacement) {
            // now passing the writer to the superclass, as most code stays unchanged
            super(Opcodes.ASM5, writer);
            matchString = match;
            replaceWith = replacement;
        }

        @Override
        public void visitLdcInsn(Object cst) {
            super.visitLdcInsn(matchString.equals(cst)? replaceWith: cst);
        }
    }



}
