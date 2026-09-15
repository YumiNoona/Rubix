plugins { kotlin("jvm") }
kotlin { jvmToolchain(17) }
sourceSets.main { java.srcDir("../vendor/min2phase/src");java.srcDir("../vendor/threephase/src") }
dependencies {
 implementation("org.slf4j:slf4j-api:2.0.17")
 testImplementation("org.slf4j:slf4j-nop:2.0.17")
 testImplementation(kotlin("test")); testImplementation("junit:junit:4.13.2")
}
