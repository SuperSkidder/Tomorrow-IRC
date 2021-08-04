package superskidder.gui;

import com.jfoenix.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
    @FXML
    private JFXTextField users;

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
    private ArrayList<ClientThread> needHeartsUsers = new ArrayList<>();


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
        if (clients.size() <= 0)
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
            try {

                while (isStart) {
                    if (timer.delay(1500L)) {
                        mainLooping:
                        if (needHeartsUsers.isEmpty()) {
                            for (ClientThread ct : clients) {
                                ct.getWriter().println(IRCUtils.toJson(new ServerHeartNeededPacket(System.currentTimeMillis(), IRCUtils.toJson(ct.user))));
                                ct.getWriter().flush();
                                System.out.println("HeartNeeded:" + ct.user.authName);
                                needHeartsUsers.add(ct);
                                lastSendTime = System.nanoTime() / 1000000L;
                            }
                        } else {
                            ArrayList<ClientThread> remove = new ArrayList();
                            for (ClientThread c : needHeartsUsers) {
                                if (c.resend < 3) {
                                    c.getWriter().println(IRCUtils.toJson(new ServerHeartNeededPacket(System.currentTimeMillis(), IRCUtils.toJson(c.user))));
                                    c.getWriter().flush();
                                    System.out.println("HeartNeeded (R):" + c.user.authName);
                                    c.resend++;
                                } else {
                                    addLog("客户端超时: " + c.getUser().getAuthName());
                                    c.shutdown();
                                    remove.add(c);
                                }
                            }
                            for (ClientThread ct : remove) {
                                needHeartsUsers.remove(ct);
                            }
                        }
                        timer.reset();

                    }


                }

            } catch (Exception e1) {
                System.out.println("Heart Packet报错！");
                new HeartPacket().start();
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
                    client.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    //为一个客户端服务的线程
    class ClientThread extends Thread {
        public int resend = 0;
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
                    user = new User(c.username, c.password, c.hwid, "");
                    //验证

                    int flag = 0;
                    for (String s : readFile(users.getText()).split(System.lineSeparator())) {
                        if (s.equals("USERNAME=" + c.username) || s.equals("PWD=" + c.password) || s.equals("HWID=" + c.hwid)) {
                            flag++;
                        }
                    }
                    clients.add(this);

                    if (flag == 3) {
                        addLog(c.username + " " + c.password + " Login Successfully");
                    } else {
                        addLog(c.username + " " + c.password + " Login failed");
                        shutdown();
                        return;
                    }

                    dispatcherMessage(new ServerChatPacket(System.currentTimeMillis(), prefix + user.getAuthName() + "\247a joined successfully!"));
                    addLog("[" + this.user.getAuthName() + "] joined successfully!");
                    user.connected = true;
//                    Core.sendGroupMessages(QQTest.selfQ, 171271622, "[IRC]" + c.content, 0);
//                    Core.sendGroupMessages(QQTest.selfQ, 1131855207, "[IRC]" + c.content, 0);

                }

                //反馈连接成功信息

                //向所有在线用户发送该用户上线命令
//                for (int i = clients.size() - 1; i >= 0; i--) {
//                    clients.get(i).getWriter().println(prefix + "User:" + user.getAuthName() + "joined IRC Server!");
//                    clients.get(i).getWriter().flush();
//                }
            } catch (Exception e) {
                e.printStackTrace();
                addLog("心跳线程报错:" + e.getMessage());
                new HeartPacket().start();
            }
        }

        public String readFile(String fileName) {
            StringBuilder result = new StringBuilder();

            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileInputStream fIn = new FileInputStream(file);
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fIn))) {
                    String str;
                    while ((str = bufferedReader.readLine()) != null) {
                        result.append(str);
                        result.append(System.lineSeparator());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
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
//                        Core.sendGroupMessages(QQTest.selfQ, 171271622, "[IRC]" + c.content, 0);
//                        Core.sendGroupMessages(QQTest.selfQ, 1131855207, "[IRC]" + c.content, 0);
                    } else if (packet.type.equals(IRCType.HEART)) {
                        ClientHeartPacket c = (ClientHeartPacket) IRCUtils.toPacket(message, ClientHeartPacket.class);
                        User u = (User) IRCUtils.toObject(c.content, User.class);
                        this.user.GameID = u.getGameID();
                        this.user.head = u.head;
                        needHeartsUsers.remove(this);
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

        public void shutdown() {
            this.writer.println(IRCUtils.toJson(new ServerStopPacket(System.currentTimeMillis(), "Server closed because [ Heart Packet ]")));

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(this);
            this.stop();
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
