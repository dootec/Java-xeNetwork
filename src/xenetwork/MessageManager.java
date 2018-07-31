package xenetwork;

import static xenetwork.Client.AT;
import static xenetwork.Client.TEXT;
import static xenetwork.Server.clients;

public class MessageManager {

    private Manager manager;
    private Client to;
    private boolean forwarding = false;
    private String message;

    public String getMessage() {
        return message;
    }

    public MessageManager(Manager manager) {
        this.manager = manager;
        download();
    }

    protected void download(){
        try {
            byte[] bytes = new byte[(int) manager.getTotalLength()];
            manager.getClient().in2.read(bytes);
            message = new String(bytes);
            manager.getClient().getTracking().setDownloading(false);
            if(manager.getClient().isSubServer && (to = clients.get(manager.getFromTo())) != null && manager.getClient().xescb.forwarding(manager.getClient(), to, this)){
                to.sendCommand(TEXT + AT + manager.getTotalLength() + AT + manager.getClient().getId());
                to.sendBytes(bytes);
            }else if(manager.getClient().isSubServer == false){
                manager.getClient().xeccb.hasMessage(manager.getClient(), manager.getFromTo(), message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
