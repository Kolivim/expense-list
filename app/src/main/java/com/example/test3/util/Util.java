package com.example.test3.util;

import java.time.format.DateTimeFormatter;

public class Util {

    public static final DateTimeFormatter dateFormatterInsert = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm Z");                                  /** "MM/dd/yyyy - HH:mm:ss Z" */
    public static final DateTimeFormatter dateFormatterSee = DateTimeFormatter.ofPattern("dd.MM.yy");

}
