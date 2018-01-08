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
    ChatDAO dao;

    public boolean addUser(RegChatter user) {
        return dao.addUser(user);
    }

    public RegChatter getUser(Object PrimaryKey) {
        return dao.getUser(PrimaryKey);
    }

    public List<String> getRooms() {
        return dao.getRooms();
    }

    public boolean addRoom(ChatRoom room) {
        return dao.addRoom(room);
    }
}
