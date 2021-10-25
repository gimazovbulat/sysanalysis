package dz2;


public class TimeStampResp {
    /*
    granted=0, токен присутствует
    grantedWithMods=1, токен присутствует с изменениями
    rejection=2, штамп времени не получен
    waiting=3, запрос на получение штампа времени еще не обработан
    revocationWarning=4, аннулирование неизбежно
    revocationNotification=5, аннулирование имело место
    keyUpdateWarning=6
     */
    private int status;
    /* PKIFreeText */
    private String statusString;

    private TimeStampToken timeStampToken;

    public TimeStampResp() {
    }

    public TimeStampResp(int status, String statusString, TimeStampToken timeStampToken) {
        this.status = status;
        this.statusString = statusString;
        this.timeStampToken = timeStampToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public TimeStampToken getTimeStampToken() {
        return timeStampToken;
    }

    public void setTimeStampToken(TimeStampToken timeStampToken) {
        this.timeStampToken = timeStampToken;
    }
}
