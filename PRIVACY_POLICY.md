# OV-fiets Beschikbaarheid: Privacy policy

This is an open source app. See [the main page of this repo](https://github.com/cristan/OvFietsBeschikbaarheidApp/tree/main) for the license.

## Data collected by the app

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information. Anything that is remembered from the user is stored locally in your device only, and can be erased by clearing the app's data. No analytics software is present in the app either at the time of writing.

### Explanation of permissions requested in the Android version of the app

The list of permissions required by the Android version of the app can be found in the [AndroidManifest.xml](https://github.com/cristan/OvFietsBeschikbaarheidApp/blob/main/composeApp/src/androidMain/AndroidManifest.xml) file:

<br/>

|                 Permission                  | Why it is required                                                                                                                                                                                                     |
|:-------------------------------------------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        `android.permission.Internet`        | Needed to access up-to-date data from the internet                                                                                                                                                                     |
| `android.permission.ACCESS_COARSE_LOCATION` | This allows you to quickly see OV-fiets locations near your position. Your location is not stored by this app at all.                                                                                                  |
|  `android.permission.ACCESS_FINE_LOCATION`  | I wanted to use ACCESS_COARSE_LOCATION only since I don't need an exact location, but then GPS is _never_ used and this often leads to no, or very slow location retrieval. I ask for a fine location to prevent this. |

### Explanation of permissions requested in the iOS version of the app

|        Permission         | Why it is required                                                                                                    |
|:-------------------------:|-----------------------------------------------------------------------------------------------------------------------|
| GPS location while in use | This allows you to quickly see OV-fiets locations near your position. Your location is not stored by this app at all. |

<hr style="border:1px solid gray">

If you find any problems with this app or have any questions regarding privacy or otherwise, don't hesitate to create a ticket at https://github.com/cristan/OvFietsBeschikbaarheidApp/issues.