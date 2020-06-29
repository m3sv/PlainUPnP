package com.m3sv.plainupnp.upnp.trackmetadata

import android.util.Xml
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import org.xmlpull.v1.XmlSerializer
import timber.log.Timber
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.SAXParserFactory

class TrackMetadata() {

    private var id: String? = null
    private var title: String? = null
    private var artist: String? = null
    private var genre: String? = null
    private var artURI: String? = null
    private var res: String? = null
    private var itemClass: String? = null
    private var xmlreader: XMLReader = SAXParserFactory.newInstance().newSAXParser().xmlReader
    private val upnpItemHandler: UpnpItemHandler = UpnpItemHandler()

    constructor(
        id: String?,
        title: String?,
        artist: String?,
        genre: String?,
        artURI: String?,
        res: String?,
        itemClass: String?
    ) : this() {
        this.id = id
        this.title = title
        this.artist = artist
        this.genre = genre
        this.artURI = artURI
        this.res = res
        this.itemClass = itemClass
    }

    private fun parseTrackMetadata(xml: String?) {
        if (xml == null || xml == NOT_IMPLEMENTED) return

        xmlreader?.let { reader ->
            try {
                reader.contentHandler = upnpItemHandler
                reader.parse(InputSource(StringReader(xml)))
            } catch (e: Exception) {
                Timber.e(e)
                Timber.w("Error while parsing metadata !")
                Timber.w("XML : %s", xml)
            }
        }
    }

    //start a tag called "root"
    val xml: String
        get() {
            val serializer: XmlSerializer = Xml.newSerializer()
            val stringWriter = StringWriter()

            with(serializer) {
                try {
                    setOutput(stringWriter)
                    startDocument(null, null)
                    setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

                    //start a tag called "root"
                    startTag(null, "DIDL-Lite")
                    attribute(null, "xmlns", "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
                    attribute(null, "xmlns:dc", "http://purl.org/dc/elements/1.1/")
                    attribute(null, "xmlns:upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/")
                    attribute(null, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/")
                    startTag(null, "item")
                    attribute(null, "id", "" + id)
                    attribute(null, "parentID", "")
                    attribute(null, "restricted", "1")
                    if (title != null) {
                        startTag(null, "dc:title")
                        text(title)
                        endTag(null, "dc:title")
                    }
                    if (artist != null) {
                        startTag(null, "dc:creator")
                        text(artist)
                        endTag(null, "dc:creator")
                    }
                    if (genre != null) {
                        startTag(null, "upnp:genre")
                        text(genre)
                        endTag(null, "upnp:genre")
                    }
                    if (artURI != null) {
                        startTag(null, "upnp:albumArtURI")
                        attribute(null, "dlna:profileID", "JPEG_TN")
                        text(artURI)
                        endTag(null, "upnp:albumArtURI")
                    }
                    if (res != null) {
                        startTag(null, "res")
                        text(res)
                        endTag(null, "res")
                    }
                    if (itemClass != null) {
                        startTag(null, "upnp:class")
                        text(itemClass)
                        endTag(null, "upnp:class")
                    }
                    endTag(null, "item")
                    endTag(null, "DIDL-Lite")
                    endDocument()
                    flush()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            val xml = stringWriter.toString()

            Timber.d("TrackMetadata : %s", xml)
            return xml
        }

    inner class UpnpItemHandler : DefaultHandler() {
        private val buffer = StringBuilder()

        override fun startElement(
            namespaceURI: String?,
            localName: String,
            qName: String?,
            atts: Attributes
        ) {
            buffer.setLength(0)
            if (localName == "item") {
                id = atts.getValue("id")
            }
        }

        override fun endElement(
            uri: String?,
            localName: String?,
            qName: String?
        ) {
            when (localName) {
                "title" -> title = buffer.toString()
                "creator" -> artist = buffer.toString()
                "genre" -> genre = buffer.toString()
                "albumArtURI" -> artURI = buffer.toString()
                "class" -> itemClass = buffer.toString()
                "res" -> res = buffer.toString()
            }
        }

        override fun characters(
            ch: CharArray?,
            start: Int,
            length: Int
        ) {
            buffer.append(ch, start, length)
        }
    }

    private companion object {
        private const val NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
    }
}
