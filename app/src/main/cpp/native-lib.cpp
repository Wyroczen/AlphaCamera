//
// Created by wisni on 20.09.2020.
//

#include <jni.h>
#include <string>
#include <iostream>

using std::string;
using std::cout;
using std::endl;

void print_welcome_message(){
    cout<<"Hello from shared library!"<<endl;
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

    const char* test = "Hello from the lib!";
    return env->NewStringUTF(test);
}