package chat.integration;

import java.io.StringReader;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/actions")
public class WebSocketServer {

    @Inject
    private SessionHandler sessionHandler;

    @OnOpen
    public void onOpen(Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {

    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonobject = reader.readObject();
            String action = jsonobject.getString("action");
            switch (action) {
                case "login":
                    sessionHandler.loginUser(session, jsonobject);
                    break;
                case "send":
                    sessionHandler.sendMessage(session, jsonobject);
                    break;
                case "remove":
                    sessionHandler.removeSession(session);
                    break;
                case "register":
                    sessionHandler.registerUser(session, jsonobject);
                    break;
                case "refresh":
                    sessionHandler.refresh(session);
                    break;
                case "switchRoom":
                    sessionHandler.switchRoom(session, jsonobject);
                    break;
                default:
                    JsonObject msg = Json.createObjectBuilder()
                            .add("action", "response")
                            .add("user", "SERVER")
                            .add("message", "ACTION NOT SUPPORTED")
                            .build();
                    sessionHandler.sendToSession(session, msg);
                    break;
            }
        }
    }
}
