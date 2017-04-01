package org.etourdot.vertx.mods;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Before;
import org.junit.Rule;

public class AbstractXmlTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    protected EventBus eventBus;

    @Before
    public void setup(TestContext context) {

        Vertx vertx = rule.vertx();
        eventBus = vertx.eventBus();

        vertx.deployVerticle(new XmlWorker(), context.asyncAssertSuccess());
    }
}
