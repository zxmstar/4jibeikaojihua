plugins {
    id("com.android.application")
}

android {
    namespace = "com.zxmstar.cet4checkin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zxmstar.cet4checkin"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
