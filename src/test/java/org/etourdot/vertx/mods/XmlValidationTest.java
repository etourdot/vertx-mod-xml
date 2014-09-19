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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * XML Module Test<p>
 */
@RunWith(JavaClassRunner.class)
public class XmlValidationTest extends TestVerticle {

    private Logger logger;

    @Override
    public void start() {
        initialize();
        final JsonObject conf = new JsonObject();
        container.deployModule(System.getProperty("vertx.modulename"), conf,
                new AsyncResultHandler<String>() {
                    @Override
                    public void handle(AsyncResult<String> asyncResult) {
                        if (asyncResult.failed()) {
                            container.logger().error(asyncResult.cause());
                        }
                        assertTrue(asyncResult.succeeded());
                        assertNotNull("deploymentID should not be null",
                                asyncResult.result());
                        // If deployed correctly then start the tests!
                        startTests();
                    }
                });
        logger = container.logger();
    }

    /**
     * Tests XmlValidationHandler
     */

    @Test
    public void testXmlValidationWithoutXml() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertEquals("xml or xmlurl must be specified", message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlStreamValidationXmlStandaloneOk() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
                assertNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.XML, "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><test>ok</test><okok></okok></root>");
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlBufferValidationXmlStandaloneOk() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
                assertNull(message.body().getString("message"));
                testComplete();
            }
        };
        final Buffer buffer = new Buffer();
        buffer.appendString("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><test>ok</test><okok></okok></root>");
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, buffer,replyHandler);
    }

    @Test
    public void testXmlUrlValidationXmlStandaloneOk() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
                assertNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.URL_XML, getClass().getResource("books_standalone_ok.xml").toURI()
                .toASCIIString());
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlValidationXmlPublicDTDOk() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
                assertNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.URL_XML, getClass().getResource("books_dtd_ok.xml").toURI().toASCIIString());
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlValidationXmlPublicDTDKo() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertNotNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.URL_XML, getClass().getResource("books_dtd_ko.xml").toURI().toASCIIString());
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlStreamValidationXmlStandaloneKo() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertNotNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.XML, "<root><test>ok<okok/></root></test>");
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }

    @Test
    public void testXmlBufferValidationXmlStandaloneKo() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertNotNull(message.body().getString("message"));
                testComplete();
            }
        };
        final Buffer buffer = new Buffer();
        buffer.appendString("<root><test>ok<okok/></root></test>");
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, buffer, replyHandler);
    }

    @Test
    public void testXmlUrlValidationXmlStandaloneKo() throws Exception {
        final Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertNotNull(message.body().getString("message"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlValidationHandler.URL_XML, this.getClass().getResource("books_standalone_ko.xml").toURI().toASCIIString());
        vertx.eventBus().send(XmlWorker.VALIDATION_ADDRESS, jsonObject, replyHandler);
    }
}

