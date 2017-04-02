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
import com.google.common.io.Files;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * XML Module Test<p>
 */
@RunWith(VertxUnitRunner.class)
public class XmlTransformTest extends AbstractXmlTest {

    @Test
    public void testXmlTransformWithXslKo(TestContext context) throws Exception {
        Async async = context.async();

        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess( message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertTrue(message.body().getString("message").contains("Failed to compile stylesheet"));
            async.complete();
        });

        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlTransformHandler.XML, "<root><test>ok</test><okok/></root>");
        jsonObject.put(XmlTransformHandler.XSL, Files.toString(new File(getClass().getResource("xsl_ko.xsl").getFile()), Charsets.UTF_8));

        eventBus.send(XmlWorker.TRANSFORM_ADDRESS, jsonObject, handler);

    }

    @Test
    public void testXmlTransformWithXslOk(TestContext context) throws Exception {

        Async async = context.async();

        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess( message -> {
            context.assertEquals("ok", message.body().getString("status"));
            context.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><toto/>",message.body().getString("output"));
            async.complete();
        });

        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlTransformHandler.URL_XML, getClass().getResource(
                "books_standalone_ok.xml").toURI().toASCIIString());
        jsonObject.put(XmlTransformHandler.URL_XSL, getClass().getResource("xsl_ok.xsl").toURI().toASCIIString());
        eventBus.send(XmlWorker.TRANSFORM_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlTransformWithXslParams(TestContext context) throws Exception {

        Async async = context.async();

        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess( message -> {
                context.assertEquals("ok", message.body().getString("status"));
                context.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><toto/>c:/test",message.body().getString("output"));
                async.complete();
        });

        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlTransformHandler.URL_XML, getClass().getResource("books_standalone_ok.xml").toURI().toASCIIString());
        jsonObject.put(XmlTransformHandler.URL_XSL, getClass().getResource("xsl_ok.xsl").toURI().toASCIIString());
        JsonArray params = new JsonArray();
        params.add(new JsonObject().put("filename","c:/test"));
        jsonObject.put(XmlTransformHandler.PARAMS, params);

        eventBus.send(XmlWorker.TRANSFORM_ADDRESS, jsonObject, handler);
    }
}

