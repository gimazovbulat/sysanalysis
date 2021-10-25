package dz2;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampToken {
    private String ts;
    private String signature;

    public TimeStampToken() {
    }

    public TimeStampToken(String ts, String signature) {
        this.ts = ts;
        this.signature = signature;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void setTs(Date tsDate) {
        this.ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:ss'.'SX").format(tsDate);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
