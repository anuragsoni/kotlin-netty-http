package com.sonianurag.http

/**
 * Request method is used to indicate the purpose of a HTTP request. See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.3](https://datatracker.ietf.org/doc/html/rfc7231#section-4.3)
 * for more details.
 */
sealed class Method {
  data object Get : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Post : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Options : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Head : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Put : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Patch : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Delete : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Trace : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data object Connect : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  data class Custom(val method: String) : Method() {
    override fun toString(): String {
      return super.toString()
    }
  }

  override fun toString(): String =
      when (this) {
        is Get -> "GET"
        is Post -> "POST"
        is Options -> "OPTIONS"
        is Head -> "HEAD"
        is Put -> "PUT"
        is Patch -> "PATCH"
        is Delete -> "DELETE"
        is Trace -> "TRACE"
        is Connect -> "CONNECT"
        is Custom -> this.method.uppercase()
      }
}

fun String.toHttpMethod(): Method {
  return when (this.uppercase()) {
    "GET" -> Method.Get
    "POST" -> Method.Post
    "OPTIONS" -> Method.Options
    "HEAD" -> Method.Head
    "PUT" -> Method.Put
    "PATCH" -> Method.Patch
    "DELETE" -> Method.Delete
    "TRACE" -> Method.Trace
    "CONNECT" -> Method.Connect
    else -> Method.Custom(this)
  }
}

/**
 * [isSafe] returns true if the semantics for a HTTP method are essentially read-only, and the
 * client does not expect any state change on the server as a result of the request.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.1](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.1)
 * for more details.
 */
fun Method.isSafe(): Boolean =
    when (this) {
      is Method.Get,
      is Method.Head,
      is Method.Options,
      is Method.Trace -> true
      else -> false
    }

/**
 * [isIdempotent] returns true if multiple requests with a HTTP method are intended to have the same
 * effect on the server as a single such request. This function returns true for PUT, DELETE and all
 * safe methods.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.2](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.2)
 * for more details.
 */
fun Method.isIdempotent(): Boolean =
    when (this) {
      is Method.Put,
      is Method.Delete -> true
      else -> this.isSafe()
    }

/**
 * [isCacheable] indicates that responses to requests with an HTTP method are allowed to be strored
 * for future reuse. This function returns true for GET, HEAD and POST.
 *
 * See
 * [https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.3](https://datatracker.ietf.org/doc/html/rfc7231#section-4.2.3)
 * for more details.
 */
fun Method.isCacheable(): Boolean =
    when (this) {
      is Method.Get,
      is Method.Head,
      is Method.Post -> true
      else -> false
    }
