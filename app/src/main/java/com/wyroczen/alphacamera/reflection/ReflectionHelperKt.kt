package com.wyroczen.alphacamera.reflection

import java.lang.reflect.Method

class ReflectionHelperKt {
    fun zoranBypass(){
        val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
        val getDeclaredMethod = Class::class.java.getDeclaredMethod("getDeclaredMethod", String::class.java, arrayOf<Class<*>>()::class.java)
        val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
        val getRuntime = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
        val sh = getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", arrayOf(arrayOf<String>()::class.java)) as Method
        val vmRuntime = getRuntime.invoke(null)
        sh.invoke(vmRuntime, arrayOf("L"))
    }
}