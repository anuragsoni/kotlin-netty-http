package com.sonianurag.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.handler.codec.http.HttpDecoderConfig
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.HttpServerExpectContinueHandler
import io.netty.handler.codec.http.HttpServerKeepAliveHandler
import io.netty.handler.flush.FlushConsolidationHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import java.net.BindException
import java.net.SocketAddress
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit

object Http {
  fun server(
      whereToListen: SocketAddress,
      config: ServerConfig.() -> Unit = {},
      handler: suspend (Request) -> Response
  ) {
    val serverConfig = ServerConfig().apply(config)

    require(serverConfig.acceptorLoopThreads > 0) {
      "Number of acceptor threads must be greater than zero"
    }

    serverConfig.workerThreads?.let {
      require(it > 0) { "Number of worker threads must be greater than zero" }
    }

    val transport =
        serverConfig.transport?.let {
          require(it.isAvailable()) { "Transport: ${it::class.java.name} isn't available." }
          it
        } ?: initializeTransport()

    val bossGroup =
        transport.eventLoopGroup(serverConfig.acceptorLoopThreads, true, "shuttle/acceptor")
    val childGroup =
        serverConfig.workerThreads?.let { transport.eventLoopGroup(it, true, "shuttle/worker") }
    val server = ServerBootstrap()

    try {
      when (childGroup) {
        null -> server.group(bossGroup)
        else -> server.group(bossGroup, childGroup)
      }
      server.channel(transport.serverSocketChannelKClass.java)
      server.childOption(ChannelOption.TCP_NODELAY, serverConfig.tcpNoDelay)
      server.option(ChannelOption.SO_BACKLOG, serverConfig.backlog)
      serverConfig.keepAlive?.let { server.childOption(ChannelOption.SO_KEEPALIVE, true) }
      serverConfig.connectTimeout?.let {
        server.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, it.toInt(DurationUnit.MILLISECONDS))
      }
      serverConfig.receiveBufferSize?.let { server.childOption(ChannelOption.SO_RCVBUF, it) }
      serverConfig.sendBufferSize?.let { server.childOption(ChannelOption.SO_SNDBUF, it) }
      serverConfig.reuseAddress?.let { server.option(ChannelOption.SO_REUSEADDR, it) }

      server.childHandler(
          object : ChannelInitializer<Channel>() {
            override fun initChannel(ch: Channel) {
              ch.config().setAutoRead(false)
              val pipeline = ch.pipeline()
              pipeline.addLast(
                  PipelineStages.READ_TIMEOUT,
                  ReadTimeoutHandler(serverConfig.readTimeout.inWholeSeconds, TimeUnit.SECONDS))
              pipeline.addLast(
                  PipelineStages.HTTP_REQUEST_DECODER, HttpRequestDecoder(HttpDecoderConfig()))
              pipeline.addLast(PipelineStages.HTTP_RESPONSE_ENCODER, HttpResponseEncoder())
              pipeline.addLast(
                  PipelineStages.HTTP_SERVER_EXPECT_CONTINUE, HttpServerExpectContinueHandler())
              pipeline.addLast(PipelineStages.HTTP_KEEP_ALIVE, HttpServerKeepAliveHandler())
              pipeline.addLast(PipelineStages.FLUSH_CONSOLIDATION, FlushConsolidationHandler())
              pipeline.addLast(PipelineStages.HTTP_REQUEST_HANDLER, HttpServerHandler(ch, handler))
              ch.read()
            }
          })
      val bindResult = server.bind(whereToListen).awaitUninterruptibly()
      if (!bindResult.isSuccess) {
        throw BindException("Failed to bind to $whereToListen: ${bindResult.cause().message}")
      }
      bindResult.channel().closeFuture().sync()
    } finally {
      childGroup?.shutdownGracefully()?.sync()
      bossGroup.shutdownGracefully().sync()
    }
  }
}
