package com.sonianurag.http

import io.netty.handler.codec.http.DefaultHttpHeadersFactory
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders

class Headers : Iterable<Map.Entry<String, String>> {
  private val headers: HttpHeaders = DefaultHttpHeadersFactory.headersFactory().newHeaders()

  override fun iterator(): Iterator<Map.Entry<String, String>> {
    return headers.iteratorAsString()
  }

  fun add(key: CharSequence, value: String) {
    headers.add(key, value)
  }

  fun add(key: CharSequence, value: Int) {
    headers.add(key, value)
  }

  fun add(httpHeaders: List<Pair<String, String>>) {
    httpHeaders.forEach { headers.add(it.first, it.second) }
  }

  fun contains(key: String): Boolean {
    return headers.contains(key)
  }

  fun replace(key: String, value: String) {
    headers.set(key, value)
  }

  fun get(key: String): String? {
    return headers.get(key)
  }

  fun getAll(key: String): List<String> {
    return headers.getAll(key)
  }

  fun remove(key: String) {
    headers.remove(key)
  }

  fun remove(key: CharSequence) {
    headers.remove(key)
  }

  fun contentLength(): Int? {
    return headers.getInt(HttpHeaderNames.CONTENT_LENGTH)
  }
}
