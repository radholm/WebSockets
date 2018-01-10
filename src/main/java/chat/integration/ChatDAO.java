package chat.integration;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import chat.model.ChatRoom;
import chat.model.RegChatter;

@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class ChatDAO {

    @PersistenceContext(unitName = "chatPU")
    private EntityManager manager;

    public boolean addUser(RegChatter user) {
        if (manager.find(RegChatter.class, user.getName()) != null) {
            return false;
        }
        manager.persist(user);
        return true;
    }

    public RegChatter getUser(Object PrimaryKey) {
        return manager.find(RegChatter.class, PrimaryKey);
    }

    public boolean addRoom(ChatRoom room) {
        if (manager.find(ChatRoom.class, room.getName()) != null) {
            return false;
        }
        manager.persist(room);
        return true;
    }

    public List<String> getRooms() {
        Query query = manager.createQuery("SELECT r.roomName FROM ChatRoom r");
        return query.getResultList();
    }
}
