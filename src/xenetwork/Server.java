package xenetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static xenetwork.Client.NS;
import static xenetwork.Client.USERS;
import static xenetwork.Client.COMMA;

public class Server implements Runnable, Client.XeServerCallbackGround {

    private String id;
    private int port;
    private ServerSocket[] servers = new ServerSocket[2];
    private boolean isThreadOn = true;

    protected static Map<String, Client> clients = new HashMap<String, Client>();
    protected static CopyOnWriteArrayList<String> clientsName = new CopyOnWriteArrayList<String>();
    private XeServerCallback xescb;

    @Override
    public void run() {
        try {
            servers[0] = new ServerSocket(port);
            servers[1] = new ServerSocket(port + 1);
            while (isThreadOn) {
                if(servers[0].isBound() && servers[1].isBound()){
                    Client newUser = new Client(servers[0].accept(), servers[1].accept(), this);
                }
            }
        } catch (Exception e) {
            //System.out.println(e);
        }
    }

    public interface XeServerCallback {
        public void hasNewUser(Client client);

        public void hasExitUser(Client client);

        public void streamStartedServer(Client client);

        public void streamContinuesServer(Client client);

        public boolean forwarding(Client from, Client to, XFileManager xFileManager);

        public boolean forwarding(Client from, Client to, MessageManager messageManager);

        public boolean forwarding(Client from, Client to, XFileUploadManager xFileUploadManager);

        public boolean forwarding(Client from, Client to, PathManager pathManager);
    }

    public Server(String id, int port, XeServerCallback xescb) {
        this.id = id;
        this.port = port;
        this.xescb = xescb;
        new Thread(this).start();
    }

    private void sendUsers() {
        String allusersname = "";
        synchronized (clientsName){
            for (String id : clientsName) {
                allusersname += id + COMMA;
            }
        }
        synchronized (clientsName){
            for (String id : clientsName) {
                synchronized (clients){
                    clients.get(id).sendCommand(USERS + NS + allusersname);
                }
            }
        }
    }

    public void close(){
        isThreadOn = false;
        try {
            servers[0].close();
        } catch (IOException e) {
        }
        try {
            servers[1].close();
        } catch (IOException e) {
        }
        for (String id:clientsName){
            Client c = clients.get(id);
            c.subClose();
            //hasExitUser(c);
        }
        clientsName.clear();
        clients.clear();
    }

    @Override
    public void hasNewUser(Client client) {
        xescb.hasNewUser(client);
        sendUsers();
    }

    @Override
    public void hasExitUser(Client client) {
        xescb.hasExitUser(client);
        synchronized (clients){
            clients.remove(client.getId());
        }
        synchronized (clientsName){
            clientsName.remove(client.getId());
        }
        sendUsers();
    }

    @Override
    public void streamStartedServer(Client client, Tracking tracking) {
        xescb.streamStartedServer(client);
    }

    @Override
    public void streamContinuesServer(Client client, Tracking tracking) {
        xescb.streamContinuesServer(client);
    }

    @Override
    public boolean forwarding(Client from, Client to, XFileManager xFileManager) {
        return xescb.forwarding(from, to, xFileManager);
    }

    @Override
    public boolean forwarding(Client from, Client to, MessageManager messageManager) {
        return xescb.forwarding(from, to, messageManager);
    }

    @Override
    public boolean forwarding(Client from, Client to, XFileUploadManager xFileUploadManager) {
        return xescb.forwarding(from, to, xFileUploadManager);
    }

    @Override
    public boolean forwarding(Client from, Client to, PathManager pathManager) {
        return xescb.forwarding(from, to, pathManager);
    }
}
