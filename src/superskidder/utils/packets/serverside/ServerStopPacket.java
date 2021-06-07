package superskidder.utils.packets.serverside;

import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ServerStopPacket extends IRCPacket {
    public ServerStopPacket(long time, String content) {
        super(time, content, IRCType.STOP);
    }
}
