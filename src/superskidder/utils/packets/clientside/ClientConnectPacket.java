package superskidder.utils.packets.clientside;

import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ClientConnectPacket extends IRCPacket {
    public String username = "";
    public String gameID = "";

    public ClientConnectPacket(long time, String content) {
        super(time, content, IRCType.CONNECT);
    }
}
