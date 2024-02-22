package com.sonianurag.buf

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*
import kotlin.NoSuchElementException

private fun checkPosLen(pos: Int, len: Int, totalLength: Int) {
  require(pos >= 0) { "Pos must be non-negative: $pos" }
  require(len >= 0) { "Len must be non-negative: $len" }
  require((pos + len) <= totalLength) {
    "pos + len must be less than totalLength: $pos + $len > $totalLength"
  }
}

/** [Buf] is an immutable byte buffer with efficient index based access. */
abstract class Buf {
  companion object {
    val empty: Buf = NoopBuffer()
  }
  /** The number of bytes in the buffer. */
  abstract val length: Int

  /** [isEmpty] returns true if the buffer is empty. */
  fun isEmpty(): Boolean = length == 0

  abstract operator fun get(index: Int): Byte

  protected abstract fun unsafeByteArrayBuf(): ByteArrayBuf

  /** Decodes a string from the [Buf] in [charset]. */
  fun decodeToString(charset: Charset): String {
    val buf = unsafeByteArrayBuf()
    return String(buf.buffer, buf.from, buf.to, charset)
  }

  protected fun checkDestinationForWrite(destinationLength: Int, destinationOffset: Int) {
    require(destinationOffset >= 0) {
      "Destination offset must be a non-negative integer: $destinationOffset"
    }
    require(length <= (destinationLength - destinationOffset)) {
      "Destination is too small. Capacity = ${destinationLength - destinationOffset}, needed: $length"
    }
  }

  operator fun iterator(): ByteIterator =
      object : ByteIterator() {
        private var index = 0

        override fun hasNext(): Boolean = index != length

        override fun nextByte(): Byte {
          return if (index != length) {
            get(index++)
          } else {
            throw NoSuchElementException("$index")
          }
        }
      }

  abstract fun write(destination: ByteArray, offset: Int)

  abstract fun write(destination: ByteBuffer)
}

private class NoopBuffer : Buf() {
  override val length: Int = 0

  override fun get(index: Int): Byte = throw IndexOutOfBoundsException()

  override fun write(destination: ByteArray, offset: Int) {
    checkDestinationForWrite(destination.size, offset)
  }

  override fun write(destination: ByteBuffer) {}

  override fun toString(): String = "EmptyBuf"

  override fun unsafeByteArrayBuf(): ByteArrayBuf {
    return ByteArrayBuf("".encodeToByteArray(), 0, 0)
  }
}

class ByteArrayBuf
internal constructor(internal val buffer: ByteArray, internal val from: Int, internal val to: Int) :
    Buf() {
  override val length: Int = to - from

  override fun unsafeByteArrayBuf(): ByteArrayBuf = this

  override fun toString(): String = "ByteArrayBuf(length=$length)"

  override fun get(index: Int): Byte {
    if (index + from >= to) {
      throw IndexOutOfBoundsException()
    }
    return buffer[index + from]
  }

  override fun write(destination: ByteArray, offset: Int) {
    checkDestinationForWrite(destination.size, offset)
    System.arraycopy(buffer, from, destination, offset, length)
  }

  override fun write(destination: ByteBuffer) {
    checkDestinationForWrite(destination.remaining(), 0)
    destination.put(buffer, from, length)
  }

  companion object {
    /**
     * Creates a new buf from the user provided [ByteArray]. The buffer instance uses the provided
     * bytearray internally, so mutating the bytearray after creating the Buf instance violates the
     * immutability constraint. It's recommended to make a copy of the bytearray before creating a
     * buf instance if there is a need to modify the bytearray after wards.
     */
    fun create(buf: ByteArray, pos: Int = 0, len: Int? = null): Buf {
      val l = len ?: (buf.size - pos)
      checkPosLen(pos, l, buf.size)
      return if (l == 0) {
        empty
      } else {
        ByteArrayBuf(buf, pos, pos + l)
      }
    }
  }

  object Safe {
    /**
     * Similar to [ByteArrayBuf.create], but performs a defensive copy of the user provided
     * bytearray before creating an instance of Buf.
     */
    fun create(buf: ByteArray, pos: Int = 0, len: Int? = null): Buf {
      val l = len ?: (buf.size - pos)
      checkPosLen(pos, l, buf.size)
      return if (l == 0) {
        empty
      } else {
        val copy = Arrays.copyOfRange(buf, pos, pos + l)
        ByteArrayBuf(copy, pos, pos + l)
      }
    }
  }
}

/** Encodes a string to a Buf using [charset]. */
fun String.encodeToBuf(charset: Charset): Buf {
  return ByteArrayBuf.create(this.toByteArray(charset))
}
