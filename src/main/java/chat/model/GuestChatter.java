package chat.model;

public class GuestChatter implements ChatInterface {

    private String username;
    private String room;
    private long ID;

    public GuestChatter(String name, String room) {
        this.username = name;
        this.room = room;
        this.ID = (int) (Math.random() * 100);
    }

    public String getRoom() {
        return this.room;
    }

    public String getName() {
        return this.username;
    }
}
