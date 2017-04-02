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
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.sf.saxon.s9api.*;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * XML Module<p> Please see the busmods manual for a full description<p> entrée: { "xsl": source
 * xsl, "params" : liste params, "xml": source xml } sortie: { "status": "ok/ko", "errors": liste
 * erreurs, "xml": xml transformé }
 */
public class XmlTransformHandler extends XmlDefaultHandler {

    public static final String XSL = "xsl";
    public static final String URL_XSL = "url_xsl";
    public static final String XML = "xml";
    public static final String URL_XML = "url_xml";
    public static final String PARAMS = "params";

    private final Processor processor;
    private final Cache<HashCode, XsltTransformer> cacheTransformers;

    public XmlTransformHandler(Processor processor) {
        this.processor = processor;
        cacheTransformers = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();
    }

    public void handle(Message message) {
        final JsonObject messageBody = (JsonObject) message.body();
        final String xsl = messageBody.getString(XSL);
        final String url_xsl = messageBody.getString(URL_XSL);
        final String xml = messageBody.getString(XML);
        final String url_xml = messageBody.getString(URL_XML);
        final JsonArray params = messageBody.getJsonArray(PARAMS);

        if (Strings.isNullOrEmpty(xml) && Strings.isNullOrEmpty(url_xml)) {
            sendError(message, "xml ou url_xml must be specified");
            return;
        }
        if (Strings.isNullOrEmpty(xsl) && Strings.isNullOrEmpty(url_xsl)) {
            sendError(message, "xsl or url_xsl must be specified");
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
            final Source xsl_source;
            if (!Strings.isNullOrEmpty(xsl)) {
                if (!Strings.isNullOrEmpty(url_xsl)) {
                    sendError(message, "xsl either url_xsl must be specified");
                    return;
                }
                xsl_source = new StreamSource(new ByteArrayInputStream(xsl.getBytes("UTF-8")));
            } else {
                xsl_source = new SAXSource(new InputSource(url_xsl));
            }
            final HashCode key = Hashing.crc32().hashString(Joiner.on("").skipNulls().join(xsl,
                    url_xsl), Charsets.UTF_8);
            final XsltTransformer xsltTransformer = cacheTransformers.get(key,
                    new Callable<XsltTransformer>() {
                        @Override
                        public XsltTransformer call() throws Exception {
                            final XsltCompiler xsltCompiler = processor.newXsltCompiler();
                            final XsltExecutable xsltExecutable = xsltCompiler.compile(xsl_source);
                            return xsltExecutable.load();
                        }
                    });
            final XdmNode source = processor.newDocumentBuilder().build(xml_source);
            final StringWriter writer = new StringWriter();
            final Serializer out = processor.newSerializer(writer);
            xsltTransformer.setInitialContextNode(source);
            xsltTransformer.setDestination(out);
            if (params != null) {
                for (Object param : params) {
                    final JsonObject object = (JsonObject) param;
                    for (String fieldName : object.fieldNames()) {
                        xsltTransformer.setParameter(new QName(fieldName), new XdmAtomicValue(object.getString(fieldName)));
                    }
                }
            }
            xsltTransformer.transform();
            JsonObject outputObject = new JsonObject();
            outputObject.put("output", writer.toString());
            sendOK(message, outputObject);
        } catch (SaxonApiException | UnsupportedEncodingException | ExecutionException e) {
            sendError(message, e.getMessage());
        }
    }
}

