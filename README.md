<p align="center"><img src="https://github.com/m3sv/PlainUPnP/raw/master/app/src/main/ic_launcher-web.png" width="150"></p> 


<h2 align="center"><b>PlainUPnP</b></h2>
<a href="https://play.google.com/store/apps/details?id=com.m3sv.plainupnp">
  <img alt="Android app on Google Play" src="https://play.google.com/intl/en_gb/badges/images/badge_new.png" />
</a>

## Table of contents

* [Description](#description)
* [Technologies used](#technologies-used)

## Description

PlainUPnP - intially DroidUPnP - is an UPnP control point application for android.

PlainUPnP allows you to stream videos, music and photos. 
Browse UPnP media sources and stream media content to a selected UPnP device or play locally. 

Compatible with UPnP/DLNA/Smart TV.

Features: 
* Simple and easy to use interface
* UPnP content streaming
* Dark/Light themes
* Launching UPnP content locally


*In order to stream your content to another device using PlainUPnP, your TV/Android device must have UPnP server installed, such as Kodi/Plex

This is a rewrite of trishika's [DroidUPnP](https://github.com/trishika/DroidUPnP), original project can be found [here](https://github.com/trishika/DroidUPnP).

## Technologies used
* Initially Java, rewritten to Kotlin
* MVVM with LiveData for presentation 
* RxJava/RxKotlin
* Coroutines
* Glide 
* Dagger 2 for dependency injection
* Cling for UPnP functionality
* Firebase Crashlytics
* CircleCI for CI/CD
