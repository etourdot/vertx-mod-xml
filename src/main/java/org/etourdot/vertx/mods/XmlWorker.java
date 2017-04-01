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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;

/**
 * XML Module<p> Please see the busmods manual for a full description<p>
 */
public class XmlWorker extends AbstractVerticle {

    public final static String VALIDATION_ADDRESS = "xmlworker.validation";
    public final static String TRANSFORM_ADDRESS = "xmlworker.transform";
    public final static String QUERY_ADDRESS = "xmlworker.query";
    public final static String XPATH_ADDRESS = "xmlworker.xpath";

    private Processor processor;

    @Override
    public void start() {

        Configuration configuration = new Configuration();
        processor = new Processor(configuration);
        EventBus eb = vertx.eventBus();
        eb.consumer(VALIDATION_ADDRESS, new XmlValidationHandler(processor)::handle);
        eb.consumer(TRANSFORM_ADDRESS, new XmlTransformHandler(processor)::handle);
        eb.consumer(QUERY_ADDRESS, new XmlQueryHandler(processor)::handle);
        eb.consumer(XPATH_ADDRESS, new XmlXPathHandler(processor)::handle);
    }

    @Override
    public void stop() {
    }

}

