package xenetwork;

import static xenetwork.Client.FILE;
import static xenetwork.Client.FILEDOWNLOAD;
import static xenetwork.Client.TEXT;
import static xenetwork.Client.PATH;

class Manager {

    private Client client;
    private String[] part;
    private String format;
    private long totalLength;
    private String fromTo;

    protected Client getClient() {
        return client;
    }

    protected String[] getPart() {
        return part;
    }

    protected String getFormat() {
        return format;
    }

    protected long getTotalLength() {
        return totalLength;
    }

    protected String getFromTo() {
        return fromTo;
    }

    protected Manager(Client client, String[] part) {
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

            -Get List:
            PATH@ 1@ Foo@ Path/
        */
        this.client = client;
        this.part = part;
        format = part[0];
        totalLength = Long.parseLong(part[1]);
        fromTo = part[2];
        if (getClient().isSubServer == false) {
            getClient().xeccb.streamStarted(getClient(), getClient().getTracking());
        } else {
            getClient().xescb.streamStartedServer(getClient(), getClient().getTracking());
        }

        switch (format) {
            case FILE:
                new XFileManager(this);
                break;
            case TEXT:
                new MessageManager(this);
                break;
            case FILEDOWNLOAD:
                new XFileUploadManager(this);
                break;
            case PATH:
                new PathManager(this);
                break;
            default:
                break;
        }

    }
}
