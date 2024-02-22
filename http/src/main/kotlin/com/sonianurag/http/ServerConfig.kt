package com.sonianurag.http

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ServerConfig {
  var backlog: Int = 128
  var acceptorLoopThreads: Int = 1
  var workerThreads: Int? = null
  var tcpNoDelay: Boolean = true
  var connectTimeout: Duration? = null
  var readTimeout: Duration = 15.seconds
  var keepAlive: Boolean? = null
  var receiveBufferSize: Int? = 65536
  var sendBufferSize: Int? = 65536
  var reuseAddress: Boolean? = null
  var transport: Transport? = null
}
