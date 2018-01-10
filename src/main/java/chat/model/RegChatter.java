package chat.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RegChatter implements ChatInterface, Serializable {

    @GeneratedValue(strategy = GenerationType.AUTO)
    private long ID;
    @Id
    private String username;
    private String password;
    private String room = null;

    public RegChatter(String name) {
        this.username = name;
        this.password = "";
    }

    public RegChatter(String name, String pass) {
        this.username = name;
        this.password = pass;
    }

    public long getID() {
        return this.ID;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return this.room;
    }

    public String getName() {
        return this.username;
    }

    public boolean verify(String pass) {
        return this.password.equals(pass);
    }
}
