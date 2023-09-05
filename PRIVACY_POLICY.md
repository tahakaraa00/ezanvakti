## KIBLEGAH: Privacy policy

Welcome to the KIBLEGAH Qibla and Prayer Times app for Android!

This is an open source Android app developed by Wrichik Basu. The source code is available on GitHub under the MIT license; the app is also available on Google Play.

As an avid Android user myself, I take privacy very seriously.
I know how irritating it is when apps collect your data without your knowledge.

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information. All data (app preferences (like theme, etc.) and alarms) created by the you (the user) is stored on your device only, and can be simply erased by clearing the app's data or uninstalling it.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/WrichikBasu/ShakeAlarmClock/blob/1031bad5edd2e73eda091cd1e84746f4710c7528/app/src/main/AndroidManifest.xml#L7-L15

<br/>

| Permission | Why it is required |
| :---: | --- |
| `android.permission.ACCESS_FINE_LOCATION` | This is required to schedule an qibla and prayer times, and was introduced in Android 12. You, as the user, or the system, can revoke this permission at any time from Settings. Revoking this permission will, however, kill the app immediately if it was alive, and cancel all qibla and prayer times set by the app. |
| `android.permission.ACCESS_COARSE_LOCATION` | Required to qibla and prayer times. Permission automatically granted by the system; can't be revoked by user. |
| `android.permission.INTERNET`  | Required to qibla and prayer times. Permission automatically granted by the system; can't be revoked by user. |
| `android.permission.ACCESS_BACKGROUND_LOCATION` | Enables the app to create background services that will qibla and prayer times. Permission automatically granted by the system; can't be revoked by user. |
| `android.hardware.sensor.accelerometer` | Required to set alarms, whether exact or inexact. Permission automatically granted by the system; can't be revoked by user. |
| `android.hardware.sensor.compass` | The only sensitive permission that the app requests, and can be revoked by the system or the user at any time.  |


 <hr style="border:1px solid gray">

If you find any security vulnerability that has been inadvertently caused by me, or have any question regarding how the app protectes your privacy, please send me an email or post a discussion on GitHub, and I will surely try to fix it/help you.

Yours sincerely,  
Zübeyir Yalha Karakuş 
Eskişehir, Turkey.  
tahakaraa00@gmail.com
