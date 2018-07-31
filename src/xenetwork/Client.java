package xenetwork;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static xenetwork.Server.clients;
import static xenetwork.Server.clientsName;

public class Client implements Runnable {

    private boolean priority = false;
    private static final byte[] lock = new byte[]{1, 2, 3, 2, 1};
    private static final short UPSPEED = 1024;
    private static final short maxBuffer = 5120;
    protected static final String TEXT = "TEXT";
    protected static final String PATH = "PATH";
    protected static final String FILE = "FILE";
    protected static final String FILEDOWNLOAD = "FILEDOWNLOAD";
    protected static final String USERS = "users";
    protected static final String AT = "@";
    protected static final String NS = "#";
    protected static final String COMMA = ",";
    private static final String OK = "ok";
    private static final String NO = "no";
    private static final String CONTROL = "#CONTROL#";
    private static final String CONTROL_APPROVAL = "#CONTROL_APPROVAL#";
    private static final String ENTER = "enter";
    private static final String EXIT = "exit";
    private static final String ENTER_OK = ENTER + NS + OK;
    private static final String ENTER_NO = ENTER + NS + NO;

    protected boolean isSubServer = false;
    private Tracking tracking = new Tracking();

    public Tracking getTracking() {
        return tracking;
    }

    private WorkPool workPool = new WorkPool(this);

    private String id;
    private char[] ports = new char[2];
    private String ip;

    private String paths = "";
    private String savePath;

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getSavePath() {
        return savePath;
    }

    private ArrayList<String> users = new ArrayList<>();
    protected XeServerCallbackGround xescb;
    protected XeClientCallback xeccb;
    private boolean isThreadOn = true;
    private boolean connection = false;

    private Socket clientSocket1;
    public DataInputStream in1;
    private DataOutputStream out1;

    private Socket clientSocket2;
    public DataInputStream in2;
    protected DataOutputStream out2;

    public String getId() {
        return id;
    }

    public char[] getPorts() {
        return ports;
    }

