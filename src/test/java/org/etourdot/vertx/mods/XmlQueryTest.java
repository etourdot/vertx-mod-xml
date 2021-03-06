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
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * XML Module Test<p>
 */
@RunWith(JavaClassRunner.class)
public class XmlQueryTest extends TestVerticle {

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

    @Test
    public void testXmlQuery() throws Exception {
        Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
                assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>true",message.body().getString("output"));
                testComplete();
            }
        };
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(XmlQueryHandler.URL_XML, getClass().getResource(
                "books_standalone_ok.xml").toURI().toASCIIString());
        jsonObject.putString(XmlQueryHandler.QUERY, "contains(., '352')");
        vertx.eventBus().send(XmlWorker.QUERY_ADDRESS, jsonObject, replyHandler);
    }

}

