package example;

import xenetwork.*;

public class test implements Server.XeServerCallback, Client.XeClientCallback {

    private static Server server;
    private static Client user1, user2;


    public static void main(String[] args) {

        System.out.println("The program started.");

        server = new Server("xeNetwork", 8904, new test());
        user1 = new Client("Ege", "127.0.0.1", (char) 8904, new test());
        user2 = new Client("Foo", "127.0.0.1", (char) 8904, new test());

        while(!user1.isConnection());
        while(!user2.isConnection());

        user1.setSavePath("C:\\Users\\egegu\\Documents\\" + user1.getId());
        user2.setSavePath("C:\\Users\\egegu\\Documents\\" + user2.getId());

        user1.sendMessage("Foo", "Hi, How are you?");
        user1.getListOfFile("Foo", "C:\\");
        user1.sendFiles("Foo", "C:\\Users\\egegu\\Documents\\MyProjects");
        user1.sendFiles("Foo", "C:\\Users\\egegu\\Documents\\Holiday\\Episode1\\Day1\\movie1.mp4");
        user1.getFiles("Foo", "C:\\Pictures");
        user1.getFiles("Foo", "C:\\Pictures\\MyCam\\rose.png");

        System.out.println("The Program Stopped.");
    }

    @Override
    public void statusConn(Client client, boolean status) {
        if (status == false) {
            System.out.println(client.getId() + "'s connection couldn't established. If you sure connection parameters, server might be offline.");
        }
    }

    @Override
    public void statusID(Client client, boolean status) {
        if (status == false) {
            System.out.println("The username you selected for " + client.getId() + " is not available. If you want to continue, use the changeID method, which changes the username.");
        }
    }

    @Override
    public void usersUpdated(Client client) {
        System.out.println("Users list has updated, all users: " + client.getUsers());
    }

    @Override
    public void streamStarted(Client client, Tracking tracking) {

    }

    @Override
    public void streamContinues(Client client, Tracking tracking) {
         if(client.getTracking().isDownloading()){
            System.err.println("Speed : " + tracking.convert(tracking.getDownSpeed(), 1) + "MB/s\tBytes : " + tracking.convert(client.getTracking().getTotalDownloaded(), 1) + "\tProgress Now : " + tracking.convert(client.getTracking().getNowPercentage(), 1) + "%\tProgress Total : " + tracking.convert(client.getTracking().getTotalPercentage(), 1) + "%");
        }
        if(client.getTracking().isUploading()){
            System.out.println("Speed : " + tracking.convert(tracking.getUpSpeed(), 1) + "MB/s\tBytes : " + tracking.convert(client.getTracking().getTotalUploaded(), 1) + "\tProgress Now : " + tracking.convert(client.getTracking().getUpNowPercentage(), 1) + "%\tProgress Total : " + tracking.convert(client.getTracking().getUpTotalPercentage(), 1) + "%");
        }
    }

    @Override
    public void hasMessage(Client client, String fromID, String message) {
        System.out.println("Heyy '" + client.getId() + "' has your new message from '" + fromID + "' and its message : '" + message + "'");
    }

    @Override
    public boolean hasFile(Client client, String fromID, XFileManager flow) {
        return true;
    }

    @Override
    public boolean hasDownloadingFileOrFolder(Client client, String fromID, String path) {
        System.out.println("Hey " + client.getId() + ", " + fromID + " want to download your this path---> " + path);
        return true;
    }

    @Override
    public void listOfPath(Client client, String fromID, PathManager pathManager) {
        for (String path : pathManager.getPaths()){
            if(path.contains(".")){
                System.err.println(path);
            }else{
                System.out.println(path);
            }
        }
    }

    @Override
    public void hasNewUser(Client client) {
        System.err.println("'" + client.getId() + "' is connected to the server.");
    }

    @Override
    public void hasExitUser(Client client) {
        System.err.println("'" + client.getId() + "' is logged out of the server.");
    }

    @Override
    public void streamStartedServer(Client client) {

    }

    @Override
    public void streamContinuesServer(Client client) {
        //System.out.println("[SERVER]Downloading Info :\n\nSpeed : " + client.getDownloadTracking()[0] + "Kb/s\nBytes : " + client.getDownloadTracking()[3] + "\nProgress Now : " + client.getDownloadTracking()[1] + "%\nProgress Total : " + client.getDownloadTracking()[2] + "%");
    }

    @Override
    public boolean forwarding(Client from, Client to, XFileManager xFileManager) {
        return true;
    }

    @Override
    public boolean forwarding(Client from, Client to, MessageManager messageManager) {
        return true;
    }

    @Override
    public boolean forwarding(Client from, Client to, XFileUploadManager xFileUploadManager) {
        return true;
    }

    @Override
    public boolean forwarding(Client from, Client to, PathManager pathManager){return true;}

}
