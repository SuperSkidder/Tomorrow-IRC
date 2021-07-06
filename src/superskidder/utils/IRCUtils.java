package superskidder.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import superskidder.utils.packets.IRCPacket;

import java.lang.reflect.Type;

public class IRCUtils {
    static Gson gson = new GsonBuilder().setVersion(1.1).setPrettyPrinting().create();

    public static String toJson(IRCPacket packet) {
        String j = gson.toJson(packet);
        return j;
    }

    public static String toJson(Object packet) {
        String j = gson.toJson(packet);
        return j;
    }

    public static IRCPacket toPacket(String packet,Type ctype) {
        return gson.fromJson(packet, ctype);
    }
    public static Object toObject(String packet,Type ctype) {
        return gson.fromJson(packet, ctype);
    }
}
