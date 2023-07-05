import java.io.InputStream
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import kotlin.io.path.inputStream

enum class HashingAlgorithm(algorithm: String) {
  SHA256("SHA-256"),
  SHA1("SHA-1");

  private val threadLocalMessageDigest = ThreadLocal.withInitial { MessageDigest.getInstance(algorithm) }

  val digest: MessageDigest
    get() = threadLocalMessageDigest.get()
}

fun Path.hashFile(algorithm: HashingAlgorithm): ByteArray = inputStream().use { input -> input.hash(algorithm) }

fun InputStream.hash(algorithm: HashingAlgorithm): ByteArray {
  val digestStream = DigestInputStream(this, algorithm.digest)
  digestStream.use { stream ->
    val buffer = ByteArray(1024)
    while (stream.read(buffer) != -1) {
      // reading
    }
  }
  return digestStream.messageDigest.digest()
}

fun ByteArray.asHexString(): String {
  val sb: StringBuilder = StringBuilder(size * 2)
  for (aHash in this) {
    sb.append("%02x".format(aHash.toInt() and 0xFF))
  }
  return sb.toString()
}
