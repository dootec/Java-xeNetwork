package xenetwork;

import java.util.ArrayList;

import static xenetwork.Client.*;

class WorkPool {

    private ArrayList<String> list = new ArrayList<>();
    private Client client;

    protected WorkPool(Client client) {
        this.client = client;
    }

    protected void addNew(String type, String content1, String content2) {
        if (client.isConnection()) {
            synchronized (list) {
                list.add(type + AT + content1 + AT + content2);
            }
            nextIteration();
        }
    }

    protected void nextIteration() {
        if (client.isConnection()) {
            String[] s;
            synchronized (list) {
                if (list.size() > 0) {
                    s = list.get(0).split(AT);
                    //s = list.get(list.size()-1).split(AT);
                    switch (s[0]) {
                        case FILE:
                            if (client.uploadingControl()) {
                                list.remove(0);
                                client.coreSendFile(s[1], s[2]);
                            }
                            break;
                        case TEXT:
                            if (client.coreSendMessage(s[1], s[2])) {
                                list.remove(0);
                            }
                            break;
                        case FILEDOWNLOAD:
                            if(client.downloadingControl()){
                                list.remove(0);
                                client.coreGetFiles(s[1], s[2]);
                            }
                            break;
                        case PATH:
                            if(client.downloadingControl()){
                                list.remove(0);
                                client.coreGetListOfFile(s[1], s[2]);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } else {
            list.clear();
        }
    }
}
