package xerus.monstercat.api

import javafx.scene.image.Image
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import xerus.ktutil.replaceIllegalFileChars
import xerus.monstercat.cacheDir
import java.io.File
import java.io.InputStream
import java.net.URI

object Covers {

	private val coverCacheDir = cacheDir.resolve("cover-images").apply { mkdirs() }
	
	private fun coverCacheFile(file: File, coverUrl: String, size: Int) {
		file.mkdirs()
		val newFile = file.resolve(coverUrl.substringAfterLast('/').replaceIllegalFileChars())
		file.resolve("${newFile.nameWithoutExtension}-${size}x$size.${newFile.extension}")
	}
	
	private fun createImage(content: InputStream, size: Number) =
			Image(content, size.toDouble(), size.toDouble(), false, false)
	
	/** Returns an Image of the cover in the requested size using caching.
	 * @param size the size of the Image - the underlying image data will always be 64x64, thus this is the default. */
	fun getThumbnailImage(coverUrl: String, size: Number = 64, invalidate: Boolean = false): Image =
		getCover(coverUrl, 64,  invalidate).use { createImage(it, size) }

	/** @return an [InputStream] of the downloaded image file
	 * @param coverUrl the URL for fetching the cover
	 * @param size the final size of the cover
	 * @param invalidate set to true to ignore already existing cache files
	 */
	fun getCover(coverUrl: String, size: Int = 64, invalidate: Boolean = false) : InputStream {
		val file = coverCacheFile(coverCacheDir , coverUrl, size)
		if(!file.exists() || invalidate) {
			fetchCover(coverUrl, size).content.use { input ->
				file.outputStream().use { out ->
					input.copyTo(out)
				}
			}
		}
		return file.inputStream()
	}
	
	/** Returns a larger Image of the cover in the requested size using caching.
	 * @param size the size of the Image - the image file will always be 2048x2048 */
	fun getCoverImage(coverUrl: String, size: Int = 2048, invalidate: Boolean = false): Image =
			getCover(coverUrl, 2048, invalidate).use { createImage(it, size) }
	
	/** Fetches the given [coverUrl] with an [APIConnection] in the requested [size].
	 * @param coverUrl the base url to fetch the cover
	 * @param size the size of the cover to be fetched from the api, with all powers of 2 being available.
	 *             By default null, which results in the biggest size possible, usually between 2k and 8k. */
	fun fetchCover(coverUrl: String, size: Int? = null): HttpEntity =
		APIConnection.executeRequest(HttpGet(getCoverUrl(coverUrl, size))).entity
	
	/** Attaches a parameter to the [coverUrl] for the requested [size] */
	private fun getCoverUrl(coverUrl: String, size: Int? = null) =
		URI(coverUrl).toASCIIString() + size?.let { "?image_width=$it" }.orEmpty()
	
}