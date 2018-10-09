#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_demo_android_ss_com_floatbubbleapplication_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
