package chat.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChatRoom implements Serializable {

    @Id
    private String roomName;
    private String owner;

    public ChatRoom(String name) {
        this.roomName = name;
        this.owner = null;
    }

    public ChatRoom(String name, String owner) {
        this.roomName = name;
        this.owner = owner;
    }

    public String getName() {
        return this.roomName;
    }

    public String getOwner() {
        return this.owner;
    }
}
