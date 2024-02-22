package com.sonianurag.http

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

internal class HttpServerHandler(
    channel: Channel,
    private val handler: suspend (Request) -> Response
) : ChannelInboundHandlerAdapter(), CoroutineScope {
  override val coroutineContext: CoroutineContext = channel.eventLoop().asCoroutineDispatcher()

  @Deprecated("Deprecated in Java")
  override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
    when (cause) {
      is IOException -> ctx?.close()
      else -> {
        ctx?.close()
        cause?.printStackTrace()
      }
    }
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is HttpRequest -> {
        val bodyHandler = BodyHandler(ctx.channel())
        val bodyDrained = bodyHandler.bodyHandlerRemoved
        ctx.pipeline()
            .addAfter(
                PipelineStages.HTTP_REQUEST_HANDLER,
                PipelineStages.HTTP_REQUEST_BODY_HANDLER,
                bodyHandler)

        val request = msg.toKotlinRequest(bodyHandler.reads)

        launch {
          val response = handler(request)
          val nettyResponse = response.toNettyResponse()
          ctx.write(nettyResponse)

          response.reads
              .mapNotNull { buf ->
                if (buf.isEmpty()) {
                  null
                } else {
                  DefaultHttpContent(buf.toByteBuf(ctx))
                }
              }
              .flowOn(ctx.executor().asCoroutineDispatcher())
              .collect { content -> ctx.writeAndFlush(content).coAwait() }

          ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).coAwait()
          bodyDrained.await()
          ctx.read()
        }
      }
      is HttpContent -> ctx.fireChannelRead(msg)
      else -> ctx.fireExceptionCaught(Error("Unexpected message type ${msg::class.java.name}"))
    }
  }
}
