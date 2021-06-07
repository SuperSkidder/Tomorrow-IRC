package superskidder.QQ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.alibaba.fastjson.JSONObject;
/**
 * 通信类
 * @author zhaoqk
 *
 * 2020年8月10日 下午5:07:31
 */
public class ChatClient extends Thread{

    //定义一个Socket对象
    Socket socket = null;
    public ChatClient() {

    }
    public ChatClient(String host, int port) {
        try {
            //需要服务器的IP地址和端口号，才能获得正确的Socket对象
            socket = new Socket(host, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        super.run();
        try {
            InputStream s = socket.getInputStream();
            byte[] buf = new byte[128];
            int len = 0;
            String temp = "";
            while ((len = s.read(buf)) != -1) {
                String data = new String(buf, 0, len);
                temp += data;
                if(len < 128){
                    String temp2 = temp;
                    temp = "";
                    parseData(temp2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseData(String data){
        if(data.equals("")){
            return;
        }
        //去重  %7B%7D
        if(data.lastIndexOf("%7B")> 0){
            data = data.substring(data.lastIndexOf("%7B"));
        }
        data = StringUtils.getURLDecoderString(data);
        try{
            JSONObject json = JSONObject.parseObject(data);
            if(json.getInteger("type") == 0){//连接成功
                System.out.println("[连接成功]");
            }else if(json.getInteger("type") == 1){//好友消息
                QQTest.receivePrivateMessages(json.toJSONString());
            }else if(json.getInteger("type") == 2){//群聊消息
                QQTest.receiveGroupMessages(json.toJSONString());
            }else if(json.getInteger("type") == 3){//事件消息
                QQTest.receiveEventMessages(json.toJSONString());
            }
        }catch (Exception e) {
            System.out.println("错误数据" + data);
            e.printStackTrace();
        }

    }


    //写操作
    public void sendMsg(String msg){
        OutputStream os= null;
        try {
            os= socket.getOutputStream();
            os.write(msg.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

