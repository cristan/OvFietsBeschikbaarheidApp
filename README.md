# OV-fiets beschikbaarheid
[![Build Status](https://github.com/cristan/OvFietsBeschikbaarheidApp/actions/workflows/android_ci.yml/badge.svg)](https://github.com/cristan/OvFietsBeschikbaarheidApp/actions/workflows/android_ci.yml)

[![Kotlin](https://img.shields.io/badge/Kotlin-%20-blue?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-%20-blue?logo=android)](https://developer.android.com/compose)
[![Android](https://img.shields.io/badge/Platform-Android-green)](https://developer.android.com/)
[![Koin](https://img.shields.io/badge/Koin-%20-orange?logo=koin)](https://github.com/InsertKoinIO/koin)
[![Ktor](https://img.shields.io/badge/Ktor-%20-blue?logo=kotlin)](https://ktor.io/)
[![Compass](https://img.shields.io/badge/Compass-%20-F5F5F5)](https://github.com/jordond/compass/)
[![Google Maps](https://img.shields.io/badge/Google%20Maps-%20-blue?logo=google-maps)](https://github.com/googlemaps/android-maps-compose)

This app is designed to find how many OV-fiets bikes are available as quick as possible.

<div align="center">
<img src="./screenshots/screenshot1.png" alt="Screenshot 1" width="45%"> <img src="./screenshots/screenshot2.png" alt="Screenshot 2" width="45%">
</div>

## Features
* See locations nearby via GPS
* Geocoding: see locations nearby the location you've typed
* Show additional information like address, opening hours and other locations at the same station.

## About the code
Data comes from [openOV](https://openov.nl) combined with the total availabilty per station kindly provided by [ovfietsbeschikbaar.nl](https://ovfietsbeschikbaar.nl/).

The code is pretty much using the latest technologies available (at least at the time of writing)
* 100% Jetpack Compose
* Material Design 3
* Libraries like Koin, Compass, and Ktor so it can later easily be converted to Compose Multiplatform
* Gradle Kotlin DSL (`.kts`)

## Contributing
Contributions, comments, and suggestions are welcome! Developing should be quite fun with all the new frameworks.

## License
The license is the GPLv3, except for these exceptions:
* You are writing software unrelated to the OV-fiets
* You are training AI models
* You are writing software for the NS

In these cases, there are no restrictions (the license is CC-0)
