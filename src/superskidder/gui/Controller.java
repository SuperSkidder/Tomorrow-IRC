package superskidder.gui;

import com.jfoenix.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import superskidder.QQ.Core;
import superskidder.QQ.QQTest;
import superskidder.utils.TimerUtil;
import superskidder.User;
import superskidder.utils.IRCUtils;
import superskidder.utils.packets.IRCPacket;
import superskidder.utils.packets.IRCType;
import superskidder.utils.packets.clientside.ClientChatPacket;
import superskidder.utils.packets.clientside.ClientConnectPacket;
import superskidder.utils.packets.clientside.ClientHeartPacket;
import superskidder.utils.packets.serverside.ServerChatPacket;
import superskidder.utils.packets.serverside.ServerHeartNeededPacket;
import superskidder.utils.packets.serverside.ServerStopPacket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    private Label title;
    @FXML
    private JFXButton launch;
    @FXML
    private JFXTextField port;
    @FXML
    private JFXTextField max;
    @FXML
    private JFXTextArea log;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        addLog("准备就绪");
        Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
            public void run() {
                closeServer();
            }
        });
    }


    @FXML
    private void launchirc() {
        if (!isStart) {
            launch.setText("Stop IRC Server");
        } else {
            launch.setText("Launch IRC");
        }
        if (!isStart) {
            try {
                serverStart(Integer.valueOf(max.getText()), Integer.valueOf(port.getText()));
            } catch (BindException e) {
                showMessage("警告", "端口号已经被占用，请换一个");
//            e.printStackTrace();
            }
        } else {
            closeServer();
        }

    }

    private void showMessage(String tit, String content) {
        new DialogBuilder(title).setTitle(tit).setMessage(content).setNegativeBtn("确定").create();
    }

    public void addLog(String text) {
        String date = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
        log.setText(log.getText() + "\n" + "[" + date + "]" + text);
    }


    //Server

    private ServerSocket serverSocket;
    private ServerThread serverThread;
    private static ArrayList<ClientThread> clients;
    public TimerUtil timer = new TimerUtil();

    public static String prefix = "\247d[IRC]\2477";

    private boolean isStart = false;
    private Map<ClientThread, Integer> needHeartsUsers = new HashMap<>();


    //启动服务器
    public void serverStart(int max, int port) throws BindException {
        try {

            clients = new ArrayList<>();
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket, max);
            serverThread.start();
            new HeartPacket().start();
            addLog("服务器启动成功");

            isStart = true;
        } catch (BindException e) {
            isStart = false;
            throw new BindException("端口号已被占用，请换一个！");
        } catch (Exception e1) {
            e1.printStackTrace();
            isStart = false;
            throw new BindException("启动服务器异常！");
        }
    }


    //关闭服务器
    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();//停止服务器线程
            if (QQTest.clientTest != null)
                QQTest.clientTest.stop();

            for (int i = clients.size() - 1; i >= 0; i--) {
                //给所有在线用户发送关闭命令

                clients.get(i).getWriter().println(IRCUtils.toJson(new ServerStopPacket(System.currentTimeMillis(), "Server closed because [ SOME REASON ]")));
                clients.get(i).getWriter().flush();
                //释放资源
                clients.remove(i);
                System.gc();
            }
            if (serverSocket != null) {
                serverSocket.close();//关闭服务器端连接
            }
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
        System.gc();
        addLog("Server closed");
        log.clear();
    }

    //群发服务器消息
    public static void sendServerMessage(String message) {
        if(clients.size() <= 0)
            return;
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).getWriter().println(IRCUtils.toJson(new ServerChatPacket(System.currentTimeMillis(), message)));
            clients.get(i).getWriter().flush();
        }
    }


    //心跳包线程
    class HeartPacket extends Thread {
        public long lastSendTime = 0L;

        @Override
        public void run() {
            while (isStart) {
                if (timer.delay(5000L)) {
                    List<ClientThread> theHeartStopsBeatingUsers = new ArrayList<>();
                    mainLooping:
                    for (ClientThread ct : clients) {
                        if (needHeartsUsers.isEmpty()) {
                            ct.getWriter().println(IRCUtils.toJson(new ServerHeartNeededPacket(System.currentTimeMillis(), "HEARTNEEDED")));
                            ct.getWriter().flush();
                            //for (ClientThread client : clients) {
                            needHeartsUsers.put(ct, 0);
                            //}
                            lastSendTime = System.nanoTime() / 1000000L;
                        } else {
                            for (ClientThread ct2 : needHeartsUsers.keySet()) {
                                if (needHeartsUsers.get(ct2) < 3) {
                                    // 似乎会逐渐偏移 然后就错以为是未发送心跳 只能这样了555
                                    ct.getWriter().println(IRCUtils.toJson(new ServerHeartNeededPacket(System.currentTimeMillis(), "HEARTNEEDED")));
                                    ct.getWriter().flush();
                                    needHeartsUsers.put(ct2, needHeartsUsers.get(ct2) + 1);
                                    continue mainLooping;
                                }
                                theHeartStopsBeatingUsers.add(ct2);
                                addLog("客户端超时: " + ct2.getUser().getAuthName());
                            }

                            needHeartsUsers.clear();
                        }
                    }

                    for (ClientThread theHeartStopsBeatingUser : theHeartStopsBeatingUsers) {
                        theHeartStopsBeatingUser.stop();
                        clients.remove(theHeartStopsBeatingUser);
                    }

                    timer.reset();
                }


            }


        }
    }


    //服务器线程
    class ServerThread extends Thread {
        private final ServerSocket serverSocket;
        private final int max;//人数上限

        //服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket, int max) {
            this.serverSocket = serverSocket;
            this.max = max;
        }

        @Override
        public void run() {
            super.run();
            while (true) {//不停的等待客户端的链接

                try {
                    Socket socket = serverSocket.accept();

                    ClientThread client = new ClientThread(socket);
                    clients.add(client);
                    client.start();//开启对此客户端服务的线程
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    //为一个客户端服务的线程
    class ClientThread extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private User user;

        public PrintWriter getWriter() {
            return writer;
        }

        public User getUser() {
            return user;
        }

        //客户端线程的构造方法
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new MyBufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new MyPrintWriter(socket.getOutputStream());
                //接收客户端的基本用户信息
                String message = reader.readLine();
                IRCPacket packet = IRCUtils.toPacket(message, IRCPacket.class);
                if (packet.type.equals(IRCType.CHAT)) {
                    ClientChatPacket c = (ClientChatPacket) IRCUtils.toPacket(message, ClientChatPacket.class);
                } else if (packet.type.equals(IRCType.HEART)) {
                    ClientHeartPacket c = (ClientHeartPacket) IRCUtils.toPacket(message, ClientHeartPacket.class);
                    needHeartsUsers.remove(this);
                } else if (packet.type.equals(IRCType.CONNECT)) {
                    ClientConnectPacket c = (ClientConnectPacket) IRCUtils.toPacket(message, ClientConnectPacket.class);
                    user = new User(c.username, "none", "w", c.gameID);
                    dispatcherMessage(new ClientChatPacket(System.currentTimeMillis(), prefix + "User:" + user.getAuthName() + "joined IRC Server!"));
                    Core.sendGroupMessages(1186475932, 171271622, "[IRC]" + c.content, 0);
                    Core.sendGroupMessages(1186475932, 1131855207, "[IRC]" + c.content, 0);

                }

                //反馈连接成功信息

                //向所有在线用户发送该用户上线命令
//                for (int i = clients.size() - 1; i >= 0; i--) {
//                    clients.get(i).getWriter().println(prefix + "User:" + user.getAuthName() + "joined IRC Server!");
//                    clients.get(i).getWriter().flush();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String get(String string) throws Exception {
            URL url = new URL(string);

            HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();
            httpurlconnection.setRequestMethod("GET");
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
            StringBuilder stringbuilder = new StringBuilder();
            String s;

            while ((s = bufferedreader.readLine()) != null) {
                stringbuilder.append(s);
                stringbuilder.append('\r');
            }

            bufferedreader.close();
            return stringbuilder.toString();
        }

        public void run() {//不断接收客户端的消息，进行处理。
            String message;

            while (true) {
                try {
                    message = reader.readLine();//接收客户端消息

                    if (message == null)
                        return;

                    IRCPacket packet = IRCUtils.toPacket(message, IRCPacket.class);
                    if (packet.type.equals(IRCType.CHAT)) {
                        ClientChatPacket c = (ClientChatPacket) IRCUtils.toPacket(message, ClientChatPacket.class);
                        dispatcherMessage(c);
                        addLog(c.content);
                        Core.sendGroupMessages(1186475932, 171271622, "[IRC]" + c.content, 0);
                        Core.sendGroupMessages(1186475932, 1131855207, "[IRC]" + c.content, 0);
                    } else if (packet.type.equals(IRCType.HEART)) {
                        ClientHeartPacket c = (ClientHeartPacket) IRCUtils.toPacket(message, ClientHeartPacket.class);
                        needHeartsUsers.remove(this);
                    } else if (packet.type.equals(IRCType.CONNECT)) {
                        ClientConnectPacket c = (ClientConnectPacket) IRCUtils.toPacket(message, ClientConnectPacket.class);
                        user = new User(c.username, "none", "w", c.gameID);
                        dispatcherMessage(new ServerChatPacket(System.currentTimeMillis(), prefix + user.getAuthName() + "\247a joined successfully!"));
                        addLog("[" + this.user.getAuthName() + "] joined successfully!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //转发消息
        public void dispatcherMessage(IRCPacket message) {
            for (int i = clients.size() - 1; i >= 0; i--) {
                clients.get(i).getWriter().println(IRCUtils.toJson(message));
                clients.get(i).getWriter().flush();
            }
        }
    }

    static class MyPrintWriter extends PrintWriter {
        public MyPrintWriter(Writer out) {
            super(out);
        }

        public MyPrintWriter(Writer out, boolean autoFlush) {
            super(out, autoFlush);
        }

        public MyPrintWriter(OutputStream out) {
            super(out);
        }

        public MyPrintWriter(OutputStream out, boolean autoFlush) {
            super(out, autoFlush);
        }

        public MyPrintWriter(String fileName) throws FileNotFoundException {
            super(fileName);
        }

        public MyPrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
            super(fileName, csn);
        }

        public MyPrintWriter(File file) throws FileNotFoundException {
            super(file);
        }

        public MyPrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
            super(file, csn);
        }

        @Override
        public void println(String x) {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] msgByte = x.getBytes(StandardCharsets.UTF_8);

            x = encoder.encodeToString(msgByte);

            super.println(x);
        }
    }

    static class MyBufferedReader extends BufferedReader {
        public MyBufferedReader(Reader in, int sz) {
            super(in, sz);
        }

        public MyBufferedReader(Reader in) {
            super(in);
        }

        @Override
        public String readLine() throws IOException {
            try {
                String msg = super.readLine();
                msg = this.cleanStr(msg);

                Base64.Decoder decoder = Base64.getDecoder();
                msg = new String(decoder.decode(msg), StandardCharsets.UTF_8);

                msg = this.cleanStr(msg);
                return msg;
            } catch (Exception e) {

            }
            return null;
        }

        public String cleanStr(String str) {
            try {
                str = str.replaceAll("\n", "");
                str = str.replaceAll("\r", "");
                str = str.replaceAll("\t", "");
            } catch (Exception e) {
            }
            return str;
        }
    }
}
