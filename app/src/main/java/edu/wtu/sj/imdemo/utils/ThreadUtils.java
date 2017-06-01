package edu.wtu.sj.imdemo.utils;

import android.os.Handler;

/**
 * Created by admin on 2017/4/19.
 */

public class ThreadUtils {
    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    public static Handler mHandler = new Handler();

    public static void runInUIThread(Runnable task) {
        mHandler.post(task);
    }
}
