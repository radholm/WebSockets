package chat.model;

public class GuestChatter implements ChatInterface {

    private long ID;
    private String username;
    private String room;

    public GuestChatter(String name, String room) {
        this.ID = (int) (Math.random() * 100);
        this.username = name;
        this.room = room;
    }
    
    public long getID() {
        return this.ID;
    }

    public String getName() {
        return this.username;
    }

    public String getRoom() {
        return this.room;
    }
}
