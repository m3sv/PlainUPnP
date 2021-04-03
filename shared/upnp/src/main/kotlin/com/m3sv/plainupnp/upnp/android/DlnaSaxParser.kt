package com.m3sv.plainupnp.upnp.android

import org.seamless.xml.SAXParser
import org.xml.sax.XMLReader
import javax.xml.parsers.SAXParserFactory

class DlnaSaxParser : SAXParser() {
    override fun create(): XMLReader = try {
        SAXParserFactory
            .newInstance()
            .apply {
                // Configure factory to prevent XXE attacks
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
            .newSAXParser()
            .xmlReader
    } catch (ex: Exception) {
        throw RuntimeException(ex)
    }
}
