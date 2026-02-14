package com.example.test3;

import android.os.Handler;
import android.os.Looper;

public final class Ui  {

    private static final Handler MAIN = new Handler(Looper.getMainLooper());


    public static void run(Runnable r) {
        MAIN.post(r); // выполнение кода в UI-потоке
    }


    private Ui() {} // нельзя создать экземпляр


}
