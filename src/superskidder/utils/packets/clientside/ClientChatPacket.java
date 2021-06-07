package superskidder.utils.packets.clientside;


import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;

public class ClientChatPacket extends IRCPacket {
    public String s = "";
    public ClientChatPacket(long time,String content){
        super(time,content, IRCType.CHAT);
    }
}
