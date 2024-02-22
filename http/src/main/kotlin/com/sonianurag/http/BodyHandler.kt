package com.sonianurag.http

import com.sonianurag.buf.Buf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.LastHttpContent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class BodyHandler(channel: io.netty.channel.Channel) :
    ChannelInboundHandlerAdapter(), CoroutineScope {
  private val chan = Channel<Buf>()
  override val coroutineContext: CoroutineContext = channel.eventLoop().asCoroutineDispatcher()
  val bodyHandlerRemoved = CompletableDeferred<Unit>()

  override fun handlerRemoved(ctx: ChannelHandlerContext?) {
    bodyHandlerRemoved.complete(Unit)
  }

  val reads: Flow<Buf> =
      flow {
            while (channel.isOpen && isActive) {
              channel.read()
              try {
                val message = chan.receive()
                emit(message)
              } catch (e: Throwable) {
                when (e) {
                  is ClosedReceiveChannelException -> {
                    return@flow
                  }
                  else -> throw e
                }
              }
            }
          }
          .flowOn(coroutineContext)

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    when (msg) {
      is LastHttpContent -> {
        if (msg == LastHttpContent.EMPTY_LAST_CONTENT) {
          chan.close()
          ctx.pipeline().remove(this)
        } else {
          val buf = msg.content().toBuf()
          val handler = this
          launch {
            chan.send(buf)
            chan.close()
            ctx.pipeline().remove(handler)
          }
        }
      }
      is HttpContent -> {
        val buf = msg.content().toBuf()
        launch { chan.send(buf) }
      }
      else -> {
        throw Error("Expecting to read Http Content but received: ${msg::class.java.name}")
      }
    }
  }
}
