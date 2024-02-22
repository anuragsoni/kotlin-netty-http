plugins { `java-library` }

dependencies {
  implementation(libs.netty.all)
  implementation(libs.netty.iouring)
  implementation(libs.kotlinx.coroutines)

  testImplementation(libs.kotlin.junit5)
  testImplementation(libs.jqwik.kotlin)
  testRuntimeOnly(libs.junit.launcher)
}
