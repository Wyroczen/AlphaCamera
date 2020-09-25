//
// Created by wisni on 20.09.2020.
//

#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>

using std::string;
using std::cout;
using std::endl;

int SIGNATURE_CORRECT = 0;


void print_welcome_message() {
    cout << "Hello from shared library!" << endl;
}

jobject getContext(JNIEnv *env) {
    jclass jAppAppGlobalsClass = env->FindClass("android/app/AppGlobals");
    jmethodID jGetInitialApplication = env->GetStaticMethodID(jAppAppGlobalsClass,
                                                              "getInitialApplication",
                                                              "()Landroid/app/Application;");

    jobject jApplicationObject = env->CallStaticObjectMethod(jAppAppGlobalsClass,
                                                             jGetInitialApplication);
    return jApplicationObject;
}

/**
 *
 * @param env - JNIEnv instance, shared to the whole application
 * @return
 */
jstring getSignature(JNIEnv *env) {
    jobject thiz = getContext(env);

    // Context
    jclass native_context = env->FindClass("android/content/Context");

    // Context#getPackageManager()
    jmethodID methodID_func = env->GetMethodID(native_context, "getPackageManager",
                                               "()Landroid/content/pm/PackageManager;");
    jobject package_manager = env->CallObjectMethod(thiz, methodID_func);
    jclass pm_clazz = env->GetObjectClass(package_manager);

    // PackageManager#getPackageInfo()
    jmethodID methodId_pm = env->GetMethodID(pm_clazz, "getPackageInfo",
                                             "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    // Context#getPackageName()
    jmethodID methodID_packagename = env->GetMethodID(native_context, "getPackageName",
                                                      "()Ljava/lang/String;");
    jstring name_str = static_cast<jstring>(env->CallObjectMethod(thiz, methodID_packagename));
    jobject package_info = env->CallObjectMethod(package_manager, methodId_pm, name_str, 64);
    jclass pi_clazz = env->GetObjectClass(package_info);

    // PackageInfo#signatures
    jfieldID fieldID_signatures = env->GetFieldID(pi_clazz, "signatures",
                                                  "[Landroid/content/pm/Signature;");
    jobject signatur = env->GetObjectField(package_info, fieldID_signatures);
    jobjectArray signatures = reinterpret_cast<jobjectArray>(signatur);

    // PackageInfo#signatures[0]
    jobject signature = env->GetObjectArrayElement(signatures, 0);
    jclass s_clazz = env->GetObjectClass(signature);

    // PackageInfo#signatures[0].toCharString()
    jmethodID methodId_ts = env->GetMethodID(s_clazz, "toCharsString", "()Ljava/lang/String;");
    jstring ts = (jstring) env->CallObjectMethod(signature, methodId_ts);

    return ts;//env->GetStringUTFChars(ts, 0);
}

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes,
                                                                       env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte *pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *) pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

