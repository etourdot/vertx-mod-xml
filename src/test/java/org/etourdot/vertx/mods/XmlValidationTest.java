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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * XML Module Test<p>
 */
@RunWith(VertxUnitRunner.class)
public class XmlValidationTest extends AbstractXmlTest {


    @Test
    public void testXmlValidationWithoutXml(TestContext context) throws Exception {

        Async async = context.async();

        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertEquals("xml or xmlurl must be specified", message.body().getString("message"));
            async.complete();
        });

        final JsonObject jsonObject = new JsonObject();
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlStreamValidationXmlStandaloneOk(TestContext context) throws Exception {
        
        Async async = context.async();

        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
                context.assertEquals("ok", message.body().getString("status"));
                context.assertNull(message.body().getString("message"));
                async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.XML, "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><test>ok</test><okok></okok></root>");
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlBufferValidationXmlStandaloneOk(TestContext context) throws Exception {
        
        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
                context.assertEquals("ok", message.body().getString("status"));
                context.assertNull(message.body().getString("message"));
                async.complete();
        });
        final Buffer buffer = Buffer.buffer();
        buffer.appendString("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><test>ok</test><okok></okok></root>");
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, buffer,handler);
    }

    @Test
    public void testXmlUrlValidationXmlStandaloneOk(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
                context.assertEquals("ok", message.body().getString("status"));
                context.assertNull(message.body().getString("message"));
                async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.URL_XML, getClass().getResource("books_standalone_ok.xml").toURI()
                .toASCIIString());
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlValidationXmlPublicDTDOk(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
                context.assertEquals("ok", message.body().getString("status"));
                context.assertNull(message.body().getString("message"));
                async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.URL_XML, getClass().getResource("books_dtd_ok.xml").toURI().toASCIIString());
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlValidationXmlPublicDTDKo(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertNotNull(message.body().getString("message"));
            async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.URL_XML, getClass().getResource("books_dtd_ko.xml").toURI().toASCIIString());
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlStreamValidationXmlStandaloneKo(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertNotNull(message.body().getString("message"));
            async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.XML, "<root><test>ok<okok/></root></test>");
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }

    @Test
    public void testXmlBufferValidationXmlStandaloneKo(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertNotNull(message.body().getString("message"));
            async.complete();
        });
        final Buffer buffer = Buffer.buffer();
        buffer.appendString("<root><test>ok<okok/></root></test>");
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, buffer, handler);
    }

    @Test
    public void testXmlUrlValidationXmlStandaloneKo(TestContext context) throws Exception {

        Async async = context.async();
        
        final Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess(message -> {
            context.assertEquals("error", message.body().getString("status"));
            context.assertNotNull(message.body().getString("message"));
            async.complete();
        });
        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlValidationHandler.URL_XML, this.getClass().getResource("books_standalone_ko.xml").toURI().toASCIIString());
        eventBus.send(XmlWorker.VALIDATION_ADDRESS, jsonObject, handler);
    }
}

