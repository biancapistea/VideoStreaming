Video Streaming App

1. First, you need to set up the project by following these steps:
   - You need to create an account for LiveSwitch cloud: https://www.frozenmountain.com/signup-liveswitch-cloud
   - In the LiveSwitch cloud, you need to go to Downloads to download the LiveSwitch SDK for Android
   - Create a new Android Studio project.
   - In the app folder, create a libs folder.
   - Add the following jar files to your project to provide your app with the minimum set of Opus and VPX capabilities:
fm.liveswitch.jar
fm.liveswitch.opus.jar
fm.liveswitch.vpx.jar
fm.liveswitch.yuv.jar
fm.liveswitch.openh264.jar
fm.liveswitch.android.jar

Add architecture-specific native libraries to your Android project's app/src/main folder. These libraries are in the Android/Libraries/jniLibs/arm64-v8a, Android/Libraries/jniLibs/armebi-v7a and Android/Libraries/jniLibs/x86 folders. Do the following:

- Add the entire jniLibs folder into your project directory.
- In your project non-source files, open app/build.gradle.
- In app/build.gradle, add the following to the end of the android block:
packagingOptions {
            exclude 'lib/linux_armv7/*'
            exclude 'lib/linux_armv8/*'
            exclude 'lib/linux_arm64/*'
            exclude 'lib/linux_x64/*'
            exclude 'lib/linux_x86/*'
            exclude '**/libaudioprocessingfm.so'
            exclude '**/libopenh264fm.so'
            exclude '**/libopusfm.so'
            exclude '**/libvpxfm.so'
            exclude '**/libyuvfm.so'
        }
sourceSets {
            main {
                    jniLibs {
                        srcDirs = [
                                'src/main/jniLibs'
                        ]
                    }
            }
        }
lintOptions {
            abortOnError false
        }
  
- In app/build.gradle, add the following to the dependencies block:
    implementation fileTree(include: ["*.jar"], dir: "libs")

- Resync your Gradle project.

- Add the following permissions:
You must add the following permissions to your manifest:

<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
<uses-permission android:name="android.permission.BLUETOOTH" />
<!-- largeHeap is necessary to avoid OOM errors on older devices -->
<application android:label="..." android:theme="..." android:name="android.support.multidex.MultiDexApplication" android:largeHeap="true"></application>
