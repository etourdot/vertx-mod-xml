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
public class XmlQueryTest extends AbstractXmlTest {

    @Test
    public void testXmlQuery(TestContext context) throws Exception {
        final Async async = context.async();

        final JsonObject jsonObject = new JsonObject();
        jsonObject.put(XmlQueryHandler.URL_XML, getClass().getResource(
                "books_standalone_ok.xml").toURI().toASCIIString());
        jsonObject.put(XmlQueryHandler.QUERY, "contains(., '352')");

        Handler<AsyncResult<Message<JsonObject>>> handler = context.asyncAssertSuccess( message -> {
            context.assertEquals("ok", message.body().getString("status"));
            context.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>true", message.body().getString("output"));
            async.complete();

        });

        eventBus.send(XmlWorker.QUERY_ADDRESS, jsonObject, handler);
    }

}

