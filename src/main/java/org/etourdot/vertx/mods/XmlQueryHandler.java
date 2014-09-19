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
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
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
public class XmlQueryHandler extends XmlDefaultHandler {

    public static final String QUERY = "query";
    public static final String XML = "xml";
    public static final String URL_XML = "url_xml";
    public static final String PARAMS = "params";

    private final Processor processor;
    private final Cache<HashCode, XQueryEvaluator> cacheXQueryEvaluator;

    public XmlQueryHandler(Processor processor) {
        this.processor = processor;
        cacheXQueryEvaluator = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();
    }

    public void handle(Message message) {
        final JsonObject messageBody = (JsonObject) message.body();
        final String query = messageBody.getString(QUERY);
        final String xml = messageBody.getString(XML);
        final String url_xml = messageBody.getString(URL_XML);
        final JsonArray params = messageBody.getArray(PARAMS);

        if (Strings.isNullOrEmpty(xml) && Strings.isNullOrEmpty(url_xml)) {
            sendError(message, "xml ou url_xml must be specified");
            return;
        }
        if (Strings.isNullOrEmpty(query)) {
            sendError(message, "query must be specified");
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
            final HashCode key = Hashing.crc32().hashString(query, Charsets.UTF_8);
            final XQueryEvaluator xQueryEvaluator = cacheXQueryEvaluator.get(key,
                    new Callable<XQueryEvaluator>() {
                        @Override
                        public XQueryEvaluator call() throws Exception {
                            final XQueryCompiler xQueryCompiler = processor.newXQueryCompiler();
                            final XQueryExecutable xQueryExecutable = xQueryCompiler.compile(query);
                            return xQueryExecutable.load();
                        }
                    });
            final StringWriter writer = new StringWriter();
            final Serializer out = processor.newSerializer(writer);
            xQueryEvaluator.setSource(xml_source);
            xQueryEvaluator.setDestination(out);
            if (params != null) {
                for (Object param : params) {
                    final JsonObject object = (JsonObject) param;
                    for (String fieldName : object.getFieldNames()) {
                        xQueryEvaluator.setExternalVariable(new QName(fieldName),
                                new XdmAtomicValue(object.getString(fieldName)));
                    }
                }
            }
            xQueryEvaluator.run();
            JsonObject outputObject = new JsonObject();
            outputObject.putString("output", writer.toString());
            sendOK(message, outputObject);
            return;
        } catch (SaxonApiException | UnsupportedEncodingException | ExecutionException e) {
            sendError(message, e.getMessage());
            return;
        }
    }
}

