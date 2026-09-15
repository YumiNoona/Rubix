plugins { id("com.android.application"); kotlin("android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "com.cubeguide"; compileSdk = 36
 defaultConfig { applicationId = "com.cubeguide"; minSdk = 26; targetSdk = 36; versionCode = 7; versionName = "1.3.0"; ndk { abiFilters += (providers.gradleProperty("testAbi").orNull?.let { listOf(it) } ?: listOf("arm64-v8a", "armeabi-v7a")) }; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }

 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
 implementation(project(":core"))
 implementation(platform("androidx.compose:compose-bom:2026.03.00"))
 implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.activity:activity-compose:1.12.4")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
 implementation("androidx.camera:camera-camera2:1.6.2")
 implementation("androidx.camera:camera-lifecycle:1.6.2")
 implementation("androidx.camera:camera-view:1.6.2")
 implementation("androidx.exifinterface:exifinterface:1.4.2")
 implementation("org.opencv:opencv:4.13.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
 testImplementation(kotlin("test"))
 androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.00"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 androidTestImplementation("androidx.test:runner:1.7.0")
 androidTestImplementation("androidx.test.ext:junit:1.3.0")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
