package edu.wtu.sj.imdemo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by admin on 2017/4/19.
 */

public class ToastUtils {
    public static void showToastSafe(final Context context, final String str) {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
