import com.sonianurag.http.Http
import com.sonianurag.http.toResponse
import java.net.InetSocketAddress
import kotlin.math.max

fun main() {
  Http.server(
      InetSocketAddress("localhost", 8080),
      config = { workerThreads = max(1, Runtime.getRuntime().availableProcessors() - 1) }) { request
        ->
        "Hello World".toResponse(version = request.version)
      }
}
