package com.example.zioerjens.stampyseller;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class CheckForInternet extends AsyncTask<String,String,String> {

    private Activity activity;
    private Boolean openDialog;
    private AlertDialog dialog;

    public CheckForInternet(Activity activity){
        this.activity = activity;
        openDialog = false;
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.e("INTERNETCHECK",hasInternetConnection(activity).toString());

        while (true){
            Log.e("INTERNETCHECK",hasInternetConnection(activity).toString());
            if (!hasInternetConnection(activity) && !openDialog){
                showInternetConnectionPopUp();
                openDialog = true;
            } else if (hasInternetConnection(activity) && openDialog){
                dismissInternetConnectionPopUp();
                openDialog = false;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                Log.e("CheckForInternet","Thread.sleep not possible");
            }
        }
    }

    public void showInternetConnectionPopUp(){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
                View mView = activity.getLayoutInflater().inflate(R.layout.show_internet_connectivity, null);
                TextView title = (TextView) mView.findViewById(R.id.successTitle);
                mView.findViewById(R.id.success).setBackgroundResource(R.drawable.no_internet_connectivity);
                title.setText(R.string.no_internet_connectivity);
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }

    public static Boolean hasInternetConnection(Activity activity){
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void dismissInternetConnectionPopUp(){
        dialog.dismiss();
    }
}