    public String getIp() {
        return ip;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public boolean isConnection() {
        return connection;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        while (isThreadOn) {
            action();
            if ((System.currentTimeMillis() - start) > 500) {
                start = System.currentTimeMillis();
                systickTimer();
            }
        }
    }

    private void systickTimer() {
        sendCommand("ECHO");
        workPool.nextIteration();
    }

    public interface XeClientCallback {
        public void statusConn(Client client, boolean status);

        public void statusID(Client client, boolean status);

        public void usersUpdated(Client client);

        public void streamStarted(Client client, Tracking tracking);

        public void streamContinues(Client client, Tracking tracking);

        public void hasMessage(Client client, String fromID, String message);

        public boolean hasFile(Client client, String fromID, XFileManager xFManager);

        public boolean hasDownloadingFileOrFolder(Client client, String fromID, String path);

        public void listOfPath(Client client, String fromID, PathManager pathManager);
    }

    public interface XeServerCallbackGround {
        public void hasNewUser(Client client);

        public void hasExitUser(Client client);

        public void streamStartedServer(Client client, Tracking tracking);

        public void streamContinuesServer(Client client, Tracking tracking);

        public boolean forwarding(Client from, Client to, XFileManager xFileManager);

        public boolean forwarding(Client from, Client to, MessageManager messageManager);

        public boolean forwarding(Client from, Client to, XFileUploadManager xFileUploadManager);

        public boolean forwarding(Client from, Client to, PathManager pathManager);
    }

    public Client(String id, String ip, char port, XeClientCallback xeccb) {
        try {
            isSubServer = false;
            isThreadOn = true;
            this.id = id;
            this.ip = ip;
            ports[0] = port;
            ports[1] = (char) (port + 1);
            this.xeccb = xeccb;
            clientSocket1 = new Socket(ip, port);
            clientSocket2 = new Socket(ip, port + 1);
            in1 = new DataInputStream(clientSocket1.getInputStream());
            out1 = new DataOutputStream(clientSocket1.getOutputStream());
            in2 = new DataInputStream(clientSocket2.getInputStream());
            out2 = new DataOutputStream(clientSocket2.getOutputStream());
            sendCommand("enter#" + id);
            xeccb.statusConn(this, true);
            new Thread(this).start();
            savePath = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\" + id;
        } catch (Exception e) {
            xeccb.statusConn(this, false);
        }
    }

    protected Client(Socket clientSocket1, Socket clientSocket2, XeServerCallbackGround xescb) {
        isSubServer = true;
        id = "subserver";
        this.ip = clientSocket1.getInetAddress().toString();
        ports[0] = (char) clientSocket1.getLocalPort();
        ports[1] = (char) clientSocket2.getLocalPort();
        this.clientSocket1 = clientSocket1;
        this.clientSocket2 = clientSocket2;
        this.xescb = xescb;
        try {
            in1 = new DataInputStream(clientSocket1.getInputStream());
            out1 = new DataOutputStream(clientSocket1.getOutputStream());
            in2 = new DataInputStream(clientSocket2.getInputStream());
            out2 = new DataOutputStream(clientSocket2.getOutputStream());
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendCommand(String command) {
        try {
            byte[] bytes = command.getBytes();
            out1.write(bytes);
            out1.write(lock);
            out1.flush();
        } catch (Exception e) {
            secureClose(false);
            //e.printStackTrace();
        }
    }

    protected void sendBytes(byte[] bytes) {
        try {
            out2.write(bytes);
            out2.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(String to, String message) {
        workPool.addNew(TEXT, to, message);
    }

    protected boolean coreSendMessage(String to, String message) {
        if (control()) {
            byte[] bytes = message.getBytes();
            sendCommand("TEXT@" + bytes.length + "@" + to);
            sendBytes(bytes);
            return true;
        } else {
            System.out.println("mesaj gonderilemedi : " + message);
        }
        return false;
    }


    public void sendFiles(String to, String filePath) {
        workPool.addNew(FILE, to, filePath);
    }

    protected void coreSendFile(String to, String filePath) {
        new Thread(() -> {
            File file = new File(filePath);
            String parent = file.getParent();
            if (file.getName().indexOf('.') != -1) {
                //file
                try {
                    long size = Files.size(new File(filePath).toPath());
                    sendCommand(FILE + AT + size + AT + to + AT + filePath.replace(parent, "") + "*" + size + ";");
                    sendFile(filePath, 0, size);
                    getTracking().setUploading(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //folder
                paths = "";
                listFilesForFolder(file);
                String totalCode = "";
                long totalLength = 0;
                String[] arrayPath = paths.split(";");
                for (String path : arrayPath) {
                    long i = new File(path).length();
                    totalCode += path.replace(parent, "") + "*" + i + ";";
                    totalLength += i;
                }
                sendCommand(FILE + AT + totalLength + AT + to + AT + totalCode);
                long total = 0;
                for (String path : arrayPath) {
                    try {
                        long len = new File(path).length();
                        sendFile(path, total, totalLength);
                        total += len;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                getTracking().setUploading(false);
            }
        }).start();
    }
    protected void sendFile(String filePath, long current, long totalLength) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            char maxBufferCounter = 0;
            byte[] bytes = new byte[maxBuffer];
            long filesLength = Files.size(new File(filePath).toPath());
            long bytesLeft = filesLength;
            float upSpeed, upNowPercentage, upTotalPercentage;
            long uploadedBytes = 0, previousUploadedBytes = 0, totalUploaded = 0;
            long oldtime = System.currentTimeMillis();
            while (bytesLeft > 0) {
                if (maxBufferCounter == maxBuffer) {
                    out2.write(bytes);
                    out2.flush();
                    maxBufferCounter = 0;
                    bytes = new byte[maxBuffer];
                }
                char nextPacketSize = (char) ((bytesLeft > UPSPEED) ? UPSPEED : bytesLeft);
                fis.read(bytes, maxBufferCounter, nextPacketSize);
                if (nextPacketSize < UPSPEED) {
                    out2.write(bytes, 0, maxBufferCounter + nextPacketSize);
                    out2.flush();
                }
                if ((System.currentTimeMillis() - oldtime) > 1000) {
                    oldtime = System.currentTimeMillis();
                    upSpeed = (float) ((float) (uploadedBytes - previousUploadedBytes) / 1000000.0);
                    upNowPercentage = (float) (100.0 * ((float) uploadedBytes / (float) filesLength));
                    upTotalPercentage = (float) (100.0 * (float) (current + uploadedBytes) / (float) totalLength);
                    totalUploaded += uploadedBytes - previousUploadedBytes;
                    getTracking().setUpProperties(upSpeed, upNowPercentage, upTotalPercentage, totalUploaded);
                    if (isSubServer == false) {
                        xeccb.streamContinues(this, getTracking());
                    } else {
                        xescb.streamContinuesServer(this, getTracking());
                    }
                    previousUploadedBytes = uploadedBytes;
                }
                uploadedBytes += nextPacketSize;
                maxBufferCounter += nextPacketSize;
                bytesLeft -= nextPacketSize;
            }
            upSpeed = 0;
            upNowPercentage = 100;
            upTotalPercentage = (float) (100.0 * (float) (current + uploadedBytes) / (float) totalLength);
            totalUploaded += uploadedBytes - previousUploadedBytes;
            getTracking().setUpProperties(upSpeed, upNowPercentage, upTotalPercentage, totalUploaded);
            if (isSubServer == false) {
                xeccb.streamContinues(this, getTracking());
            } else {
                xescb.streamContinuesServer(this, getTracking());
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getFiles(String from, String filePath) {
        workPool.addNew(FILEDOWNLOAD, from, filePath);
    }

    protected void coreGetFiles(String from, String filePath) {
        sendCommand(FILEDOWNLOAD + AT + 1 + AT + from + AT + filePath);
    }

    private void listFilesForFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                paths += fileEntry.getAbsolutePath() + ";";
            }
        }
    }

    private void action() {
        String[] s;
        String data;
        try {
            if (priority == false) {
                if ((data = readCommand()) != ".") {
                    if (data.contains(AT)) {
                        s = data.split(AT);
                        getTracking().setDownloading(true);
                        new Manager(this, s);
                    } else {
                        if (isSubServer) {
                            s = data.split(NS);
                            if (data.equals(CONTROL)) {
                                sendCommand(CONTROL_APPROVAL);
                            } else if (s[0].equals(ENTER)) {
                                id = s[1];
                                if (clientsName.indexOf(id) == -1) {
                                    synchronized (clients) {
                                        clients.put(id, this);
                                    }
                                    synchronized (clientsName) {
                                        clientsName.add(id);
                                    }
                                    xescb.hasNewUser(this);
                                    sendCommand(ENTER_OK);
                                } else {
                                    sendCommand(ENTER_NO);
                                }
                            } else if (s[0].equals(EXIT)) {
                                secureClose(true);
                            }
                        } else {
                            s = data.split(NS);
                            if (s[0].equals(ENTER)) {
                                if (s[1].equals(OK)) {
                                    connection = true;
                                    xeccb.statusID(this, true);
                                } else if (s[1].equals(NO)) {
                                    xeccb.statusID(this, false);
                                }
                            } else if (s[0].equals(EXIT)) {
                                secureClose(false);
                            } else if (s[0].equals(USERS)) {
                                synchronized (users) {
                                    users.clear();
                                    s = data.split(NS)[1].split(COMMA);
                                    for (String id : s) {
                                        users.add(id);
                                    }
                                }
                                xeccb.usersUpdated(this);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    protected void secureClose(boolean flag) {
        if (isSubServer) {
            subClose();
        } else {
            close(flag);
        }
    }
    public void close(){
        close(true);
    }
    protected void close(boolean flag) {
        connection = false;
        if (flag) {
            sendCommand(EXIT + NS + id);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isThreadOn = false;
        users.clear();
        try {
            clientSocket1.shutdownOutput();
        } catch (Exception e) {
        }
        try {
            clientSocket2.shutdownOutput();
        } catch (Exception e) {
        }
        try {
            clientSocket1.shutdownInput();
        } catch (Exception e) {
        }
        try {
            clientSocket2.shutdownInput();
        } catch (Exception e) {
        }
        try {
            out1.close();
        } catch (Exception e) {
        }
        try {
            out2.close();
        } catch (Exception e) {
        }
        try {
            in1.close();
        } catch (Exception e) {
        }
        try {
            in2.close();
        } catch (Exception e) {
        }
        try {
            clientSocket1.close();
        } catch (Exception e) {
        }
        try {
            clientSocket2.close();
        } catch (Exception e) {
        }
        xeccb.statusConn(this, false);
    }
    protected void subClose() {
        connection = false;
        isThreadOn = false;
        try {
            clientSocket1.shutdownOutput();
        } catch (Exception e) {
        }
        try {
            clientSocket2.shutdownOutput();
        } catch (Exception e) {
        }
        try {
            clientSocket1.shutdownInput();
        } catch (Exception e) {
        }
        try {
            clientSocket2.shutdownInput();
        } catch (Exception e) {
        }
        try {
            out1.close();
        } catch (Exception e) {
        }
        try {
            out2.close();
        } catch (Exception e) {
        }
        try {
            in1.close();
        } catch (Exception e) {
        }
        try {
            in2.close();
        } catch (Exception e) {
        }
        try {
            clientSocket1.close();
        } catch (Exception e) {
        }
        try {
            clientSocket2.close();
        } catch (Exception e) {
        }
        xescb.hasExitUser(this);
    }


    public void getListOfFile(String from, String path) {
        workPool.addNew(PATH, from, path);
    }
    protected void coreGetListOfFile(String from, String path) {
        sendCommand(PATH + AT + 1 + AT + from + AT + path);
    }


    protected boolean downloadingControl() {
        if (getTracking().isDownloading() == false /*&& getTracking().isUploading() == false*/) {
            getTracking().setDownloading(true);
            if (control()) {
                return true;
            }
        }
        return false;
    }
    protected boolean uploadingControl() {
        if (getTracking().isUploading() == false /*&& getTracking().isDownloading() == false*/) {
            getTracking().setUploading(true);
            if (control()) {
                return true;
            }
        }
        return false;
    }

    private boolean control() {
        priority = true;
        sendCommand(CONTROL);
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < 200) {
            if (readCommand().equals(CONTROL_APPROVAL)) {
                priority = false;
                return true;
            }
        }
        priority = false;
        return false;
    }

    private String readCommand() {
        try {
            if (in1.available() > 0) {
                byte[] tamp = new byte[5120];
                byte[] newBytes = new byte[5];
                char j = 0;
                while (true) {
                    tamp[j] = in1.readByte();
                    for (int i = 1; i < 5; i++) {
                        newBytes[i - 1] = newBytes[i];
                    }
                    newBytes[4] = tamp[j];
                    j++;
                    if (Arrays.equals(lock, newBytes)) {
                        return new String(tamp, 0, j - 5);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ".";
    }
}
