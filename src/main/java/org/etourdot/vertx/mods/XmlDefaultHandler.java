package org.etourdot.vertx.mods;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by Emmanuel TOURDOT on 01/09/2014.
 */
abstract class XmlDefaultHandler implements Handler<Message<?>> {
    public abstract void handle(Message<?> message);

    void sendOK(Message<JsonObject> message) {
        sendOK(message, null);
    }

    protected void sendStatus(String status, Message<JsonObject> message) {
        sendStatus(status, message, null);
    }

    void sendStatus(String status, Message<JsonObject> message, JsonObject json) {
        if (json == null) {
            json = new JsonObject();
        }
        json.putString("status", status);
        message.reply(json);
    }

    void sendOK(Message<JsonObject> message, JsonObject json) {
        sendStatus("ok", message, json);
    }

    void sendError(Message<JsonObject> message, String error) {
        sendError(message, error, null);
    }

    void sendError(Message<JsonObject> message, String error, Exception e) {
        JsonObject json = new JsonObject().putString("status", "error").putString("message", error);
        message.reply(json);
    }

}
