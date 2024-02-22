package com.sonianurag.http

import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.incubator.channel.uring.IOUring
import io.netty.incubator.channel.uring.IOUringEventLoopGroup
import io.netty.incubator.channel.uring.IOUringServerSocketChannel
import io.netty.util.concurrent.DefaultThreadFactory
import kotlin.reflect.KClass

interface Transport {
  val serverSocketChannelKClass: KClass<out ServerSocketChannel>

  fun isAvailable(): Boolean

  fun eventLoopGroup(threadCount: Int, daemon: Boolean, threadPoolName: String): EventLoopGroup
}

class EpollTransport : Transport {

  init {
    require(Epoll.isAvailable()) { "Epoll transport isn't available on the host system." }
  }

  override val serverSocketChannelKClass: KClass<out ServerSocketChannel> =
      EpollServerSocketChannel::class

  override fun isAvailable(): Boolean {
    return Epoll.isAvailable()
  }

  override fun eventLoopGroup(
      threadCount: Int,
      daemon: Boolean,
      threadPoolName: String
  ): EventLoopGroup {
    val threadFactory = DefaultThreadFactory(threadPoolName, true)
    return EpollEventLoopGroup(threadCount, threadFactory)
  }
}

class NioTransport : Transport {
  override val serverSocketChannelKClass: KClass<out ServerSocketChannel> =
      NioServerSocketChannel::class

  override fun isAvailable(): Boolean {
    return true
  }

  override fun eventLoopGroup(
      threadCount: Int,
      daemon: Boolean,
      threadPoolName: String
  ): EventLoopGroup {
    val threadFactory = DefaultThreadFactory(threadPoolName, true)
    return NioEventLoopGroup(threadCount, threadFactory)
  }
}

class IOUringTransport : Transport {

  init {
    require(IOUring.isAvailable()) { "IOUring transport isn't available on the host system." }
  }

  override val serverSocketChannelKClass: KClass<out ServerSocketChannel> =
      IOUringServerSocketChannel::class

  override fun isAvailable(): Boolean {
    return IOUring.isAvailable()
  }

  override fun eventLoopGroup(
      threadCount: Int,
      daemon: Boolean,
      threadPoolName: String
  ): EventLoopGroup {
    val threadFactory = DefaultThreadFactory(threadPoolName, true)
    return IOUringEventLoopGroup(threadCount, threadFactory)
  }
}

fun initializeTransport(): Transport {
  return when {
    Epoll.isAvailable() -> EpollTransport()
    IOUring.isAvailable() -> IOUringTransport()
    else -> NioTransport()
  }
}
