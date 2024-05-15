****Video Streaming App****

1. First, you need to set up the project by following these steps:
   - You need to create an account for LiveSwitch cloud: https://www.frozenmountain.com/signup-liveswitch-cloud
   - In the LiveSwitch cloud, you need to go to Downloads to download the LiveSwitch SDK for Android
   - Create a new Android Studio project.
   - In the app folder, create a libs folder.
   - Add the following jar files to your project to provide your app with the minimum set of Opus and VPX capabilities:
      ```
      fm.liveswitch.jar
      fm.liveswitch.opus.jar
      fm.liveswitch.vpx.jar
      fm.liveswitch.yuv.jar
      fm.liveswitch.openh264.jar
      fm.liveswitch.android.jar
       ```
   - Add architecture-specific native libraries to your `Android project's app/src/main` folder. These libraries are in the `Android/Libraries/jniLibs/arm64-v8a`, `Android/Libraries/jniLibs/armebi-v7a` and `Android/Libraries/jniLibs/x86 folders`. Do the following:
      - Add the entire jniLibs folder into your project directory.
      - In your project non-source files, open app/build.gradle.
      - In app/build.gradle, add the following to the end of the android block:
    
         ```
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
         ```
  
   - In app/build.gradle, add the following to the dependencies block:
   
     `implementation fileTree(include: ["*.jar"], dir: "libs")`
   - Resync your Gradle project.
   - Add the following permissions to your manifest file:

      ```
      <uses-permission android:name="android.permission.INTERNET"/>
      <uses-permission android:name="android.permission.CAMERA"/>
      <uses-permission android:name="android.permission.RECORD_AUDIO"/>
      <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
      <uses-permission android:name="android.permission.BLUETOOTH" />
      <application android:label="..." android:theme="..." android:name="android.support.multidex.MultiDexApplication" android:largeHeap="true"></application>
      ```

2. Then, create the following classes: **CameraLocalMedia**, intended for managing local camera in an Android application.
3. Create the **LocalMedia** class, intended for managing local audio
4. Create the **AecContext** for audio echo cancellation
5. Create **VideoLogic** class for managing video-related operations (it's a controller for video communication functionalities in an Android app):
   - It handles client registration and connection to a video communication service.
   - Provides methods for joining and leaving video channels asynchronously.
   - Manages local and remote video streams, including starting and stopping local video capture.
   - Utilizes layout management for arranging video streams within the UI.
   - Implements audio echo cancellation (AEC) and utilizes audio and video streams for communication.
6. Then, add the UI and the corresponding logic for Main Activity.
7. The **MainActivity** class sets up the main activity of an Android application:
   - Initializes UI elements and sets up button listeners.
   - Manages video playback, toggling remote user video on and off.
   - Checks and requests necessary permissions for audio and camera usage.
   - Handles fragment transactions for UI navigation.
   - Creates and manages the instance of VideoLogic, coordinating video-related operations throughout the app.
8. Create the **StartingFragment** and the UI for it. In Starting Fragment we have the buttons for join and leave the conference.
9. Then, add the logic for drawing.
10. I created a separate file for CustomDrawingView where I put the logic to draw on screen
11. Add the CustomDrawingView to mainactivity.xml
12. Run the app