/*
 * Class:     NativeLibJNI
 * Method:    printWelcome
 * Signature: ()V
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_wyroczen_alphacamera_jni_NativeLibJNI_printWelcome(JNIEnv *env, jobject thiz) {
    print_welcome_message();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_wyroczen_alphacamera_jni_NativeLibJNI_getWelcome(JNIEnv *env, jobject thiz) {

    const char *test = "Hello from the lib!";
    //const char signature = *getSignature(env);
    return env->NewStringUTF(test);
    return getSignature(env);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_wyroczen_alphacamera_jni_NativeLibJNI_isSignatureCorrect(JNIEnv *env, jobject thiz) {

    jstring native_signature = getSignature(env);
    string native_signature_string = jstring2string(env, native_signature);
    string signature_string = "308201dd30820146020101300d06092a864886f70d010105050030373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b3009060355040613025553301e170d3139303333303131313231355a170d3439303332323131313231355a30373116301406035504030c0d416e64726f69642044656275673110300e060355040a0c07416e64726f6964310b300906035504061302555330819f300d06092a864886f70d010101050003818d0030818902818100c6cfdfc2d59a6271acccb45be0edb27e33d20450afc82cb4057ec32de15a4e55ff18a24f4fef64def0439e387e9d420d60ab77162c76b444548a683ecde9f8dd69774196bc4fadb46e757acd1aa5a825892a651cdc5c2bd23a5cf2d23c90ea974ec3041e4d383049853d9d2b2a64ac69e3ae09dee5f9e9c20cf63995631f25e10203010001300d06092a864886f70d01010505000381810078459c60881e71385c8106269606811e4caaa5ebc38c5771e8c09d4fbd6d0e43ffd4be9c77815edb8645a578860624f195c707cd04ac61d6a5405caa24e75b67e7f4c8d7c1adbf95c9634a8dbe390c7bb16756b98b9a4d2417ba5d6195e92936a1e1dc63c94cb9057959a7a6c222dd030cb1fe2b2cb415e1477426d286a34f8a";

    int match;
    bool result = false;
    match = signature_string.compare(native_signature_string);
    __android_log_print(ANDROID_LOG_INFO, "AlphaCamera-Native", "Signature check (0 is true) = %d",
                        match); //0 is ok
    if (match == 0) {
        SIGNATURE_CORRECT = 1;
        result = true;
    }

    return (jboolean) result;
}extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_wyroczen_alphacamera_jni_NativeLibJNI_removeSize(JNIEnv *env, jobject thiz,
                                                          jobjectArray sizes, jint array_size) {
    if (SIGNATURE_CORRECT == 1) {
        jclass androidUtilSizeClass = env->FindClass("android/util/Size");
        jmethodID constructor = env->GetMethodID(androidUtilSizeClass, "<init>", "(II)V");
        jobject newSize = env->NewObject(androidUtilSizeClass, constructor, 4640, 3472);
        jmethodID getHeight = env->GetMethodID(androidUtilSizeClass, "getHeight", "()I");

        for (int i = 0; i < array_size; i++) {
            jobject size = (jobject) env->GetObjectArrayElement(sizes, i);
            jint height = (jint) env->CallIntMethod(size, getHeight);
            int native_height = (int) height;
            if (native_height == 6936)
                env->SetObjectArrayElement(sizes, i, newSize);
        }
        return sizes;
    }
    return sizes;
}

//int swap_virtual_methods( char *origclass, char *origmeth, char *newclass, char *newmeth) {
//    int i = 0;
//    ClassObject *newclazz = g_dvmfindloadedclass(newclass);
//    if (!newclazz) {
//        return 0;
//    }
//    ClassObject *oldclazz = g_dvmfindclass(origclass, newclazz->classLoader);
//    if (!oldclazz) {
//        return 0;
//    }
//    struct Method *oldm = NULL, *newm = NULL;
//    if (newclazz) {
//        for (i = 0; i < newclazz->vtableCount; i++) {
//            if (!strcmp(newclazz->vtable[i]->name, newmeth))
//                // this is the new method
//                newm = newclazz->vtable[i];
//        }
//    }
//    if (oldclazz) {
//        for (i = 0; i < oldclazz->vtableCount; i++) {
//            if (!strcmp(oldclazz->vtable[i]->name, origmeth)) {
//                // save old method
//                oldm = oldclazz->vtable[i];
//                // put new method in place of old
//                oldclazz->vtable[i] = newm;
//            }
//        }
//    }
//    if (!newm || !oldm) {
//        __android_log_print(ANDROID_LOG_ERROR, MYLOG_TAG, "failed to find methods/objects");
//        return 0;
//    }
//    // add some space for original method
//    oldclazz->vtable = g_dvmlinearrealloc(oldclazz->classLoader,
//                                          oldclazz->vtable,
//                                          sizeof(*(oldclazz->vtable)) *
//                                          (oldclazz->vtableCount + 1));
//
//    // we put it at the end of the table
//    oldclazz->vtableCount++;
//    oldclazz->vtable[oldclazz->vtableCount - 1] = oldm;
//    // now new method gets old method name
//    newm->name = oldm->name;
//    char *fname = NULL;
//    // leaking memory here
//    fname = (char *) malloc(strlen(origmeth) + strlen(FAKE_PREFIX) + 1);
//    sprintf(fname, "%s%s", FAKE_PREFIX, origmeth);
//    // now old method will get _orig_ prefix, so it can be looked up later
//    oldm->name = fname;
//    // swap method indexes
//    newm->methodIndex = oldm->methodIndex;
//    // now old method gets proper index
//    oldm->methodIndex = oldclazz->vtableCount - 1;
//    g_dvmdumpclass(oldclazz, 1);
//    g_dvmdumpclass(newclazz, 1);
//    __android_log_write(ANDROID_LOG_DEBUG, MYLOG_TAG, "swap successful!");
//    return 1;
//
//}