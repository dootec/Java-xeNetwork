package xenetwork;

import static xenetwork.Client.AT;
import static xenetwork.Client.FILEDOWNLOAD;
import static xenetwork.Server.clients;

public class XFileUploadManager {

    private Manager manager;
    private String path;
    private Client to;

    public String getPath() {
        return path;
    }

    public XFileUploadManager(Manager manager) {
        this.manager = manager;
        this.path = manager.getPart()[3];
        manager.getClient().getTracking().setDownloading(false);
        if(manager.getClient().isSubServer == false && manager.getClient().xeccb.hasDownloadingFileOrFolder(manager.getClient(), manager.getFromTo(), path)){
            manager.getClient().sendFiles(manager.getFromTo(), manager.getPart()[3]);
        }else if(manager.getClient().isSubServer && manager.getClient().xescb.forwarding(manager.getClient(), clients.get(manager.getFromTo()), this) && (to = clients.get(manager.getFromTo())) != null){
            to.sendCommand(FILEDOWNLOAD + AT + 1 + AT + manager.getClient().getId() + AT + path);
        }
    }
}
