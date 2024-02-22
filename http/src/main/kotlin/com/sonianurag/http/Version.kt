package com.sonianurag.http

sealed class Version {
  data object Http10 : Version()

  data object Http11 : Version()
}
