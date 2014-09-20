/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.etourdot.vertx.mods;

import com.google.common.base.Strings;


import net.sf.saxon.s9api.Processor;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * XML Module<p> Please see the busmods manual for a full description<p>
 *     entrée: { "xml": source xml ou "xmlurl": url source xml } ou buffer
 *     sortie: { "status": "ok/ko", "errors": liste erreurs }
 */
public class XmlValidationHandler extends XmlDefaultHandler {

    public static final String XML = "xml";
    public static final String URL_XML = "url_xml";

    private final Processor processor;

    public XmlValidationHandler(Processor processor) {
        this.processor = processor;
    }

    public void handle(Message message) {
        try {
            final XmlToValidate xmlToValidate = new XmlToValidate(message).invoke();
            if (xmlToValidate.isNotValide()) {
                return;
            }
            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            xmlReader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            final XmlErrorHandler xmlErrorHandler = new XmlErrorHandler();
            xmlReader.setErrorHandler(xmlErrorHandler);
            xmlReader.parse(xmlToValidate.getInputSource());
            if (sendErrorMessage(message, xmlErrorHandler)) {
                return;
            }
        } catch (Exception e) {
            sendError(message, e.getMessage());
            return;
        }
        sendOK(message);
    }

    private boolean sendErrorMessage(Message message, XmlErrorHandler xmlErrorHandler) {
        final String errors = xmlErrorHandler.getErrors();
        if (!Strings.isNullOrEmpty(errors)) {
            sendError(message, errors);
            return true;
        }
        return false;
    }

    private class XmlToValidate {

        private boolean myResult;
        private final Message message;
        private InputSource inputSource;

        public XmlToValidate(Message message) {
            this.message = message;
        }

        boolean isNotValide() {
            return myResult;
        }

        public InputSource getInputSource() {
            return inputSource;
        }

        public XmlToValidate invoke() throws UnsupportedEncodingException {
            if (message.body() instanceof Buffer) {
                final Buffer buffer = (Buffer) message.body();
                inputSource = new InputSource(new ByteArrayInputStream(buffer.getBytes()));
            } else {
                final JsonObject jsonObject = ((JsonObject) message.body());
                final String xml = jsonObject.getString(XML);
                final String xmlurl = jsonObject.getString(URL_XML);
                if (Strings.isNullOrEmpty(xml) && Strings.isNullOrEmpty(xmlurl)) {
                    sendError(message, "xml or xmlurl must be specified");
                    myResult = true;
                    return this;
                }

                if (!Strings.isNullOrEmpty(xml)) {
                    if (!Strings.isNullOrEmpty(xmlurl)) {
                        sendError(message, "xml either xmlurl must be specified");
                        myResult = true;
                        return this;
                    }
                    inputSource = new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                } else {
                    inputSource = new InputSource(xmlurl);
                }
            }
            myResult = false;
            return this;
        }
    }
}

