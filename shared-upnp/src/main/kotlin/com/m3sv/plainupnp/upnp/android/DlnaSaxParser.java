package com.m3sv.plainupnp.upnp.android;

import org.seamless.xml.SAXParser;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;

public class DlnaSaxParser extends SAXParser {
    @Override
    protected XMLReader create() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // Configure factory to prevent XXE attacks
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            xmlReader.setErrorHandler(getErrorHandler());
            return xmlReader;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
