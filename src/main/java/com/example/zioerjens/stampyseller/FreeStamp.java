package com.example.zioerjens.stampyseller;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FreeStamp {

    public String dateTime;
    public String code;

    public FreeStamp(String code) {
        this.dateTime = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss").format(new Date());
        this.code = code;
    }
}
