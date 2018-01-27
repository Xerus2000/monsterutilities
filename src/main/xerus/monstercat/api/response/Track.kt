package xerus.monstercat.api.response

import com.google.api.client.util.Key
import xerus.ktutil.getField
import xerus.ktutil.helpers.PseudoParser
import xerus.ktutil.joinEnumeration
import xerus.ktutil.printNamed
import xerus.monstercat.downloader.TRACKNAMEPATTERN
import java.util.*
import java.util.Collections.emptyList


/** JvmFields are used for Reflection, which is needed for the formatted [toString] method */
open class Track(
        @Key("_id") var
        id: String = "",
        @Key var
        created: String = "",
        @Key @JvmField var
        title: String = "",
        @Key @JvmField var
        artistsTitle: String = "",
        @Key var
        albums: List<Album> = emptyList(),
        @Key @JvmField var
        artists: List<Artist> = emptyList(),
        @JvmField var
        remix: String = "",
        @JvmField var
        featuring: String = "") {

    val alb: Album
        get() = albums.first()

    val streamHash: String?
        get() = albums.find { it.streamHash.isNotEmpty() }?.streamHash

    @JvmField
    var titleRaw: String = ""

    open fun toFileName() = toString(TRACKNAMEPATTERN())

    open fun init() {
        if (titleRaw.isNotEmpty())
            return
        val split = title.split('(', ')', '[', ']').map { it.trim() }
        titleRaw = split[0]
        if (split.size > 1)
            split.subList(1, split.lastIndex).forEach {
                when {
                    // todo split it up
                    it.startsWith("feat", true) -> featuring = it.split(' ', limit = 2)[1]
                    it.endsWith("mix", true) -> remix = it
                }
            }
    }

    /**
     * parses this object to a String using the given format String
     * @throws NoSuchFieldException if the format contains an unknown field
     */
    fun toString(format: String): String {
        init()
        // todo consider outer brackets
        val bits = BitSet()
        return format.split('{', '}').mapIndexed { i, cur ->
            if (i % 2 == 1) {
                cur.split('|').let {
                    val value = getField(it[0])
                    if (it.size > 1) {
                        val separator = it[1]
                        @Suppress("UNCHECKED_CAST")
                        (value as? Array<Any> ?: (value as? Collection<Any>)?.toTypedArray())?.let {
                            return@mapIndexed if (separator == "enumeration") joinEnumeration(*it)
                            else it.joinToString(separator)
                        }
                    }
                    value.toString()
                }
            } else cur
        }.joinToString(separator = "")
    }

    private fun parseRecursive(string: String): String {
        return PseudoParser('{', '}').parse(string) {
            parseRecursive(it)
        }
    }

    override fun toString(): String =
            (if (artistsTitle.isEmpty()) "%2\$s" else "%s - %s").format(artistsTitle, title)

}
