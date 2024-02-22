package com.sonianurag.http

internal object PipelineStages {
  internal const val READ_TIMEOUT = "ReadTimeout"
  internal const val HTTP_REQUEST_DECODER = "HttpRequestDecoder"
  internal const val HTTP_RESPONSE_ENCODER = "HttpResponseEncoder"
  internal const val HTTP_SERVER_EXPECT_CONTINUE = "HttpServerExpectContinue"
  internal const val HTTP_KEEP_ALIVE = "HttpKeepAlive"
  internal const val HTTP_REQUEST_HANDLER = "HttpRequestHandler"
  internal const val FLUSH_CONSOLIDATION = "FlushConsolidation"
  internal const val HTTP_REQUEST_BODY_HANDLER = "HttpRequestBodyHandler"
}
