package superskidder.utils.packets.serverside;


import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ServerChatPacket extends IRCPacket {
    public ServerChatPacket(long time, String content){
        super(time,content, IRCType.CHAT);
    }
}
