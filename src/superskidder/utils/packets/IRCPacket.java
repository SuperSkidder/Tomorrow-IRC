package superskidder.utils.packets;

public class IRCPacket {
    public long time = 0;
    public String content = "";
    public IRCType type;
    public int key = 0;//以后实现

    public IRCPacket(long time, String content, IRCType type) {
        this.time = time;
        this.content = content;
        this.type = type;
    }

}
