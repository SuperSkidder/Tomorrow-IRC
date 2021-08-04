package superskidder.utils.packets.clientside;


import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ClientConnectPacket extends IRCPacket {

    public String username = "";
    public String password = "";
    public String hwid = "";

    public ClientConnectPacket(long time, String content,String username,String password,String hwid) {
        super(time, content, IRCType.CONNECT);
        this.username = username;
        this.password = password;
        this.hwid = hwid;
    }
}
