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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

/**
 * XML Module<p> Please see the busmods manual for a full description<p>
 */
public class XmlXPathHandler extends XmlDefaultHandler {

    public static final String XPATH = "xpath";
    public static final String XML = "xml";
    public static final String URL_XML = "url_xml";
    public static final String PARAMS = "params";

    private final Processor processor;
    private final Cache<HashCode, XPathSelector> cacheXPathSelector;

    public XmlXPathHandler(Processor processor) {
        this.processor = processor;
        cacheXPathSelector = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();
    }

    public void handle(Message message) {
        final JsonObject messageBody = (JsonObject) message.body();
        final String xpath = messageBody.getString(XPATH);
        final String xml = messageBody.getString(XML);
        final String url_xml = messageBody.getString(URL_XML);
        //final JsonArray params = messageBody.getArray(PARAMS);

        if (Strings.isNullOrEmpty(xml) && Strings.isNullOrEmpty(url_xml)) {
            sendError(message, "xml ou url_xml must be specified");
            return;
        }
        if (Strings.isNullOrEmpty(xpath)) {
            sendError(message, "xpath must be specified");
            return;
        }

        try {
            final Source xml_source;
            if (!Strings.isNullOrEmpty(xml)) {
                if (!Strings.isNullOrEmpty(url_xml)) {
                    sendError(message, "xml either url_xml must be specified");
                    return;
                }
                xml_source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            } else {
                xml_source = new SAXSource(new InputSource(url_xml));
            }
            final HashCode key = Hashing.crc32().hashString(xpath, Charsets.UTF_8);
            final XPathSelector xPathSelector = cacheXPathSelector.get(key,
                    new Callable<XPathSelector>() {
                        @Override
                        public XPathSelector call() throws Exception {
                            final XPathCompiler xPathCompiler = processor.newXPathCompiler();
                            final XPathExecutable xPathExecutable = xPathCompiler.compile(xpath);
                            return xPathExecutable.load();
                        }
                    });
            final XdmNode source = processor.newDocumentBuilder().build(xml_source);
            xPathSelector.setContextItem(source);
            final XdmValue xdmValue = xPathSelector.evaluate();
            final StringWriter writer = new StringWriter();
            final Serializer out = processor.newSerializer(writer);
            out.serializeXdmValue(xdmValue);
            JsonObject outputObject = new JsonObject();
            outputObject.putString("output", writer.toString());
            sendOK(message, outputObject);
        } catch (SaxonApiException | UnsupportedEncodingException | ExecutionException e) {
            sendError(message, e.getMessage());
        }
    }
}

