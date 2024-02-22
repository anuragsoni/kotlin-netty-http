package com.sonianurag.http

import com.sonianurag.buf.Buf
import kotlinx.coroutines.flow.Flow

interface Request {
  val version: Version
  val method: Method
  val path: String
  val headers: Headers
  val reads: Flow<Buf>
}
