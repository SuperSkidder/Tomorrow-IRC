package superskidder.QQ;

import java.util.Base64;

import com.alibaba.fastjson.JSONObject;
import superskidder.QQ.Core;
import superskidder.QQ.StringUtils;
import superskidder.gui.Controller;

/**
 * 主要类
 *
 * @author zhaoqk
 * <p>
 * 2020年8月10日 下午5:07:37
 */
public class QQTest {

    public static ChatClient clientTest;
    public static long selfQ;


    /**
     * 收到好友消息
     *
     * @param data
     */
    public static void receivePrivateMessages(String data) {
        System.out.println("[收到好友消息]" + data);
        JSONObject json = JSONObject.parseObject(data);
        long selfQQ = json.getInteger("selfQQ");//框架QQ
        selfQ = json.getInteger("selfQQ");//框架QQ
        long fromQQ = json.getInteger("fromQQ");//对方QQ
        long random = json.getInteger("random");//撤回消息用
        long req = json.getInteger("req");//撤回消息用
        String msg = json.getString("msg");//消息内容

    }

    /**
     * 收到群聊消息
     *
     * @param data
     */
    public static void receiveGroupMessages(String data) {
        System.out.println("[收到群聊消息]" + data);
        JSONObject json = JSONObject.parseObject(data);
        long selfQQ = json.getInteger("selfQQ");//框架QQ
        selfQ = json.getInteger("selfQQ");//框架QQ
        long fromGroup = json.getInteger("fromGroup");//群号
        long fromQQ = json.getInteger("fromQQ");//对方QQ
        String fromQQCard = json.getString("fromQQCardName");
        String msg = json.getString("msg");//消息内容
        if (fromGroup == 171271622 || fromGroup == 1131855207) {
            Controller.sendServerMessage("\2473[QQ]" + fromQQCard + "\2477:" + msg);
        }


    }

    public static void receiveEventMessages(String data) {
        System.out.println("[收到事件消息]" + data);
        JSONObject json = JSONObject.parseObject(data);
        long selfQQ = json.getInteger("selfQQ");//框架QQ
        selfQ = json.getInteger("selfQQ");//框架QQ
        long fromGroup = json.getInteger("fromGroup");//群号
        int msgType = json.getInteger("msgType");//类型
        long triggerQQ = json.getInteger("triggerQQ");//对方QQ
        //String triggerQQName = json.getString("triggerQQName");//对方昵称
        long seq = json.getLongValue("seq");//操作用

        //32表示QQ上线
        //17表示好友更改昵称
        //25表示邀请加入了群聊
        if (msgType == 3) {//群验证事件 申请入群
            Core.handleGroupEvent(selfQQ, fromGroup, triggerQQ, seq, 11, 3);
        } else if (msgType == 23) {
            Core.callpPraise(selfQQ, triggerQQ, 10);
        }
    }
}

