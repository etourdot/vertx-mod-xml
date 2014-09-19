package org.etourdot.vertx.mods;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Created by Emmanuel TOURDOT on 10/09/2014.
 */
public class XmlErrorHandler implements ErrorHandler {

    private final StringBuffer buffer;

    public XmlErrorHandler() {
        buffer = new StringBuffer();
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        buffer.append("WARNING");
        if (exception.getCause() != null) {
            buffer.append(exception.getCause().getMessage());
        } else {
            buffer.append(exception.getMessage());
        }
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        buffer.append("ERROR");
        if (exception.getCause() != null) {
            buffer.append(exception.getCause().getMessage());
        } else {
            buffer.append(exception.getMessage());
        }
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        buffer.append("FATAL");
        if (exception.getCause() != null) {
            buffer.append(exception.getCause().getMessage());
        } else {
            buffer.append(exception.getMessage());
        }
    }

    public String getErrors() {
        return buffer.toString();
    }

}
