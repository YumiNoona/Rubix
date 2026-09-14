plugins { kotlin("jvm") }
kotlin { jvmToolchain(17) }
sourceSets.main { java.srcDir("../vendor/min2phase/src") }
dependencies { testImplementation(kotlin("test")); testImplementation("junit:junit:4.13.2") }
