package xenetwork;

public class Tracking {

    private boolean downloading = false;
    private float downSpeed = 0;
    private float nowPercentage = 0;
    private float totalPercentage = 0;
    private long totalDownloaded = 0;

    private boolean uploading = false;
    private float upSpeed = 0;
    private float upNowPercentage = 0;
    private float upTotalPercentage = 0;
    private long totalUploaded = 0;

    public boolean isDownloading() {
        return downloading;
    }

    protected void setDownloading(boolean downloading) {
        if(this.downloading != downloading){
            this.downloading = downloading;
        }
    }
    protected void setUploading(boolean uploading) {
        if(this.uploading != uploading){
            this.uploading = uploading;
        }
    }

    public float getDownSpeed() {
        return downSpeed;
    }

    public float getNowPercentage() {
        return nowPercentage;
    }

    public float getTotalPercentage() {
        return totalPercentage;
    }

    public long getTotalDownloaded() {
        return totalDownloaded;
    }

    public boolean isUploading() {
        return uploading;
    }

    public float getUpSpeed() {
        return upSpeed;
    }

    public float getUpNowPercentage() {
        return upNowPercentage;
    }

    public float getUpTotalPercentage() {
        return upTotalPercentage;
    }

    public long getTotalUploaded() {
        return totalUploaded;
    }

    public float convert(float megaByte, String to) {
        switch (to) {
            case "GB":
                return megaByte / 1000;
            case "Gb":
                return megaByte / 125;
            case "MB":
                return megaByte;
            case "Mb":
                return megaByte * 8;
            case "KB":
                return megaByte * 1000;
            case "Kb":
                return megaByte * 8000;
            default:
                return megaByte;
        }
    }

    public String convert(float megaByte, String to, int digit) {
        float returned;
        switch (to) {
            case "GB":
                returned = megaByte / 1000;
                break;
            case "Gb":
                returned = megaByte / 125;
                break;
            case "MB":
                returned = megaByte;
                break;
            case "Mb":
                returned = megaByte * 8;
                break;
            case "KB":
                returned = megaByte * 1000;
                break;
            case "Kb":
                returned = megaByte * 8000;
                break;
            default:
                returned = megaByte;
                break;
        }
        return String.format("%." + digit + "f", (float) returned);
    }

    public String convert(float value, int digit) {
        return String.format("%." + digit + "f", (float) value);
    }

    protected void setDownProperties(float downSpeed, float nowPercentage, float totalPercentage, long totalDownloaded) {
        this.downSpeed = downSpeed;
        this.nowPercentage = nowPercentage;
        this.totalDownloaded = totalDownloaded;
        this.totalPercentage = totalPercentage;
    }

    protected void setUpProperties(float upSpeed, float upNowPercentage, float upTotalPercentage, long totalUploaded) {
        this.upSpeed = upSpeed;
        this.upNowPercentage = upNowPercentage;
        this.upTotalPercentage = upTotalPercentage;
        this.totalUploaded = totalUploaded;
    }
}
