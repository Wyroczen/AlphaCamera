#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include "Liberation.h"
#include "Patch.h"

char const *getGreetings = "number";

void doHook()
{
    void *imagehandle = dlopen("libnative-lib.so", RTLD_GLOBAL | RTLD_NOW);
    void *pGetGreetings = dlsym(imagehandle, getGreetings);

    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "DoHook imagehandle (hex): %zx\n", (size_t) imagehandle); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "DoHook pGetGreetings (hex): %zx\n", (size_t) pGetGreetings); //as hex


    char *errstr;
    errstr = dlerror();
    if (errstr != NULL)
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "A dynamic linking error occurred: (%s)\n", errstr);

    Patch *gold = Patch::Setup(pGetGreetings, "38467047"); // MOV R0, R7; BX LR; -> THUMB
    gold->Apply();
}

//jint JNI_OnLoad(JavaVM* vm, void* reserved)
//{
//    jint result = -1;

 //   doHook();

//    result = JNI_VERSION_1_4;
//    bail:
//    return result;
//}

void monkey_patch(void * sym, void * jump_target, int offset=0) {
    static int PAGE_SIZEz = 0;
    if (PAGE_SIZEz == 0) PAGE_SIZEz = getpagesize();
    void * page = (void*)((uintptr_t)sym & (uintptr_t)~(PAGE_SIZEz-1));
    //struct {
    //    unsigned char jmp_qword_ptr_rip[6];
    //    uint64_t addr;
    //} __attribute__((packed)) asm_jmp_abs = {
    //        {0xff, 0x25, 0, 0, 0, 0}, (uint64_t)jump_target
    //};
    struct {
        unsigned char jmp_qword_ptr_rip[6];
        uint64_t addr;
    } __attribute__((packed)) asm_jmp_abs = {
            {0xff, 0x25, 0, 0, 0, 0}, (uint64_t)jump_target
    };

    mprotect(page, 2 * PAGE_SIZEz, PROT_WRITE);
    void * target = (void*)((uintptr_t)sym + offset);

    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey Writing to memory!");
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey Memory Write target (hex): %zx\n", (size_t) target); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey Memory Write jump_target (hex): %zx\n", (size_t) jump_target); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey Memory Write length (hex): %zd\n", sizeof asm_jmp_abs); //as signed decimal

    memcpy(target, &asm_jmp_abs, sizeof asm_jmp_abs);
    mprotect(page, 2 * PAGE_SIZEz, PROT_READ | PROT_EXEC);
}

void monkey_patch(const char * function, void * jump_target, int offset=0) {
    static void * handle = 0;
    //if (handle == 0) handle = dlopen("libliberation-lib.so", RTLD_LAZY);
    if(handle == 0) handle = dlopen("libnative-lib.so", RTLD_GLOBAL | RTLD_NOW);
    void * sym = dlsym(handle, function);

    char *errstr;
    errstr = dlerror();
    if (errstr != NULL)
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "A dynamic linking error occurred: (%s)\n", errstr);

    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey handle (hex): %zx\n", (size_t) handle); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey sym (hex): %zx\n", (size_t) sym); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey jump target (hex): %zx\n", (size_t) jump_target); //as hex
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Monkey patch start");
    monkey_patch(sym, jump_target, offset);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wyroczen_alphacamera_jni_HookerLibJNI_helloWrapper(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "WRAPPER USED!");
}

extern "C"
void hello_wrapper(){
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "WRAPPER USED!");
}

//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_wyroczen_alphacamera_jni_NativeLibJNI_getWelcome(JNIEnv *env, jobject thiz) {
//
//    const char *test = "Hello from the lib!";
//    //const char signature = *getSignature(env);
//    return env->NewStringUTF(test);
//    return getSignature(env);
//}


extern "C"
JNIEXPORT void JNICALL
Java_com_wyroczen_alphacamera_jni_HookerLibJNI_printHelloInLog(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Hello");
}

void make_patch(void * sym) {

    size_t len = 4;
    int offset = 12;
    void * target = (void*)((uintptr_t)sym + offset);

    uint8_t *orig = new uint8_t[len];
    memcpy((void*) orig, (void*) target, len);

    for(int i = 0; i < 4; i++){
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory have bytes: (hex): %02x\n", orig[i] ); //as hex
    }

    static int PAGE_SIZEz = 0;
    if (PAGE_SIZEz == 0) PAGE_SIZEz = getpagesize();
    void *page = (void *) ((uintptr_t) sym & (uintptr_t) ~(PAGE_SIZEz - 1));

    mprotect(page, 2 * PAGE_SIZEz, PROT_WRITE);



    size_t num = 3; //my code to force insert 3 because I want to replace 5 from lib
    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory Write bytes NUM addr (hex): %zx\n", (size_t) &num); //as hex
    //mod to read hex from lib

//    uint8_t *orig = new uint8_t[len];
//    memcpy((void*) orig, (void*) target, len);

//    for(int i = 0; i < 4; i++){
//        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory have bytes: (hex): %02x\n", orig[i] ); //as hex
//    }

    //std::string data = "68008052";
    //const char *tempHex = data.c_str();
    //size_t patchSize = data.length() / 2;
    //uint8_t *patchData = new uint8_t[patchSize];
    /* convert string to hex array */
    //int n;
    //for (int i = 0; i < patchSize; i++) {
    //    sscanf(tempHex + 2 * i, "%2X", &n);
    //    patchData[i] = (unsigned char)n;
    //    __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "HEX char: %u", patchData[i]);
    //}
    struct {
        unsigned char jmp_qword_ptr_rip[4];
    } __attribute__((packed)) patchData = {
            //{0x68, 0x0, 0x80, 0x52}
            {0x28, 0x01, 0x80, 0x52}
    };
    memcpy((void*) target, &patchData, len); //original


//    uint8_t *after = new uint8_t[len];
//    memcpy((void*) after, (void*) target, len);

//    for(int i = 0; i < 4; i++){
//        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory have bytes after: (hex): %02x\n", after[i] ); //as hex
//    }



    mprotect(page, 2 * PAGE_SIZEz, PROT_READ | PROT_EXEC);

    uint8_t *after = new uint8_t[len];
    memcpy((void*) after, (void*) target, len);

    for(int i = 0; i < 4; i++){
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "Memory have bytes after: (hex): %02x\n", after[i] ); //as hex
    }
}

char const *symbol = "print_welcome_message";
void ram_patcher(){
    void *imagehandle = dlopen("libnative-lib.so", RTLD_GLOBAL | RTLD_NOW);
    void *pSymbol = dlsym(imagehandle, symbol);
    make_patch(pSymbol);
    dlclose(imagehandle);

    char *errstr;
    errstr = dlerror();
    if (errstr != NULL)
        __android_log_print(ANDROID_LOG_ERROR, "AlphaCamera-Liberation", "A dynamic linking error occurred: (%s)\n", errstr);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_wyroczen_alphacamera_jni_HookerLibJNI_doHook(JNIEnv *env, jobject thiz) {
    //doHook();
    //monkey_patch("Java_com_wyroczen_alphacamera_jni_NativeLibJNI_printWelcome", (void*)&hello_wrapper);//Java_com_wyroczen_alphacamera_jni_HookerLibJNI_helloWrapper);//(void*)&hello_wrapper);
    ram_patcher();
}