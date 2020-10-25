package com.suas.vuirtab1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class TwoCamerasActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private static final String TAG = "TwoCamerasActivity";
    private static String mIP;
    private static int mConnection = 0;
    private int mfWidth = 1280, mfHeight = 720;
    public static Thread mVideothread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_two_cameras);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ((SurfaceView) findViewById(R.id.videoViewThermal)).getHolder().addCallback(this);
        Log.d("TwoCamerasActivity", "suas, use videoView");
        Intent intent = getIntent();
        mIP = intent.getStringExtra("ip");
        mConnection = 0; // TCP link, if 1, it is UDP link
        this.mfWidth = intent.getIntExtra("width", 1280);
        this.mfHeight = intent.getIntExtra("height", 720);
        MainActivity.SetResolution(mfWidth, mfHeight);
        Log.d(TAG, "onCreate: TwoCamerasActivity width = " + mfWidth + " height = " + mfHeight);

        //videoViewMain.setZOrderOnTop(false);
        mVideothread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServiceBase.getServiceBase().getVideoService().startLink(mIP, mConnection);
                } catch (RemoteException re) {
                    Log.d("TwoCamerasActivity", "onCreate startlink got remote exception " + re.toString());
                }
            }
        });
        mVideothread.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //videoViewMain.setZOrderOnTop(true);
                //speak("Press the mic to do voice commands!");
            }
        }, 1000);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        try {
            Log.d("TwoCamerasActivity", "show fullscreen");
            ServiceBase.getServiceBase().getVideoService().Resume(width, height, 2, "TwoCamerasActivity", surfaceHolder.getSurface());
        } catch (Exception e) {
            Log.d("TwoCamerasActivity", ".getVideoService().Resume got exception " + e.toString());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
