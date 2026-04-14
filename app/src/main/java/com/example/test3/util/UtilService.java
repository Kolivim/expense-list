package com.example.test3.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class UtilService {


    public static ZonedDateTime parseDate(String dateStr) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
            Date date = formatter.parse(dateStr);
            return date.toInstant().atZone(ZoneId.systemDefault());
        } catch (ParseException e) {
            return null;
        }

    }


}
