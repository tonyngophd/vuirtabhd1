package com.suas.vuirtab1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.suas.vuirtab1.VideoWindow.Horizontal.resizeVideoSurface;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView mTextMessage;
    private Videofrag vidfragment = new Videofrag();
    private WebsiteFragment websiteFragment = new WebsiteFragment();
    private AboutFragment aboutFragment = new AboutFragment();
    private MediaGalleryFragment mediaFragment = null;
    private VideoWindow.Horizontal vidPreviewFragment = new VideoWindow.Horizontal();
    FragmentTransaction fragmentTransaction;
    static String mserverip = "192.168.2.220";
    private int LinkType = 0; //0 = TCP Link, 1 = UDP link
    public static int mWidth = 1280;
    public static int mHeight = 720;
    public static boolean Geotagging = false;
    private final String mToken = getClass().getName();
    public static TcpClient mTcpClient = null;
    protected static TextView ConnectStatustextView;
    private TextView recmsgtextView;
    protected static String ConnectionStatus;
    public static float BatteryVoltagePercent;
    public static int NumberofSats;
    public static int CPUTemp;
    private TextView textViewConnectionFull, batterypercentagetextView;
    public static TextToSpeech myTTS;
    private ImageView openCambutton;
    private Intent VuIRCameraViewintent, TwoCamerasViewintent, MainTwoCamsViewintent, MapsViewintent, GalleryIntent;
    public static String MessageReceivedFromGimmera = "";
    public static int MainWidth, MainHeight;
    public static Thread mVideoThread;
    private boolean IntroSpoken = false;
    private FrameLayout aboutfragframe;
    private FrameLayout mediafragframe;
    private static final int FLIP_DURATION = 4000;
    private ViewFlipper viewFlipper;
    private boolean isSlideshowOn = false;
    protected static CardView cardViewvideogroup;
    protected static CardView cardviewTitlte;
    private CardView cardViewflipper;
    private long connectionStatusMillis;
    public static IRCamera irCamera = new IRCamera();
    private ConstraintLayout constraintLayoutVideoPreview;
    private LinearLayout video_window2;
    private BottomNavigationView navView;


    //@SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceBase.getServiceBase().initService(getApplicationContext());
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initializeTextToSpeech();

        navView = findViewById(R.id.nav_view);
        openCambutton = findViewById(R.id.imageViewGotoVideo);
        aboutfragframe = findViewById(R.id.aboutfragframe);
        aboutfragframe.setBackgroundColor(Color.TRANSPARENT);
        mediafragframe = findViewById(R.id.aboutfragframe);
        mediafragframe.setBackgroundColor(Color.TRANSPARENT);
        viewFlipper = findViewById(R.id.viewflipper);
        cardViewvideogroup = findViewById(R.id.cardviewvideogroup);
        cardViewflipper = findViewById(R.id.cardviewflipper);
        cardviewTitlte = findViewById(R.id.cardviewTitlte);
        constraintLayoutVideoPreview = findViewById(R.id.constraintLayoutVideoPreview);
        video_window2 = findViewById(R.id.video_window2);

        mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        openCambutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenCameraActivity();
            }
        });


        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.videofragframe, vidfragment, "Video")
                .add(R.id.websitefragframe, websiteFragment, "Website")
                .add(R.id.aboutfragframe, aboutFragment, "About")
                .add(R.id.video_window2, vidPreviewFragment, VideoWindow.VIDEOWINDOW_H)
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("MainFragStack")
                .commitAllowingStateLoss();
        showHideFragment(vidfragment, false);
        showHideFragment(websiteFragment, false);
        showHideFragment(aboutFragment, false);

        ConnectStatustextView = findViewById(R.id.ConnectStatustextView);//aboutFragment
        recmsgtextView = findViewById(R.id.recmsgtextView);
        SetResolution(mWidth, mHeight);

        updateConversationHandler = new Handler();
        updateUIStatusHandler = new Handler();

        /*this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();/**/
        connectionStatusMillis = System.currentTimeMillis();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms
                while (mTcpClient == null) {
                    ConnectionStatus = getString(R.string.disconnected);
                    Log.d(TAG, "onCreate: ConnectionStatus = " + ConnectionStatus);
                    ConnectStatustextView.setText(ConnectionStatus);
                    recmsgtextView.setText("Trying to connect...");
                    new ConnectTask().execute("");
                    new CheckRealTimeConnectionTask().execute();
                    //ConnectTask();
                }
                //speak("3 seconds have passed!");
                if (!IntroSpoken) {
                    //speak("VuIR center app. Press Camera View for viewing & controlling VuIR thermal gimmera. s U A S button for visiting our website.");
                    IntroSpoken = true;
                }
                //VideoWindow.StartVideo(mserverip, 0);
            }
        }, 1000);/**/
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSlideshow();
            }
        }, 200);/**/
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndRequestPermissions();
            }
        }, 2000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndRequestPermissions();
            }
        }, 5000);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void initializeTextToSpeech() {
        Log.d(TAG, "onInit: TextToSpeech.SUCCESS = " + TextToSpeech.SUCCESS);
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
                Log.d(TAG, "onInit: status = " + status + " TextToSpeech.SUCCESS = " + TextToSpeech.SUCCESS);
                if (status != TextToSpeech.SUCCESS) {
                    Toast.makeText(MainActivity.this, "Error initializing text to speech!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onInit: Error initializing text to speech!");
                } else {
                    if (myTTS.getEngines().size() == 0) {
                        Toast.makeText(MainActivity.this, "There is no TTS Engine on this device.", Toast.LENGTH_LONG).show();
                    } else {
                        myTTS.setLanguage(Locale.US);
                        //speak("Ready to receive voice commands!");
                        //speak("Hello Mr. Eric! How are you? I can receive voice commands. How cool is that?");
                        /*Log.d(TAG, "onInit: now start mTcpClient");
                        while (mTcpClient == null) {
                            ConnectionStatus = getString(R.string.disconnected);
                            Log.d(TAG, "onCreate: ConnectionStatus = " + ConnectionStatus);
                            ConnectStatustextView.setText(ConnectionStatus);
                            recmsgtextView.setText("Trying to connect!");
                            new ConnectTask().execute("");
                        }*/
                    }
                }
            }
        });
    }

    private void addMediaFragment() {
        if (mediaFragment == null) {
            mediaFragment = new MediaGalleryFragment();
        }
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mediafragframe, mediaFragment, "MediaGallery")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("MainFragStack")
                .commitAllowingStateLoss();
    }

    public static void speak(String message) {
        Log.d(TAG, "speak: " + message + " Build.VERSION.SDK_INT" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 21) {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSlideshow();
        //myTTS.shutdown();
    }

    private boolean needToSwapBack = false;
    private boolean needFlashback = true;
    private boolean inGallery = false;
    private boolean needToRestartVideo = false;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("CommitTransaction")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    if (needToSwapBack) {
                        resizeVideoSurface(false);
                        needToSwapBack = false;
                    }
                    if (needToRestartVideo) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                VideoWindow.StartVideo(mserverip, 0);
                                needToRestartVideo = false;
                            }
                        }, 100);
                    }
                    showHideFragment(vidfragment, false);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    showHideFragment(vidPreviewFragment, true);
                    aboutfragframe.setBackgroundColor(Color.TRANSPARENT);
                    cardViewflipper.setVisibility(View.VISIBLE);
                    cardViewvideogroup.setVisibility(View.VISIBLE);
                    cardviewTitlte.setVisibility(View.VISIBLE);
                    inGallery = false;
                    return true;
                case R.id.navigation_vuir:
                    mTextMessage.setText(R.string.title_vuir);
                    OpenCameraActivity();
                    showHideFragment(vidfragment, true);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    aboutfragframe.setBackgroundColor(Color.TRANSPARENT);
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    cardViewvideogroup.setVisibility(View.INVISIBLE);
                    navView.setSelectedItemId(R.id.navigation_home);
                    inGallery = false;
                    return true;
                case R.id.navigation_gallery: //R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    //OpenTwoCamerasActivity();
                    //OpenMapsActivity();
                    //OpenGallery();
                    mTextMessage.setText(R.string.ir_media_gallery);
                    resizeVideoSurface(true);
                    navView.setSelectedItemId(R.id.navigation_home);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            needToSwapBack = true;

                            showHideFragment(vidfragment, false);
                            showHideFragment(websiteFragment, false);
                            showHideFragment(aboutFragment, false);
                            showHideFragment(vidPreviewFragment, false);
                            aboutfragframe.setBackgroundColor(Color.TRANSPARENT);
                            cardViewflipper.setVisibility(View.INVISIBLE);
                            cardViewvideogroup.setVisibility(View.INVISIBLE);
                            cardviewTitlte.setVisibility(View.INVISIBLE);

                            if (mediaFragment == null) addMediaFragment();
                            showHideFragment(mediaFragment, true);
                            inGallery = true;
                        }
                    }, 10);

                    return true;
                case R.id.navigation_web:
                    showHideFragment(vidfragment, false);
                    showHideFragment(websiteFragment, true);
                    showHideFragment(aboutFragment, false);
                    showHideFragment(mediaFragment, false);
                    aboutfragframe.setBackgroundColor(Color.TRANSPARENT);
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    cardViewvideogroup.setVisibility(View.INVISIBLE);
                    mTextMessage.setText(R.string.title_web);
                    cardviewTitlte.setVisibility(View.INVISIBLE);
                    inGallery = false;
                    return true;
                case R.id.navigation_about:
                    mTextMessage.setText(R.string.title_notifications);
                    showHideFragment(vidfragment, false);
                    showHideFragment(websiteFragment, false);
                    showHideFragment(aboutFragment, true);
                    showHideFragment(mediaFragment, false);
                    //aboutfragframe.setBackgroundColor(Color.WHITE);
                    cardViewflipper.setVisibility(View.INVISIBLE);
                    cardViewvideogroup.setVisibility(View.INVISIBLE);
                    cardviewTitlte.setVisibility(View.VISIBLE);
                    speak("About us and the app");
                    inGallery = false;
                    return true;
            }
            return false;
        }
    };


    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            //batteryTxt.setText(String.valueOf(level) + "%");
        }
    };

    public static int returnWidth() {
        return mWidth;
    }

    public static int returnHeight() {
        return mHeight;
    }

    private void OpenCameraActivity() {
        if (VuIRCameraViewintent == null) {
            VuIRCameraViewintent = new Intent(getApplicationContext(), FullScreenVideoActivity.class);
            VuIRCameraViewintent.putExtra("com.suas.vuirtab1.checklist", "Make sure to set your drone to<br><b>2.4ghz mode</b>,<br>NOT 5.8ghz.<br>Thanks!")
                    .putExtra("type", LinkType)
                    .putExtra("ip", mserverip)
                    .putExtra("width", mWidth)
                    .putExtra("height", mHeight);
        }
        startActivity(VuIRCameraViewintent);
    }

    private void OpenMapsActivity() {
        if (MapsViewintent == null) {
            MapsViewintent = new Intent(getApplicationContext(), MapsActivity.class);
            MapsViewintent.putExtra("com.suas.vuirtab1.checklist", "Make sure to set your drone to<br><b>2.4ghz mode</b>,<br>NOT 5.8ghz.<br>Thanks!")
                    .putExtra("type", LinkType)
                    .putExtra("ip", mserverip)
                    .putExtra("width", mWidth)
                    .putExtra("height", mHeight);
        }
        startActivity(MapsViewintent);
    }

    private void OpenGallery() {
        if (GalleryIntent == null) {
            GalleryIntent = new Intent(getApplicationContext(), MediaGalleryActivity.class);
        }
        startActivity(GalleryIntent);
    }

    private void OpenTwoCamerasActivity() {
        if (TwoCamerasViewintent == null) {
            TwoCamerasViewintent = new Intent(getApplicationContext(), TwoCamerasActivity.class);
            TwoCamerasViewintent.putExtra("com.suas.vuirtab1.checklist", "Make sure to set your drone to<br><b>2.4ghz mode</b>,<br>NOT 5.8ghz.<br>Thanks!")
                    .putExtra("type", LinkType)
                    .putExtra("ip", mserverip)
                    .putExtra("width", mWidth)
                    .putExtra("height", mHeight);
        }
        startActivity(TwoCamerasViewintent);
    }

    private void OpenMainTwoCamsActivity() {
        if (MainTwoCamsViewintent == null) {
            MainTwoCamsViewintent = new Intent(getApplicationContext(), MainTwoCamsActivity.class);
            MainTwoCamsViewintent.putExtra("com.suas.vuirtab1.checklist", "Make sure to set your drone to<br><b>2.4ghz mode</b>,<br>NOT 5.8ghz.<br>Thanks!")
                    .putExtra("type", LinkType)
                    .putExtra("ip", mserverip)
                    .putExtra("width", mWidth)
                    .putExtra("height", mHeight);
        }
        startActivity(MainTwoCamsViewintent);
    }

    public static void SetResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
        new Thread(new Runnable() {
            public void run() {
                SetResolutionThread();
            }
        }).start();
    }

    private static void SetResolutionThread() {
        try {
            Log.d(TAG, "In SetResolutionThread, serverip = " + mserverip);
            ServiceBase.getServiceBase().getVideoService().setServerIp(mserverip);
            ServiceBase.getServiceBase().getVideoService().SetResolution(mWidth, mHeight);
        } catch (Exception e) {
            Log.d(TAG, "SetResolutionThread got exception " + e.toString());
        }
    }

    public void showHideFragment(Fragment fragment, boolean Shown) {
        if (fragment == null) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Log.d(TAG, "showHideFragment: fragment.isHidden() = " + fragment.isHidden() + " fragment.isAdded() = " + fragment.isAdded());
        if (Shown) {
            if (fragment.isHidden()) {
                ft.show(fragment);
                Log.d("showHideFragment hidden", "Show");
            }
        } else {
            if (!fragment.isHidden()) {
                ft.hide(fragment);
                Log.d("showHideFragment Shown", "Hide");
            }
        }
        ft.commitAllowingStateLoss();
    }

    /**
     * Sends a message using a background task to avoid doing long/network operations on the UI thread
     */
    public static class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            // send the message
            mTcpClient.sendMessage(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            super.onPostExecute(nothing);
        }
    }

    String LongerMessage = "";
    public static final int CONFIRM_OK = 100;
    public static final int CONFIRM_NOT_OK = 1;
    final boolean[] NOTDisplayed = {true};
    long Millis = System.currentTimeMillis();
    /*void ConnectTask() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //we create a TCPClient object and
                mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                        if (MessageReceivedFromGimmera.length() > 300) {
                            // TODO: make a shifting mechanism to the left so no need to discard the whole received buffer
                            MessageReceivedFromGimmera = "";
                        }
                        MessageReceivedFromGimmera += message;
                        connectionStatusMillis = System.currentTimeMillis();
                        if (message != null) {
                            updateConversationHandler.post(new updateUIThread(message));
                        }
                    }
                });
                mTcpClient.run();
                while (mTcpClient.mBufferOut == null) {
                    if(System.currentTimeMillis() - Millis > 500){
                        Millis = System.currentTimeMillis();
                        Log.i(TAG, "run: onProgressUpdate mTcpClient.mBufferOut = " + mTcpClient.mBufferOut);
                        mTcpClient.run();
                    }
                }
            }
        }).start();
    }*/


    private boolean ClientStopped = false;

    //TODO need to rewrite this to utilize the existing video service to connect to Gimmera (send commands up)
    // Right now using the existing service can only send commands, but not to listen to the port actively
    @SuppressLint("StaticFieldLeak")
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        private boolean NOTDisplayed = true;

        @SuppressLint("SetTextI18n")
        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    if (MessageReceivedFromGimmera.length() > 300) {
                        // TODO: make a shifting mechanism to the left so no need to discard the whole received buffer
                        MessageReceivedFromGimmera = "";
                    }
                    MessageReceivedFromGimmera += message;
                    publishProgress(message);
                    Log.i(TAG, "messageReceived: message = " + message);
                    connectionStatusMillis = System.currentTimeMillis();
                }
            });
            mTcpClient.run();
            int i = 0;
            while ((mTcpClient != null) && (mTcpClient.mBufferOut == null)) {
                if (System.currentTimeMillis() - Millis > 500) {
                    Millis = System.currentTimeMillis();
                    Log.i(TAG, "run: onProgressUpdate mTcpClient.mBufferOut = " + mTcpClient.mBufferOut + " mTcpClient = " + mTcpClient);
                    i++;
                    if (i >= 4) {
                        Log.i(TAG, "doInBackground: onProgressUpdate (updated Disconnected Status) i = " + i);
                        updateConversationHandler.post(new updateUIThread_Disconnected());
                        i = 0;
                    }
                    mTcpClient.run();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            updateConversationHandler.post(new updateUIThread(values[0]));
        }
    }

    @SuppressLint("StaticFieldLeak")
    //AsyncTask<Params, Progress, Result>
    //https://stackoverflow.com/questions/14250989/how-to-use-asynctask-correctly-in-android
    public class CheckRealTimeConnectionTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (System.currentTimeMillis() - connectionStatusMillis > 2010) {
                connectionStatusMillis = System.currentTimeMillis();
                publishProgress("Disconnected");
            } else {
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.i(TAG, "onProgressUpdate: values[0] = " + values[0]);
            if (values[0].equals("Disconnected")) {
                updateConversationHandler.post(new updateUIThread_Disconnected());
                if (mTcpClient != null)
                    mTcpClient.stopClient();
                NOTDisplayed[0] = true;
                new ConnectTask().execute("");
                /*if(mTcpClient != null) {
                    if (mTcpClient.ClientStopped) {
                        mTcpClient = null;
                        new ConnectTask().execute("");
                    }
                }*/
            }
        }
    }

    public void onStop() {
        Log.i(TAG, "onStop: MainActivityMainActivity isFinishing() = " + isFinishing());
        Log.d(TAG, "Stop media MediaCodec");
        if (!isFinishing()) {
            try {
                ServiceBase.getServiceBase().getVideoService().Pause(this.mToken);
                //ServiceBase.getServiceBase().getVideoService().Stop();
            } catch (Exception e2) {
                Log.e(TAG, "MainActivityMainActivity ServiceBase.getServcieBase().getVideoService().Pause() got exception " + e2.toString());
            }
        }
        if (inGallery) needToRestartVideo = true;
        super.onStop();
    }

    private boolean mBackPressed = false;

    @Override
    public void onDestroy() {
        try {
            Log.i(TAG, "onDestroy: MainActivityMainActivity stop mBackPressed = " + mBackPressed);
            ServiceBase.getServiceBase().getVideoService().Stop();
        } catch (Exception e) {
            Log.e(TAG, "MainActivityMainActivity ServiceBase.getServcieBase().getVideoService().Stop() got exception " + e.toString());
        }
        if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
            myTTS = null;
        }
        if (mTcpClient != null) {
            mTcpClient.stopClient();
            mTcpClient = null;
        }
        stopSlideshow();
        if (inGallery) needToRestartVideo = true;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;
        Log.i(TAG, "onBackPressed: MainActivityMainActivity mBackPressed = " + mBackPressed);
        if ((websiteFragment != null) && (WebsiteFragment.webView != null)) {
            if (WebsiteFragment.webView.canGoBack() && websiteFragment.isVisible())
                WebsiteFragment.webView.goBack();
            else if (!inGallery)
                super.onBackPressed();
        } else if (!inGallery) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        super.onPostResume();
    }

    private static final int LOCATION = 1;
    public static String WifiSSID = "";

    protected void onStart() {
        super.onStart();
        //Assume you want to read the SSID when the activity is started
        tryToReadSSID();
    }

    private void tryToReadSSID() {
        //If requested permission isn't Granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
        } else {//Permission already granted
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                WifiSSID = wifiInfo.getSSID();//Here you can access your SSID
                //System.out.println(ssid);
                Log.i(TAG, "tryToReadSSID: Wifi SSID = " + WifiSSID);
            }
        }
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        //Log.i(TAG, "onResume MainActivity: StartVideo");
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        final Handler handler = new Handler();
        startSlideshow();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!inGallery) {
                    VideoWindow.StartVideo(mserverip, 0);
                }
            }
        }, 500);
        super.onResume();
    }

    private void startSlideshow() {
        if (!viewFlipper.isFlipping()) {
            viewFlipper.setAutoStart(true);
            viewFlipper.setFlipInterval(FLIP_DURATION);
            viewFlipper.startFlipping();
        }
    }

    private void animateSlideshow() {
        viewFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void stopSlideshow() {
        if (viewFlipper.isFlipping()) {
            viewFlipper.stopFlipping();
        }
    }

    private ServerSocket serverSocket;

    Handler updateConversationHandler, updateUIStatusHandler;
    int SERVERPORT = 2018;

    Thread serverThread = null;

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    class updateUIThread_Disconnected implements Runnable {

        public updateUIThread_Disconnected() {
        }

        @Override
        public void run() {
            // NOTE that both SetGimeraVersion_RealTimeUpdate and UpdateStatusBar have null-checks already
            // So no worry if these cause crashes if FullScreenAcvity hasn't been instantiated yet.
            SettingsFragment.SetGimeraVersion_RealTimeUpdate(false);
            BatteryVoltagePercent = 0.0f;
            NumberofSats = 0;
            CPUTemp = 0;
            ConnectionStatus = getString(R.string.disconnected);
            Log.i(TAG, "run: onProgressUpdate: values ConnectionStatus = " + ConnectionStatus);
            StatusBarFrag.UpdateStatusBar();
            ConnectStatustextView.setText(ConnectionStatus);
            StatusBarFrag.UpdateStatusBar();//Do it again to make sure it works
            NOTDisplayed[0] = true;
        }
    }

    //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    class updateUIThread implements Runnable {
        private String message;

        public updateUIThread(String str) {
            this.message = str;
        }

        @Override
        public void run() {
            recmsgtextView.setText(message);
            Log.d(TAG, "onProgressUpdate: message = " + message);
            if (message.contains("VOL")) {
                try {
                    //BatteryVoltage = Float.valueOf(message.substring(3));
                    BatteryVoltagePercent = Float.valueOf(message.substring(3));
                } catch (NumberFormatException e) {
                    try {
                        //BatteryVoltage = Float.valueOf(message.substring(3));
                        BatteryVoltagePercent = Float.valueOf(message.substring(4));
                    } catch (NumberFormatException e1) {
                        Log.e(TAG, "onProgressUpdate: exception, can't read number" + message);
                    }
                }
                Log.d(TAG, "onProgressUpdate: BatteryVoltagePercent = " + BatteryVoltagePercent);
                StatusBarFrag.UpdateStatusBar();
            }
            if (message.contains("SAT")) {
                try {
                    NumberofSats = Integer.valueOf(message.substring(3));
                } catch (NumberFormatException e) {
                    try {
                        NumberofSats = Integer.valueOf(message.substring(4));
                    } catch (NumberFormatException e1) {
                        Log.e(TAG, "onProgressUpdate: exception, can't read number" + message);
                    }
                }
                Log.d(TAG, "onProgressUpdate: NumberofSats = " + NumberofSats);
                StatusBarFrag.UpdateStatusBar();
            }
            if (message.contains("CPT")) {
                try {
                    CPUTemp = Integer.valueOf(message.substring(3));
                } catch (NumberFormatException e) {
                    try {
                        CPUTemp = Integer.valueOf(message.substring(4));
                    } catch (NumberFormatException e1) {
                        Log.e(TAG, "onProgressUpdate: exception, can't read number" + message);
                    }
                }
                Log.d(TAG, "onProgressUpdate: CPUTemp = " + CPUTemp);
                StatusBarFrag.UpdateStatusBar();
            }
            if (message.contains("CAM")) {
                if (message.contains("Bos")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("Boson", irCamera.Boson, 0, resolution);
                    if (ControlsFragment.radiovisibility != null) {
                        ControlsFragment.radiovisibility.check(R.id.radioButtonBoson);
                    }
                    if (FullScreenVideoActivity.videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        FullScreenVideoActivity.videolayout.setLayoutParams(params);
                    }
                    if(FullScreenVideoActivity.videoViewMain != null){
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        params.weight = 1.065f;
                        FullScreenVideoActivity.videoViewMain.setLayoutParams(params);
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (VideoWindow.Horizontal.llvideopreview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        Log.i(TAG, "run: reset to 1.61f");
                        VideoWindow.Horizontal.llvideopreview.setLayoutParams(params);
                    }

                    if (SettingsFragment.radiogroupPanTiltType != null) {
                        SettingsFragment.radiogroupPanTiltType.check(R.id.radioButtonSerial);
                        //TODO: code to allow real time user override of this, meaning to set a different value.
                    }
                    if (ControlsFragment.tiltseekBar != null) {
                        ControlsFragment.tiltseekBar.setMax(250);
                    }
                } else if (message.contains("BoP")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("BosonPi", irCamera.BosonPi, 0, resolution);
                    if (ControlsFragment.radiovisibility != null) {
                        ControlsFragment.radiovisibility.check(R.id.radioButtonBosonPi);
                    }
                    /*if(FullScreenVideoActivity.linearLayoutMainVideoContainer != null){
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        params.
                    }*/
                    /*if (FullScreenVideoActivity.linearLayoutMainVideoContainer != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.END;
                        FullScreenVideoActivity.linearLayoutMainVideoContainer.setLayoutParams(params);
                    }*/
                    if (FullScreenVideoActivity.videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.565f;
                        FullScreenVideoActivity.videolayout.setLayoutParams(params);
                        FullScreenVideoActivity.videolayout.setY(0);
                    }
                    if(FullScreenVideoActivity.videoViewMain != null){
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        params.weight = 1.00f; //Todo: return this of the other options to 1.065f
                        FullScreenVideoActivity.videoViewMain.setLayoutParams(params);
                        FullScreenVideoActivity.videoViewMain.setY(0);
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (VideoWindow.Horizontal.llvideopreview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.565f;
                        Log.i(TAG, "run: reset to 1.57f");
                        VideoWindow.Horizontal.llvideopreview.setLayoutParams(params);
                    }

                    if (SettingsFragment.radiogroupPanTiltType != null) {
                        SettingsFragment.radiogroupPanTiltType.check(R.id.radioButtonSerial);
                        //TODO: code to allow real time user override of this, meaning to set a different value.
                    }
                    if (ControlsFragment.tiltseekBar != null) {
                        ControlsFragment.tiltseekBar.setMax(250);
                    }
                } else if (message.contains("BPM")) {
                    if (irCamera == null) {
                        irCamera = new IRCamera();
                    }
                    int[] resolution = {320, 256};
                    try {
                        //TODO make this resolution setting work better
                        resolution[0] = Integer.valueOf(message.substring(3, 6));
                        if (resolution[0] == 640) resolution[1] = 512;
                        else if (resolution[0] == 160) resolution[1] = 120;
                        else {
                            resolution[0] = 320;
                        }
                        Log.i(TAG, "run: onProgressUpdate Resolution = " + resolution[0]);
                    } catch (NumberFormatException ignored) {
                    }
                    irCamera.setIRCamera("BosonPiMulti", irCamera.BosonPiMulti, 0, resolution);
                    if (ControlsFragment.radiovisibility != null) {
                        ControlsFragment.radiovisibility.check(R.id.radioButtonBosonPiM);
                    }
                    /*if(FullScreenVideoActivity.linearLayoutMainVideoContainer != null){
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                        params.
                    }*/
                    /*if (FullScreenVideoActivity.linearLayoutMainVideoContainer != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.END;
                        FullScreenVideoActivity.linearLayoutMainVideoContainer.setLayoutParams(params);
                    }*/
                    if (FullScreenVideoActivity.videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.2345679f;//.565f;
                        FullScreenVideoActivity.videolayout.setLayoutParams(params);
                        FullScreenVideoActivity.videolayout.setY(0);
                    }
                    if(FullScreenVideoActivity.videoViewMain != null){
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        params.weight = 1.00f; //Todo: return this of the other options to 1.065f
                        FullScreenVideoActivity.videoViewMain.setLayoutParams(params);
                        FullScreenVideoActivity.videoViewMain.setY(0);
                    }
                    /*if (FullScreenVideoActivity.vidrecSurfaceview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.61f;
                        FullScreenVideoActivity.vidrecSurfaceview.setLayoutParams(params);
                        FullScreenVideoActivity.vidrecSurfaceview.setZ(10f);
                    }*/

                    if (VideoWindow.Horizontal.llvideopreview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.2345679f;//.565f;
                        Log.i(TAG, "run: reset to 1.57f");
                        VideoWindow.Horizontal.llvideopreview.setLayoutParams(params);
                    }

                    if (SettingsFragment.radiogroupPanTiltType != null) {
                        SettingsFragment.radiogroupPanTiltType.check(R.id.radioButtonSerial);
                        //TODO: code to allow real time user override of this, meaning to set a different value.
                    }
                    if (ControlsFragment.tiltseekBar != null) {
                        ControlsFragment.tiltseekBar.setMax(250);
                    }
                } else {
                    if (ControlsFragment.radiovisibility != null) {
                        ControlsFragment.radiovisibility.check(R.id.radioButtonVuePro);
                    }
                    if (FullScreenVideoActivity.videolayout != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.47f;
                        FullScreenVideoActivity.videolayout.setLayoutParams(params);
                    }
                    if(FullScreenVideoActivity.videoViewMain != null){
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        params.weight = 1.065f;
                        FullScreenVideoActivity.videoViewMain.setLayoutParams(params);
                    }
                    if (VideoWindow.Horizontal.llvideopreview != null) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.47f;
                        VideoWindow.Horizontal.llvideopreview.setLayoutParams(params);
                    }
                    if (ControlsFragment.tiltseekBar != null) {
                        ControlsFragment.tiltseekBar.setMax(100);
                    }
                }
            }

            if (message.contains("PAL")) {
                if (ControlsFragment.paletteseekBar != null) {
                    //Log.i(TAG, "run: message.substring(3, 4) = " + message.substring(3, 4));
                    try {
                        int palette = Integer.valueOf(message.substring(3, 4));
                        if (palette >= 0 && palette <= 9) {
                            Log.d(TAG, "run: onProgressUpdate updated to pal " + palette);
                            ControlsFragment.paletteseekBar.setProgress(palette);
                        }
                    } catch (NumberFormatException ignored) {
                        try {
                            int palette = Integer.valueOf(message.substring(4, 5));
                            if (palette >= 0 && palette <= 9) {
                                Log.d(TAG, "run: onProgressUpdate updated to pal " + palette);
                                ControlsFragment.paletteseekBar.setProgress(palette);
                            }
                        } catch (NumberFormatException ignored1) {
                        }
                    }
                }
            }
            Log.d(TAG, "messageReceived: NOTDisplayed " + NOTDisplayed[0]);
            if (NOTDisplayed[0] && mTcpClient.mBufferOut != null) {
                ConnectionStatus = getString(R.string.connected);
                ConnectStatustextView.setText(ConnectionStatus);
                SettingsFragment.SetGimeraVersion_RealTimeUpdate(true);
                //recmsgtextView.setText("Connection established successfully!");
                NOTDisplayed[0] = false;
            }

            /*if (values[0].contains("REC")) {
                int confirmFlag = CONFIRM_NOT_OK;
                int value_received = 1000;
                try {
                    confirmFlag = Integer.valueOf(values[0].substring(3, values[0].indexOf("GOT")));
                    //Log.i(TAG, "onProgressUpdate:  values[0] = " + values[0] + " indexOf(\"GOT\") = " + values[0].indexOf("GOT") + " confirmFlag = " + confirmFlag);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "onProgressUpdate: exception, can't read number" + values[0]);
                }
                if (confirmFlag == CONFIRM_OK) {
                    try {
                        value_received = Integer.valueOf(values[0].substring(values[0].indexOf("GOT") + 3));
                        //Log.i(TAG, "onProgressUpdate:  values value_received = " + value_received);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "onProgressUpdate: cannot read value_received");
                    }
                    ControlsFragment.ConfirmRECfromAir(value_received, CONFIRM_OK);
                } else {
                    ControlsFragment.ConfirmRECfromAir(0, CONFIRM_NOT_OK);
                }
            }*/
            /*if ((values[0].length() - values[0].indexOf(0x7E)) < 14) {
                LongerMessage += values[0];
            } else {
                LongerMessage = values[0];
            }
            if (LongerMessage.length() >= 14) {
                boolean[] readstatus = new boolean[1];
                int[] signalValueout = new int[1];
                int[] PINOUT = new int[1];
                AirGroundCom.readA2GMessage(LongerMessage, readstatus, signalValueout, PINOUT);
            }*/
        }
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            //Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (!missingPermission.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
        tryToReadSSID();
        /*@Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION) {
                //User allowed the location and you can read it now
                tryToReadSSID();
            }
        }*/
    }
}
