package xenetwork;

import java.io.FileOutputStream;
import java.util.ArrayList;

import static xenetwork.Client.AT;
import static xenetwork.Client.FILE;
import static xenetwork.Server.clients;

public class XFileManager {

    private Manager manager;
    private Client to;
    private boolean forwarding = false;

    private ArrayList<XFile> fileList = new ArrayList<>();
    private final char DOWNSPEED = 5120;

    private float[] downloadTracking = new float[3];
    private float nowPercentage = 0;
    private float totalPercentage = 0;
    private long totalDownloaded = 0;
    private float downSpeed = 0;

    public ArrayList<XFile> getFileList() {
        return fileList;
    }

    protected Manager getManager() {
        return manager;
    }

    public XFileManager(Manager manager) {
        /*
            s[]--->
            Protocols:
            -Send Folder:
            FILE@Length_Of_Files@Foo@Path/Filename1*length1;Path/Filename2*length2;

            -Send File:
            FILE@Length_Of_File@Foo@Filename1*length1;

            -Send Message:
            TEXT @Length_Of_Message @Foo

            -Get File - Folder:
            FILEDOWNLOAD @ 1 @Foo @abcxyz.exe
        */
        this.manager = manager;
        for (String item : manager.getPart()[3].split(";")) {
            fileList.add(new XFile(this, item));
        }
        if (manager.getClient().isSubServer && (to = clients.get(manager.getFromTo())) != null) {
            forwarding = manager.getClient().xescb.forwarding(to, manager.getClient(), this);
        }
        downloadSaveAndUploadFiles();
    }

    protected void downloadSaveAndUploadFiles() {
        long total = 0;
        try {
            if (forwarding) {
                to.sendCommand(FILE + AT + manager.getTotalLength() + AT + manager.getClient().getId() + AT + manager.getPart()[3]);
            }
            for (XFile files : fileList) {
                long bytesLeft = files.getLength();
                long downloadedBytes = 0;
                long previousDownloadedBytes = 0;
                FileOutputStream fos = new FileOutputStream(files.getFullPath());
                long oldtime = System.currentTimeMillis();
                while (bytesLeft > 0) {
                    char nextPacketSize = (char) ((bytesLeft > DOWNSPEED) ? DOWNSPEED : bytesLeft);
                    byte[] bytes = new byte[nextPacketSize];
                    manager.getClient().in2.readFully(bytes);
                    fos.write(bytes);
                    fos.flush();
                    if (forwarding) {
                        to.out2.write(bytes);
                        to.out2.flush();
                    }
                    if ((System.currentTimeMillis() - oldtime) > 1000) {
                        oldtime = System.currentTimeMillis();
                        downSpeed = (float) ((float) (downloadedBytes - previousDownloadedBytes) / 1000000.0);
                        nowPercentage = (float) (100.0 * ((float) downloadedBytes / (float) files.getLength()));
                        totalPercentage = (float) (100.0 * (float) (total + downloadedBytes) / (float) manager.getTotalLength());
                        totalDownloaded += downloadedBytes - previousDownloadedBytes;
                        manager.getClient().getTracking().setDownProperties(downSpeed, nowPercentage, totalPercentage, totalDownloaded);
                        if (manager.getClient().isSubServer == false) {
                            manager.getClient().xeccb.streamContinues(manager.getClient(), manager.getClient().getTracking());
                        } else {
                            manager.getClient().xescb.streamContinuesServer(manager.getClient(), manager.getClient().getTracking());
                        }
                        previousDownloadedBytes = downloadedBytes;
                    }
                    downloadedBytes += nextPacketSize;
                    bytesLeft -= nextPacketSize;
                }
                fos.close();
                total += downloadedBytes;
            }
            downSpeed = 0;
            nowPercentage = 100;
            totalPercentage = 100;
            manager.getClient().getTracking().setDownProperties(downSpeed, nowPercentage, totalPercentage, total);
            if (manager.getClient().isSubServer == false) {
                manager.getClient().xeccb.streamContinues(manager.getClient(), manager.getClient().getTracking());
                manager.getClient().xeccb.hasFile(manager.getClient(), manager.getFromTo(), this);
            } else {
                manager.getClient().xescb.streamContinuesServer(manager.getClient(), manager.getClient().getTracking());
            }
            manager.getClient().getTracking().setDownloading(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearCache();
    }

    private void clearCache() {
        manager = null;
        to = null;
        for (XFile xFile : fileList) {
            xFile.clearCache();
            xFile = null;
        }
        fileList.clear();
        downloadTracking = null;
        nowPercentage = 0;
        totalPercentage = 0;
        totalDownloaded = 0;
        downSpeed = 0;
    }
}

