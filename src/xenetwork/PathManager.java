package xenetwork;

import java.io.File;

import static xenetwork.Client.AT;
import static xenetwork.Client.PATH;
import static xenetwork.Server.clients;

public class PathManager {

    private Client to;
    private Manager manager;
    private String[] paths;

    public String[] getPaths() {
        return paths;
    }

    public PathManager(Manager manager) {
        this.manager = manager;
        getListOfPath();
    }

    private void getListOfPath() {
        manager.getClient().getTracking().setDownloading(false);
        if(manager.getClient().isSubServer && (to = clients.get(manager.getFromTo())) != null){
            to.sendCommand(PATH + AT + manager.getTotalLength() + AT + manager.getClient().getId() + AT + manager.getPart()[3]);
        }else if(manager.getClient().isSubServer == false){
            if(manager.getPart()[3].contains(";")){
                paths = manager.getPart()[3].split(";");
                manager.getClient().xeccb.listOfPath(manager.getClient(), manager.getFromTo(), this);
            }else{
                String path = "";
                for (final File fileEntry : new File(manager.getPart()[3]).listFiles()) {
                    path += fileEntry.getAbsolutePath() + ";";
                }
                manager.getClient().sendCommand(PATH + AT + 1 + AT + manager.getFromTo() + AT + path);
            }
        }
    }
}
