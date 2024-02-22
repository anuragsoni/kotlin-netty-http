package com.sonianurag.http

import com.sonianurag.buf.Buf
import com.sonianurag.buf.encodeToBuf
import io.netty.handler.codec.http.HttpHeaderNames
import java.nio.charset.Charset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface Response {
  val status: Int
  val headers: Headers
  val version: Version
  val reads: Flow<Buf>
}

fun String.toResponse(
    charset: Charset = Charsets.UTF_8,
    status: Int = 200,
    headers: Headers = Headers(),
    version: Version = Version.Http11
): Response {
  headers.add(HttpHeaderNames.CONTENT_LENGTH, this.length)
  val body = this.encodeToBuf(charset)
  return object : Response {
    override val status: Int = status
    override val headers: Headers = headers
    override val version: Version = version
    override val reads: Flow<Buf> = flow { emit(body) }
  }
}

fun Buf.toResponse(
    status: Int = 200,
    headers: Headers = Headers(),
    version: Version = Version.Http11
): Response {
  headers.add(HttpHeaderNames.CONTENT_LENGTH, this.length)
  val body = this
  return object : Response {
    override val status: Int = status
    override val headers: Headers = headers
    override val version: Version = version
    override val reads: Flow<Buf> = flow { emit(body) }
  }
}

fun Flow<Buf>.toResponse(
    status: Int = 200,
    headers: Headers = Headers(),
    version: Version = Version.Http11
): Response {
  val body = this
  return object : Response {
    override val status: Int = status
    override val headers: Headers = headers
    override val version: Version = version
    override val reads: Flow<Buf> = body
  }
}

fun Flow<String>.toResponse(
    charset: Charset = Charsets.UTF_8,
    status: Int = 200,
    headers: Headers = Headers(),
    version: Version = Version.Http11
): Response {
  val body = this.map { it.encodeToBuf(charset) }
  return object : Response {
    override val status: Int = status
    override val headers: Headers = headers
    override val version: Version = version
    override val reads: Flow<Buf> = body
  }
}
