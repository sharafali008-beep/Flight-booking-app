package com.skybook

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application class. @HiltAndroidApp turns on Hilt dependency injection for the whole app. */
@HiltAndroidApp
class SkyBookApp : Application()
