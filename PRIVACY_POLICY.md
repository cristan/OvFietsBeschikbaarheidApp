## OV-fiets Beschikbaarheid: Privacy policy

This is an open source app. See the main page of this repo for the license.

### Data collected by the app

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information. At the time of writing, no data is collected at all, but when features like preferences will be added, they will be stored locally in your device only, and can be simply erased by clearing the app's data. No analytics software is present in the app either at the time of writing.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/cristan/OvFietsBeschikbaarheidApp/blob/main/app/src/main/AndroidManifest.xml
<br/>

|                 Permission                  | Why it is required                                                                                                                                                                                                       |
|:-------------------------------------------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        `android.permission.Internet`        | Needed to access up-to-date data from the internet                                                                                                                                                                       |
| `android.permission.ACCESS_COARSE_LOCATION` | Needed to see locations nearby. This allows you to quickly see locations nearby. Your location is not stored by this app at all.                                                                                         |
|  `android.permission.ACCESS_FINE_LOCATION`  | I wanted to use ACCESS_COARSE_LOCATION only since I don't need an exact location, but then GPS is _never_ used and this often leads to no, or very slow location retrieval. That's why I ask for a fine location as well |


<hr style="border:1px solid gray">

If you find any problems with this app or have any questions regarding privacy or otherwise, don't hesitate to contact me. You can do so by creating a ticket here or contacting me through the play store.