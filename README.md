UnityGCMPlugin
==============

This is Unity plugin for GCM(Google Cloud Messaging) Service

Demo App is available. Please see following repository.

* [snaka/UnityGCMPluginDemo](https://github.com/snaka/UnityGCMPluginDemo)

# Install

## Step1. Download jar file

* [unitygcmplugin.jar](https://github.com/snaka/UnityGCMPlugin/blob/master/unitygcmplugin/release/unitygcmplugin.jar)

## Step2. Copy jar file to your project

Copy unitygcmplugin.jar to `{YourUnityProject}/Assets/Plugins/Android` folder.


## Step3. Copy other dependant jar files to your project

Copy following dependant jar files to `{YourUnityProject}/Assets/Plugins/Android` folder too.

* android-support-v4.jar (from {AndroidSDK}/sdk/extras/android/support/v7/appcompat/libs/android-support-v4.jar)
* google-play-services.jar (from {AndroidSDK}/sdk/extras/google/google_play_services/libproject/google-play-services_lib/libs/google-play-services.jar)

## Step4. Edit AndroidManifest.xml

Create AndroidManifest.xml under `{YourUnityProject}/Assets/Plugins/Android` like following.

NOTE: You have to replace `"!{Your project number}"` to your own project number on Google Developer Console. (Leading "!" is necessary)

```
<?xml version="1.0" encoding="utf-8"?>
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    package="info.snaka.unitygcmplugin.demo"
	android:installLocation="preferExternal"
	android:theme="@android:style/Theme.NoTitleBar"
    android:versionCode="1"
    android:versionName="1.0">
    <supports-screens
        android:smallScreens="true"
        android:normalScreens="true"
        android:largeScreens="true"
        android:xlargeScreens="true"
        android:anyDensity="true"/>
        
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <permission android:name="info.snaka.unitygcmplugin.permission.C2D_MESSAGE" android:protectionLevel="signature" />
    <uses-permission android:name="info.snaka.unitygcmplugin.permission.C2D_MESSAGE" />
    
    <application
		android:icon="@drawable/app_icon"
        android:label="@string/app_name"
        android:debuggable="true">
        
        <activity android:name="info.snaka.unitygcmplugin.CustomUnityPlayerActivity"
            android:label="@string/app_name"
            android:configChanges="fontScale|keyboard|keyboardHidden|locale|mnc|mcc|navigation|orientation|screenLayout|screenSize|smallestScreenSize|uiMode|touchscreen">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
            <meta-data android:name="unityplayer.UnityActivity" android:value="true" />
            <meta-data android:name="unityplayer.ForwardNativeEventsToDalvik" android:value="false" />
        </activity>
	    
        <receiver android:name="info.snaka.unitygcmplugin.GcmBroadcastReceiver" android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="info.snaka.unitygcmplugin" />
            </intent-filter>
        </receiver>

        <service android:name="info.snaka.unitygcmplugin.GcmIntentService" />

        <meta-data android:name="apiProjectNumber" android:value="!{Your project number}" />
    </application>
</manifest>
```

# Usage


## Create the "_GcmEvent" 

Create the empty GameObject named "_GcmEvent" in scene.

## Attach script to "_GcmEvent"

Then attach following script to "_GcmEvent".

```
using System.Collections;

public class GcmEvents : MonoBehaviour {
	string m_regid = "";

	/// <summary>
	/// Get registration ID from cache.
	/// </summary>
	void Start () {
		Debug.Log ("***** Start UnityGCMPluginSample");
		var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		var activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
		var appContext = activity.Call<AndroidJavaObject>("getApplicationContext");
		
		Debug.Log ("***** get RegistrationId");
		var registrar = new AndroidJavaClass ("info.snaka.unitygcmpluginlib.GcmRegistrar");
		
		registrar.CallStatic ("clearCache", new object[] { appContext });
		
		string registrationId = registrar.CallStatic<string> ("getRegistrationId", new object[] { appContext });
		
		if (!string.IsNullOrEmpty (registrationId))
		{
			Debug.Log ("***** RegistrationId:[" + registrationId + "]");
		}
		else
		{
			// Invoke background thread to get registration ID
			Debug.Log ("***** id empty");
			registrar.CallStatic("registerInBackground", new object[]{ appContext });
		}
	}
	
	
	/// <summary>
	/// Callback from background thread if register completed.
	/// </summary>
	/// <param name="registerId">Registration ID</param>
	public void OnRegister(string registerId) {
		Debug.Log ("##### RegisterId: " + registerId);
		m_regid = registerId;
	}


	/// <summary>
	/// Display registration ID.
	/// </summary>
	void OnGUI() {
		GUILayout.TextArea(m_regid, GUILayout.ExpandWidth(true));
	}
}
```

# LICENSE

The MIT License (MIT)

