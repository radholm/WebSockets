package chat.controller;

import chat.integration.ChatDAO;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import chat.model.ChatRoom;
import chat.model.RegChatter;

@Stateless
public class Controller {

    @EJB
    ChatDAO chatDAO;

    public boolean addUser(RegChatter user) {
        return chatDAO.addUser(user);
    }

    public RegChatter getUser(Object PrimaryKey) {
        return chatDAO.getUser(PrimaryKey);
    }

    public boolean addRoom(ChatRoom room) {
        return chatDAO.addRoom(room);
    }

    public List<String> getRooms() {
        return chatDAO.getRooms();
    }
}
