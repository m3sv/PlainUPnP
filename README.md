# Rewrite of UI layer is in progress


Description
===========

DroidUPnP is an UPnP control point application for android.

DroidUPnP discovers your home UPnP device, content provider and renderer.
It allows you to browse your UPnP content directory, select the media you want
to use and allows you to play it on your connected television or any UPnP renderer
compatible device.

It also allows you to use your android device as an UPnP content provider.

It is licensed under the **GPLv3**.

BUILD
=====

Application can be build using [gradle](http://www.gradle.org).

To generate the apk :

	gradle build

To install on your android device via adb :

	gradle installDebug

Dependencies
============

The main dependence of DroidUPnP is **Cling** which is the a
UPnP stack made in Java with Android support layer.
Cling is licensed under the **LGPLv2**.
Source code is available at [4thline.org](http://4thline.org/projects/cling/)

Other dependencies are :

 * **NanoHttpd**, under a modify **BSD license**.
   Source code is available on [GitHub](https://github.com/NanoHttpd/nanohttpd).

 * **Licensesdialog**, under the **Apache license, Version 2.0**.
   Source code is available on [GitHub](https://github.com/PSDev/LicensesDialog).

Copying
=======

Copyright (C) 2015, Aurélien Chabot <aurelien@chabot.fr>

Licensed under **GPLv3**

See [COPYING](https://github.com/trishika/DroidUPnP/blob/master/COPYING).
