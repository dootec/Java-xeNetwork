package xenetwork;

import java.io.File;

 class XFile {

    private String fullPath;
    private String folderName;
    private String fileName;
    private long length;

    public long getLength() {
        return length;
    }
    public String getFullPath() {
        return fullPath;
    }

    protected XFile(XFileManager xFileManager, String s) {
        //s ---> Path/Filename1.exe*length1
        String[] x = s.split("\\*");
        try {
            fullPath = x[0];
            fileName = x[0].substring(x[0].lastIndexOf('\\'), x[0].length());
            folderName = x[0].replace(fileName, "");
        } catch (Exception e) {
            fileName = x[0];
            folderName = "";
        }
        length = Long.parseLong(x[1]);
        if(xFileManager.getManager().getClient().isSubServer){
            File dir = new File(System.getProperty("user.dir") + "\\xeData\\Server\\" + xFileManager.getManager().getClient().getId() + folderName);
            if (dir.exists() == false) {dir.mkdirs();}
            fullPath = System.getProperty("user.dir") + "\\xeData\\Server\\" + xFileManager.getManager().getClient().getId() + fullPath;
        }else{
            File dir = new File(xFileManager.getManager().getClient().getSavePath() + "\\" + folderName);
            if (dir.exists() == false) {dir.mkdirs();}
            fullPath = xFileManager.getManager().getClient().getSavePath() + fullPath;
        }
    }

    protected void clearCache(){
        fullPath = null;
        folderName = null;
        fileName = null;
        length = 0;
    }
}
