plugins { application }

dependencies {
  implementation(project(":http"))
  implementation(libs.kotlinx.coroutines)
}

application { mainClass.set("MainKt") }
