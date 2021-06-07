package superskidder.utils.packets.serverside;


import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ServerHeartNeededPacket extends IRCPacket {
    public ServerHeartNeededPacket(long time, String content) {
        super(time, content, IRCType.HEART);
    }
}
