#include "com_example_chapter14_JniTest.h"
#include <stdio.h>

JNIEXPORT jstring JNICALL Java_com_example_chapter14_JniTest_get
  (JNIEnv *, jobject){
  printf("invoke get in c++\n");
  return env->NewStringUTF("Hello from JNI!");
  }


JNIEXPORT void JNICALL Java_com_example_chapter14_JniTest_set
(JNIEnv *, jobject, jstring){
    printf("invoke set from C++\n");
    char* str=(char*)env->GetStringUTFChars(string,NULL);
    printf("%s\n",str);
    env->ReleaseStringUTFChars(string,str);
}