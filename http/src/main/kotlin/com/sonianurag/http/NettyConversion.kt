package com.sonianurag.http

import com.sonianurag.buf.Buf
import com.sonianurag.buf.ByteArrayBuf
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.flow.Flow

internal fun ByteBuf.toBuf(): Buf {
  return try {
    val byteArray = ByteArray(this.readableBytes())
    this.readBytes(byteArray)
    ByteArrayBuf.create(byteArray)
  } finally {
    ReferenceCountUtil.release(this)
  }
}

internal fun Buf.toByteBuf(ctx: ChannelHandlerContext): ByteBuf {
  val buffer = ctx.alloc().ioBuffer(this.length)
  buffer.writerIndex(this.length)
  this.write(buffer.nioBuffer())
  return buffer
}

internal fun HttpRequest.toKotlinRequest(body: Flow<Buf>): Request {
  val version =
      when (val v = this.protocolVersion()) {
        HttpVersion.HTTP_1_1 -> Version.Http11
        HttpVersion.HTTP_1_0 -> Version.Http10
        else -> throw Error("Unsupported http version: $v")
      }
  val method = this.method().name().toHttpMethod()
  val path = this.uri()
  val headers = Headers()
  this.headers().forEach { entry -> headers.add(entry.key, entry.value) }
  return object : Request {
    override val version: Version = version
    override val method: Method = method
    override val path: String = path
    override val headers: Headers = headers
    override val reads: Flow<Buf> = body
  }
}

internal fun Version.toNettyVersion(): HttpVersion {
  return when (this) {
    Version.Http11 -> HttpVersion.HTTP_1_1
    Version.Http10 -> HttpVersion.HTTP_1_0
  }
}

internal fun Response.toNettyResponse(): HttpResponse {
  val nettyResponse =
      DefaultHttpResponse(this.version.toNettyVersion(), HttpResponseStatus.valueOf(this.status))
  this.headers.forEach { entry -> nettyResponse.headers().add(entry.key, entry.value) }
  return nettyResponse
}
