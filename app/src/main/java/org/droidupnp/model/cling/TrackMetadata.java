package org.droidupnp.model.cling;

import android.util.Log;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlSerializer;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import timber.log.Timber;

public class TrackMetadata {

    protected static final String TAG = "TrackMetadata";

    @Override
    public String toString() {
        return "TrackMetadata [id=" + id + ", title=" + title + ", artist=" + artist + ", genre=" + genre + ", artURI="
                + artURI + "res=" + res + ", itemClass=" + itemClass + "]";
    }

    public TrackMetadata(String xml) {
        parseTrackMetadata(xml);
    }

    public TrackMetadata() {
    }

    public TrackMetadata(String id, String title, String artist, String genre, String artURI, String res,
                         String itemClass) {
        super();
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.artURI = artURI;
        this.res = res;
        this.itemClass = itemClass;
    }

    private String id;
    private String title;
    private String artist;
    private String genre;
    private String artURI;
    private String res;
    private String itemClass;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public String getArtURI() {
        return artURI;
    }

    public String getRes() {
        return res;
    }

    public String getItemClass() {
        return itemClass;
    }

    private XMLReader initializeReader() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader xmlreader = parser.getXMLReader();
        return xmlreader;
    }

    public void parseTrackMetadata(String xml) {
        Timber.d("XML: %s", xml);

        if (xml == null || xml.equals("NOT_IMPLEMENTED"))
            return;

        try {
            XMLReader xmlreader = initializeReader();
            UpnpItemHandler upnpItemHandler = new UpnpItemHandler();

            xmlreader.setContentHandler(upnpItemHandler);
            xmlreader.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
            Log.w(TAG, "Error while parsing metadata !");
            Log.w(TAG, "XML : " + xml);
        }
    }

    public String getXML() {
        XmlSerializer s = Xml.newSerializer();
        StringWriter sw = new StringWriter();

        try {
            s.setOutput(sw);

            s.startDocument(null, null);
            s.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            //start a tag called "root"
            s.startTag(null, "DIDL-Lite");
            s.attribute(null, "xmlns", "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/");
            s.attribute(null, "xmlns:dc", "http://purl.org/dc/elements/1.1/");
            s.attribute(null, "xmlns:upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/");
            s.attribute(null, "xmlns:dlna", "urn:schemas-dlna-org:metadata-1-0/");

            s.startTag(null, "item");
            s.attribute(null, "id", "" + id);
            s.attribute(null, "parentID", "");
            s.attribute(null, "restricted", "1");

            if (title != null) {
                s.startTag(null, "dc:title");
                s.text(title);
                s.endTag(null, "dc:title");
            }

            if (artist != null) {
                s.startTag(null, "dc:creator");
                s.text(artist);
                s.endTag(null, "dc:creator");
            }

            if (genre != null) {
                s.startTag(null, "upnp:genre");
                s.text(genre);
                s.endTag(null, "upnp:genre");
            }

            if (artURI != null) {
                s.startTag(null, "upnp:albumArtURI");
                s.attribute(null, "dlna:profileID", "JPEG_TN");
                s.text(artURI);
                s.endTag(null, "upnp:albumArtURI");
            }

            if (res != null) {
                s.startTag(null, "res");
                s.text(res);
                s.endTag(null, "res");
            }

            if (itemClass != null) {
                s.startTag(null, "upnp:class");
                s.text(itemClass);
                s.endTag(null, "upnp:class");
            }

            s.endTag(null, "item");

            s.endTag(null, "DIDL-Lite");

            s.endDocument();
            s.flush();

        } catch (Exception e) {
            Timber.e("error occurred while creating xml file : " + e.toString());
            Timber.e(e);
        }

        String xml = sw.toString();
        Log.d(TAG, "TrackMetadata : " + xml);

        return xml;
    }

    public class UpnpItemHandler extends DefaultHandler {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            buffer.setLength(0);

            if (localName.equals("item")) {
                id = atts.getValue("id");
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (localName) {
                case "title":
                    title = buffer.toString();
                    break;
                case "creator":
                    artist = buffer.toString();
                    break;
                case "genre":
                    genre = buffer.toString();
                    break;
                case "albumArtURI":
                    artURI = buffer.toString();
                    break;
                case "class":
                    itemClass = buffer.toString();
                    break;
                case "res":
                    res = buffer.toString();
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            buffer.append(ch, start, length);
        }
    }
}