package superskidder.utils.packets.clientside;


import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ClientHeartPacket extends IRCPacket {
    public ClientHeartPacket(long time, String content) {
        super(time, content, IRCType.HEART);
    }
}
