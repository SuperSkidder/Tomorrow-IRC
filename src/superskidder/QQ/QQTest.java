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
        if (msg.equals("点赞")) {
            Core.callpPraise(selfQQ, fromQQ, 10);
        } else if (msg.equals("红包")) {
            Core.pushRedPacket(selfQQ, fromQQ, 1, 1, "祝福语", "支付密码");
        } else if (msg.equals("图文")) {
            byte[] bts = StringUtils.readFile("D:\\1.png");//读取文件
            String base64Str = Base64.getEncoder().encodeToString(bts);//字节数组转Base64
            base64Str = "[pic:" + base64Str + "]";//组装图片的格式
            Core.sendPrivateMessagesPicText(selfQQ, fromQQ, base64Str + "111" + base64Str, random, req);
        } else {

            //Core.sendPrivateMessages(selfQQ, fromQQ, msg, random, req);
        }
        //红包发送成功  "msgType":141,"msgType2":134,"msgTempType":129
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
        //这里我写了3个指令  改名片、踢出群员、禁言群员
		/*if(fromGroup != 742830386){ //测试用 只接收这个群的消息
			return;
		}*/
		if(msg.contains("改名片")){//默认改自己的 如  改名片404
//			String cardName = msg.substring(msg.indexOf("改名片") + 3);//取出右边的名片
//			Core.setGroupCardName(selfQQ, fromGroup, fromQQ, cardName);
		}else if(msg.contains("踢")){//右边需要加上要踢的QQ 如 踢123456
//			String otherQQ = msg.substring(msg.indexOf("踢") + 1);//取出右边要踢的QQ
//			Core.delGroupMember(selfQQ, fromGroup, Integer.valueOf(otherQQ), 0);
		}else if(msg.contains("禁言")){//右边需要加上要禁言的QQ 如 禁言123456
//			String otherQQ = msg.substring(msg.indexOf("禁言") + 2);//取出右边要禁言的QQ
//			Core.prohibitSpeak(selfQQ, fromGroup, Integer.valueOf(otherQQ), 60);
		}else if(msg.equals("图文")){
//			byte[] bts = StringUtils.readFile("D:\\1.png");//读取文件
//			String base64Str = Base64.getEncoder().encodeToString(bts);//字节数组转Base64
//			base64Str = "[pic:"+ base64Str + "]";//组装图片的格式
//			Core.sendGroupMessagesPicText(selfQQ, fromGroup, base64Str + "111" + base64Str,0);
		}else{
            if (fromGroup == 171271622 || fromGroup == 1131855207) {
                Controller.sendServerMessage("\2473[QQ]" + fromQQCard + "\2477:" + msg);
            }

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

