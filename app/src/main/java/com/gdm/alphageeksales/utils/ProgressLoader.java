package com.gdm.alphageeksales.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import com.gdm.alphageeksales.R;
import java.util.Timer;
import java.util.TimerTask;


public class ProgressLoader {
    private static Dialog progressDialog;
    static boolean apiResponse = false;
    static int counter = 0;
    static Timer timer = new Timer();

    public static void init(Context ctx) {
        progressDialog = new Dialog(ctx);
        progressDialog.setContentView(R.layout.progress_loader);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
    }


    public static void show() {
        counter = 0;
        try {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
                startTimer();
            }
        } catch (Exception e) {/**/}
    }

    private static void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                counter++;
            }
        }, 0, 1000);
    }


    public static void dismiss() {
        apiResponse = true;
        try {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    counter++;
                    if (apiResponse && counter > 2) {
                        timer.cancel();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }
            }, 0, 1000);
        } catch (Exception e) {/**/}
    }
}
