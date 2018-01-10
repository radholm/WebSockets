package chat.integration;

import chat.controller.Controller;
import java.io.IOException;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.Session;
import chat.model.RegChatter;
import javax.json.*;
import chat.model.ChatRoom;
import chat.model.GuestChatter;
import chat.model.ChatInterface;

@ApplicationScoped
public class SessionHandler {

    @Inject
    private Controller controller;

    private final String DEFAULT_CHATROOM = "Global";

    private final Map<Session, Long> sessionUserID = new HashMap<>();
    private final Map<Session, GuestChatter> guestUser = new HashMap<>();
    private final Map<Long, Session> userIDSession = new HashMap<>();
    private final Map<Long, RegChatter> regUser = new HashMap<>();
    private final Map<String, Set<Session>> chatRooms = new HashMap<>();
    private final Set<Session> sessions = new HashSet<>();

    @PostConstruct
    void init() {
        controller.addRoom(new ChatRoom(DEFAULT_CHATROOM));
        controller.addRoom(new ChatRoom("Other"));
        List<String> rooms = controller.getRooms();
        for (String s : rooms) {
            chatRooms.put(s, new HashSet<Session>());
        }
    }

    private JsonObjectBuilder newServerResponseBuilder() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("action", "response");
        builder.add("user", "SERVER");
        return builder;
    }

    private JsonObjectBuilder newServerConnectedBuilder() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("action", "connected");
        return builder;
    }

    private void getUsers(Session session) {
        Set<Long> set = regUser.keySet();
        JsonArrayBuilder jsonarray = Json.createArrayBuilder();
        for (long l : set) {
            jsonarray.add(regUser.get(l).getName());
        }
        JsonObject obj = Json.createObjectBuilder()
                .add("action", "users")
                .add("users", jsonarray.build())
                .build();
        sendToSession(session, obj);
    }

    private void getRooms(Session session) {
        Set<String> rooms = chatRooms.keySet();
        JsonArrayBuilder jsonarray = Json.createArrayBuilder();
        for (String s : rooms) {
            jsonarray.add(s);
        }
        JsonArray arr = jsonarray.build();
        JsonObject obj = Json.createObjectBuilder()
                .add("action", "rooms")
                .add("arr", arr)
                .build();
        sendToSession(session, obj);
    }

    public void switchRoom(Session session, JsonObject info) {
        JsonObjectBuilder jsonobject = newServerResponseBuilder();
        Long LongID = sessionUserID.get(session);
        if (LongID == null) {
            jsonobject.add("message", "unregistered users cannot switch rooms");
            sendToSession(session, jsonobject.build());
            return;
        }
        long ID = LongID;
        RegChatter currUser = regUser.get(ID);
        String room = currUser.getRoom();
        String newRoom = info.getString("room");

        if (room.equals(newRoom)) {
            jsonobject.add("message", "already chatting in room " + newRoom);
            sendToSession(session, jsonobject.build());
            return;
        }
        Set<Session> chattersInRoom = chatRooms.get(room);
        chattersInRoom.remove(session);

        JsonObjectBuilder jsonobject2 = newServerResponseBuilder();
        JsonObjectBuilder jsonobject3 = newServerResponseBuilder();

        if (chatRooms.containsKey(newRoom)) {
            currUser.setRoom(newRoom);

            jsonobject.add("message", "joined room '" + newRoom + "'");
            jsonobject2.add("message", "user '" + currUser.getName() + "' switched rooms");
            jsonobject3.add("message", "user '" + currUser.getName() + "' joined the room");

            sendToSession(session, jsonobject.build());
            sendToRoom(room, jsonobject2.build());
            sendToRoom(newRoom, jsonobject3.build());
            chatRooms.get(newRoom).add(session);
        } else {
            RegChatter otherUser = controller.getUser(newRoom);
            if (otherUser == null || currUser == null) {
                jsonobject.add("message", "users need to register to open chatroom");
                sendToSession(session, jsonobject.build());
                return;
            }
            Set<Session> set = new HashSet<>();
            set.add(session);
            String nextRoom;
            do {
                int roomNumber = (int) (Math.random() * 100);
                nextRoom = "Room" + roomNumber;
            } while (chatRooms.get(nextRoom) != null);

            ChatRoom nRoom = new ChatRoom(nextRoom, currUser.getName());
            controller.addRoom(nRoom);
            chatRooms.put(nextRoom, set);

            jsonobject.add("message", "opened new room '" + nextRoom + "', user " + newRoom + " has been notified");
            jsonobject2.add("message", "user " + currUser.getName() + " switched rooms");
            jsonobject3.add("message", "user " + currUser.getName() + " has started a new room '" + nextRoom + "' and invited you");

            currUser.setRoom(newRoom);
            sendToSession(session, jsonobject.build());
            sendToRoom(room, jsonobject2.build());
            long otherUserID = otherUser.getID();
            sendToSession(userIDSession.get(otherUserID), jsonobject3.build());
        }
    }

    public void loginUser(Session session, JsonObject message) {
        JsonObjectBuilder msg = newServerResponseBuilder();
        if (sessionUserID.get(session) != null) {
            msg.add("message", "already logged in");
            sendToSession(session, msg.build());
            return;
        }
        String username = message.getString("username");
        String password = message.getString("password");
        GuestChatter guest = guestUser.get(session);
        RegChatter usr = controller.getUser(username);

        boolean verified = false, loggedIn = false;
        if (usr != null) {
            verified = usr.verify(password);
            loggedIn = userIDSession.get(usr.getID()) != null;
        } else {
            msg.add("message", "username '" + username + "' does not exist");
            sendToSession(session, msg.build());
            return;
        }
        if (!verified || loggedIn) {
            String response = loggedIn ? "already logged in" : "incorrect credentials";
            msg.add("message", "could not login - " + response);
            sendToSession(session, msg.build());
        } else {
            msg.add("message", "logged in as " + username);
            JsonObject announcement = newServerResponseBuilder()
                    .add("message", "'" + guest.getName() + "' is now '" + username + "'")
                    .build();
            sendToSession(session, msg.build());
            guestUser.remove(session);
            sendToRoom(guest.getRoom(), announcement);
            long ID = usr.getID();
            regUser.put(ID, usr);
            sessionUserID.put(session, ID);
            userIDSession.put(ID, session);
        }
    }

    public void registerUser(Session session, JsonObject message) {
        String username = message.getString("username");
        String password = message.getString("password");

        RegChatter user = controller.getUser(username);
        JsonObject msg;

        if (user != null) {
            msg = newServerResponseBuilder()
                    .add("message", "user '" + username + "' already exists")
                    .build();
        } else {
            user = new RegChatter(username, password);
            user.setRoom(DEFAULT_CHATROOM);
            msg = newServerResponseBuilder()
                    .add("message", "registered '" + username + "'")
                    .build();
            controller.addUser(user);
        }
        sendToSession(session, msg);
    }

    public void refresh(Session session) {
        getRooms(session);
        getUsers(session);
    }

    public void addSession(Session session) {
        GuestChatter guest = new GuestChatter(String.valueOf("Guest" + ((int) (Math.random() * 100))), DEFAULT_CHATROOM);
        JsonObject obj = newServerConnectedBuilder()
                .add("message", "Chatting in " + DEFAULT_CHATROOM + ", as: " + guest.getName())
                .add("room", DEFAULT_CHATROOM)
                .build();
        JsonObject newuser = newServerResponseBuilder()
                .add("message", "'" + guest.getName() + "' joined")
                .build();
        sendToSession(session, obj);
        sendToAllSessions(newuser);
        sessions.add(session);
        guestUser.put(session, guest);
        chatRooms.get(DEFAULT_CHATROOM).add(session);
    }

    public void removeSession(Session session) {
        JsonObjectBuilder msg = newServerResponseBuilder();
        sessions.remove(session);
        Long ID = sessionUserID.get(session);
        ChatInterface user;
        if (ID == null) {
            user = guestUser.get(session);
            chatRooms.get(DEFAULT_CHATROOM).remove(session);
            msg.add("message", "'" + user.getName() + "' left");
            guestUser.remove(session);
            sendToRoom(DEFAULT_CHATROOM, msg.build());
            return;
        }
        user = regUser.get(ID);
        String room = user.getRoom();
        chatRooms.get(room).remove(session);
        msg.add("message", "'" + user.getName() + "' left");
        sendToRoom(room, msg.build());
    }

    public void sendMessage(Session session, JsonObject message) {
        Long ID = sessionUserID.get(session);
        ChatInterface user;
        if (ID == null) {
            user = guestUser.get(session);
        } else {
            user = regUser.get(ID);
        }
        JsonObject msg = Json.createObjectBuilder()
                .add("action", "response")
                .add("user", user.getName())
                .add("message", message.get("message"))
                .build();
        String room = user.getRoom();
        if (room != null) {
            sendToRoom(room, msg);
        } else {
            msg = newServerResponseBuilder()
                    .add("message", "message error, not connected to chatroom")
                    .build();
            sendToSession(session, msg);
        }
    }

    private void sendToRoom(String room, JsonObject msg) {
        Set<Session> set = chatRooms.get(room);
        for (Session s : set) {
            sendToSession(s, msg);
        }
    }

    private void sendToAllSessions(JsonObject message) {
        for (Session s : sessions) {
            sendToSession(s, message);
        }
    }

    public void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException e) {
            removeSession(session);
        }
    }
}
