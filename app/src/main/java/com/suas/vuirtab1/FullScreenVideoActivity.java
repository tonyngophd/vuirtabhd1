package com.suas.vuirtab1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.yuneec.videostreaming.RTSPPlayer;
import com.yuneec.videostreaming.VideoPlayer;
import com.yuneec.videostreaming.VideoPlayerException;


import static com.suas.vuirtab1.MainActivity.myTTS;
import static com.suas.vuirtab1.MainActivity.speak;

//import android.app.Fragment;

public class FullScreenVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, OnMapReadyCallback, MediaPlayer.OnPreparedListener {
    private static final String TAG = "FullScreenVideoActivity";
    static String mIP;
    private static int mConnection = 0;
    private StatusBarFrag statusBarFragfragment = new StatusBarFrag();
    private ControlsFragment controlsFragment = new ControlsFragment();
    private GridOverVideoFragment videogrifrag;
    private FragmentTransaction fragmentTransaction;
    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    private boolean StatusBarShown = true;
    private boolean ControlFragShown = true;
    private TextView textViewConnectionFull;
    private TextView batterypercentagetextView;
    protected static TextureView videoViewMain;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean RecognitionON = false;
    protected static androidx.fragment.app.FragmentTransaction fragmentTransactionx;
    protected static FragmentManager fragmentManager;
    protected static SettingsFragment settingsFragment;
    protected static QuickInstructionsFrag quickInstructionsFrag = new QuickInstructionsFrag();
    private ScreenRecordingFragment screenRecordingFragment = new ScreenRecordingFragment();
    private static ImageView imageViewSettingFullScreen;
    private static ImageView imageViewHelpFullScreen;
    private static ImageView imageViewGridOnOFF;
    private static ImageView imageViewMapOnOFF;
    private static boolean gridOnBool = false;
    protected static boolean mapOnBool = true;
    private static int mfWidth = 1280;
    private static int mfHeight = 720;
    private static ConstraintLayout mainLayout;
    protected static int mainWidth = 0, mainHeight = 0;
    private GoogleMap mMap;
    private double lat;
    private double lon;
    private Location currentBestLocation = null;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    // Declaring a Location Manager
    protected LocationManager locationManager;
    LatLng latLng;
    MarkerOptions markerOptions;
    private static SupportMapFragment mapFragment;
    private View fragmentGoogleMaps;
    private static ImageView imageViewQuickhelp;
    private static ImageView imageViewHome;
    static ConstraintLayout.LayoutParams params;
    protected static ConstraintLayout mapcontainer;
    protected static float mMapViewX, mMapViewY;
    protected static int mMapViewW, mMapViewH;
    protected static float MapZorder, ControlZorder;
    protected static FrameLayout controlsframe, statusframe, settingsframe, quickintroframe, gridframe, screenrecordframe;
    protected static LinearLayout linearLayoutMainVideoContainer;
    private static FloatingActionButton fab;
    private static View mapView;
    protected static SeekBar seekBarMapTransparency;
    protected static int intMapTransparency = 100;
    protected static final String PREFS_NAME = "VuIRPrefsFile";
    private static float pivotXo, pivotYo;
    static boolean DataLinkConnected = false;
    static boolean hasMenuKey = false, hasBackKey = true;
    static LinearLayout videolayout;
    private static Surface videoSurface;
    private static SurfaceTexture surfaceTexture;
    private int width = 0, height = 0;
    private SurfaceView mainSurfaceview;
    //private SurfaceView vidrecSurfaceview;
    static TextureView vidrecSurfaceview;
    static boolean newIRFrameAvailable = false;
    private long millisframe = System.currentTimeMillis();
    private long millisframeRTSP = System.currentTimeMillis();
    private int frameNo = 0;
    private int frameNoRtsp = 0;
    static int fpsRtsptime = 0;
    static int fpsRtspAveraged = 15;
    static RTSPPlayer videoPlayer = null;
    private static final String video_rtsp_url = "rtsp://192.168.2.220:554/stream/1";
    static Surface rtspSurface;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeSpeechRecognizer();

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        setContentView(R.layout.full_screen_video_activity);
        CheckIfTabletHasPhysicalorOnScreenBackandMenuKeys();

        doPermMaps();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentGoogleMaps);
        assert mapFragment != null;
        mapFragment.getMapAsync(FullScreenVideoActivity.this);

        videoViewMain = (TextureView) findViewById(R.id.videoViewMain);
        imageViewSettingFullScreen = (ImageView) findViewById(R.id.imageViewSettingFullScreen);
        imageViewHelpFullScreen = (ImageView) findViewById(R.id.imageViewHelpFullScreen);
        gridframe = (FrameLayout) findViewById(R.id.gridframe);
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        fragmentGoogleMaps = (View) findViewById(R.id.fragmentGoogleMaps);
        imageViewQuickhelp = (ImageView) findViewById(R.id.imageViewQuickhelp);
        imageViewGridOnOFF = (ImageView) findViewById(R.id.imageViewGridOnOFF);
        imageViewMapOnOFF = (ImageView) findViewById(R.id.imageViewMapOnOFF);
        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        mapcontainer = (ConstraintLayout) findViewById(R.id.mapcontainer);
        controlsframe = (FrameLayout) findViewById(R.id.controlsframe);
        statusframe = (FrameLayout) findViewById(R.id.statusframe);
        settingsframe = (FrameLayout) findViewById(R.id.settingsframe);
        quickintroframe = (FrameLayout) findViewById(R.id.quickintroframe);
        screenrecordframe = (FrameLayout) findViewById(R.id.screenrecordframe);
        linearLayoutMainVideoContainer = (LinearLayout) findViewById(R.id.linearLayoutMainVideoContainer);
        videolayout = (LinearLayout) findViewById(R.id.videolayout);
        seekBarMapTransparency = (SeekBar) findViewById(R.id.seekBarMapTransparency);
        mainSurfaceview = (SurfaceView) findViewById(R.id.mainSurfaceview);
        //vidrecSurfaceview = (SurfaceView) findViewById(R.id.vidrecSurfaceview);
        vidrecSurfaceview = (TextureView) findViewById(R.id.ttvRtspVideo);

        View parent = (View) mainLayout.getParent();

        new Thread(new Runnable() {
            public void run() {
                try {
                    ServiceBase.getServiceBase().getVideoService().Pause("Fullscreen");
                    ServiceBase.getServiceBase().getVideoService().startLink(mIP, mConnection);
                    Log.i(TAG, "FullScreenVideoActivity: startLink");
                } catch (RemoteException re) {
                    Log.d("FullScreenVideoActivity", "onCreate startlink got remote exception " + re.toString());
                }
            }
        }).start();

        mainSurfaceview.getHolder().addCallback(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForegroundService(new Intent(this, ScreenRecService.class));
        }

        videoViewMain.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                videoSurface = new Surface(surfaceTexture);
                FullScreenVideoActivity.surfaceTexture = surfaceTexture;
                startVideoSurface(surfaceTexture, width, height);
                Log.e("FullScreenVideoActivity", "surfaceCreated videoSurface = " + videoSurface + " surfaceTexture = " + surfaceTexture);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                videoSurface = new Surface(surfaceTexture);
                FullScreenVideoActivity.surfaceTexture = surfaceTexture;
                startVideoSurface(surfaceTexture, width, height);
                Log.i(TAG, String.format("surfaceChanged: width = %d, mainWidth = %d, height = %d, mainHeight = %d", width, mainWidth, height, mainHeight));
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.e("FullScreenVideoActivity", "surfaceDestroyed");
                try {
                    //ServiceBase.getServiceBase().getVideoService().Stop();
                    ServiceBase.getServiceBase().getVideoService().Pause("FullScreenVideoActivity");
                } catch (Exception e) {
                    Log.d("FullScreenVideoActivity", ".getVideoService().Resume got exception " + e.toString());
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                //Log.i("FullScreenVideoActivity", "onSurfaceTextureUpdated: surfaceTexture " + surfaceTexture);
                //newIRFrameAvailable = true;
                frameNo++;
                if (System.currentTimeMillis() - millisframe > 1000) {
                    millisframe = System.currentTimeMillis();
                    Log.i(TAG, "createFrames: start frameNo = " + frameNo);
                    frameNo = 0;
                }
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ControlsFragment.createFrames();
                    }
                }).start();*/
            }
        });
        /*surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {

            }
        });*/

        Log.d("FullScreenVideoActivity", "suas, use videoView");
        Intent intent = getIntent();
        this.mIP = intent.getStringExtra("ip");
        mConnection = 0; // TCP link, if 1, it is UDP link
        mfWidth = intent.getIntExtra("width", 1280);
        mfHeight = intent.getIntExtra("height", 720);
        MainActivity.SetResolution(mfWidth, mfHeight);
        Log.d(TAG, "onCreate: FullScreenVideoActivity width = " + mfWidth + " height = " + mfHeight);

        Log.d(TAG, "trying to start screenRecService");
        Intent fsintent = new Intent(this, ScreenRecordingFragment.class);
        Log.d(TAG, "screenRecService: instantiated ");
        ContextCompat.startForegroundService(this, intent);
        Log.d(TAG, "screenRecService: started");


        //videoViewMain.setZOrderOnTop(false);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.statusframe, statusBarFragfragment, "Status")
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .add(R.id.controlsframe, controlsFragment, "Controls")
                .add(R.id.quickintroframe, quickInstructionsFrag, "Quick Intro")
                .add(R.id.screenrecordframe, screenRecordingFragment, "Screen Recording")
                .commitAllowingStateLoss();
        showHideFragment(quickInstructionsFrag, false);

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    doPermAudio();
                    checkAndRequestPermissions();
                }
            }
        });

        setRecogniserIntent();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RecognitionON) {
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    RecognitionON = true;
                } else {
                    mSpeechRecognizer.stopListening();
                    RecognitionON = false;
                }
            }
        });

        seekBarMapTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                intMapTransparency = progress;
                if ((mMap != null) && (mapView != null)) {
                    mapView.setAlpha(intMapTransparency * 0.01f); // divided by 100
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                intMapTransparency = seekBar.getProgress();
                SettingsFragment.GetFullScreenSettingValues();
            }
        });

        imageViewSettingFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (settingsFragment == null) {
                    OpenSettingsFragment();
                } else {
                    if (settingsFragment.isHidden()) {
                        OpenSettingsFragment();
                    } else {
                        HideSettingsFragment();
                    }
                }
            }
        });

        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FullScreenVideoActivity finishing");
                finish();
            }
        });

        imageViewQuickhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (currentBestLocation != null) {
                    LatLng latLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
                    Log.i(TAG, "onClick: onSuccess lat = " + currentBestLocation.getLatitude() + " lon = " + currentBestLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                            .title("Your location");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
                    mMap.addMarker(markerOptions);
                } else {
                    LatLng washdc;
                    if (lat != 0 && lon != 0) {
                        washdc = new LatLng(38.907192, -77.036873);
                    } else washdc = new LatLng(38.907192, -77.036873);
                    mMap.addMarker(new MarkerOptions().position(washdc).title("Washington DC"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(washdc));
                }*/
                if (quickInstructionsFrag.isHidden()) {
                    showHideFragment(quickInstructionsFrag, true);
                    MainActivity.speak("Quick Help. Press Play Intro for a walk-through");
                } else {
                    showHideFragment(quickInstructionsFrag, false);
                }
            }
        });

        imageViewHelpFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Being created. Please check later");
            }
        });

        imageViewMapOnOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapOnBool = !mapOnBool;
                if (mapcontainer != null) {
                    if (mapOnBool) {
                        mapcontainer.setVisibility(View.VISIBLE);
                        imageViewMapOnOFF.setImageResource(R.drawable.ic_map_blue_off_36dp);
                    } else {
                        mapcontainer.setVisibility(View.INVISIBLE);
                        imageViewMapOnOFF.setImageResource(R.drawable.ic_map_blue_on_36dp);
                    }
                }
            }
        });

        imageViewGridOnOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GridOnOff();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //videoViewMain.setZOrderOnTop(true);
                //speak("Press the mic to do voice commands!");
                RestoreUserSettings();
                SetAspectRatio(1.0f, -1.0f);
                //Log.i(TAG, "run: zOrder MapZorder = " + MapZorder + " ControlZorder = " + ControlZorder);
                SetAllZorder();
                InitializeSettingsFragment();
                InitializeGridFragment();
                showHideFragment(videogrifrag, false);
                linearLayoutMainVideoContainer.requestFocus();
                pivotXo = videoViewMain.getPivotX();
                pivotYo = videoViewMain.getPivotY();
                if (mMap != null) {
                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            mapcontainer.setZ(2f);
                            controlsframe.setZ(3f);
                            //showToast("Back to move and resize map");
                        }
                    });
                }
            }
        }, 1000);


        /*initRTSPviewer();
        vidrecSurfaceview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                rtspSurface = new Surface(surfaceTexture);
                //surface = recordedSurface;
                Log.i(TAG, "surfaceCreated: videoPlayer");
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.i(TAG, "surfaceDestroyed: videoPlayer");
                try {
                    if (videoPlayer.isPlaying())
                        videoPlayer.stop();
                    videoPlayer.releasePlayer();
                } catch (VideoPlayerException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                frameNoRtsp++;
                if (System.currentTimeMillis() - millisframeRTSP > 1000) {
                    millisframeRTSP = System.currentTimeMillis();
                    Log.i(TAG, "createFrames: start frameNoRtsp = " + frameNoRtsp);
                    fpsRtspAveraged += frameNoRtsp;
                    fpsRtsptime++;
                    frameNoRtsp = 0;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ControlsFragment.createFrames();
                    }
                }).start();
            }
        });*/
        /*vidrecSurfaceview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Surface surface = surfaceHolder.getSurface();
                //surface = recordedSurface;
                Log.i(TAG, "surfaceCreated: videoPlayer");
                try {
                    videoPlayer.setSurface(surface);
                } catch (VideoPlayerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Log.i(TAG, "surfaceChanged: videoPlayer");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.i(TAG, "surfaceDestroyed: videoPlayer");
                try {
                    if (videoPlayer.isPlaying())
                        videoPlayer.stop();
                    videoPlayer.releasePlayer();
                } catch (VideoPlayerException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    static void initRTSPviewer() {
        videoPlayer = (RTSPPlayer) VideoPlayer.getPlayer(VideoPlayer.PlayerType.LIVE_STREAM);
        videoPlayer.initializePlayer();
        try {
            videoPlayer.setDataSource(video_rtsp_url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (VideoPlayerException e) {
            e.printStackTrace();
        }
    }

    private void startVideoSurface(SurfaceTexture surfaceTexture, int width, int height) {
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
        videoSurface = new Surface(surfaceTexture);
        View parent = (View) mainLayout.getParent();
        mainWidth = parent.getWidth();
        mainHeight = parent.getHeight();
        linearLayoutMainVideoContainer.requestFocus();
        int w, h;
        //Log.i(TAG, "surfaceChanged: width hasMenuKey = " + hasMenuKey + " hasBackKey = " + hasBackKey);
        if (!hasMenuKey && !hasBackKey) {
            // Do whatever you need to do, this device has a navigation bar
            w = mainWidth;
            h = mainHeight;
        } else {
            w = width;
            h = height;
        }
        try {
            Log.d("FullScreenVideoActivity", "show fullscreen mainWidth = " + mainWidth + " mainHeight = " + mainHeight);
            ServiceBase.getServiceBase().getVideoService().Resume(w, h, 2, "FullScreenVideoActivity", videoSurface);
        } catch (Exception e) {
            Log.d("FullScreenVideoActivity", ".getVideoService().Resume got exception " + e.toString());
        }
        showHideFragment(statusBarFragfragment, true);
    }

    private void GridOnOff() {
        gridOnBool = !gridOnBool;
        if (videogrifrag != null) {
            Log.i(TAG, "onClick: gridOnBool = " + gridOnBool + " videogrifrag = " + videogrifrag);
            showHideFragment(videogrifrag, gridOnBool);
        }
        if (gridOnBool) {
            imageViewGridOnOFF.setImageResource(R.drawable.ic_grid_off_black_48dp);
        } else {
            imageViewGridOnOFF.setImageResource(R.drawable.ic_grid_on_black_48dp);
        }
    }

    public static void SetAllZorder() {
        linearLayoutMainVideoContainer.setZ(0f);
        gridframe.setZ(1f);
        mapcontainer.setZ(2f);
        controlsframe.setZ(3f);
        statusframe.setZ(4f);
        settingsframe.setZ(5f);
        fab.setZ(6f);
        imageViewGridOnOFF.setZ(7f);
        imageViewMapOnOFF.setZ(7f);
        imageViewSettingFullScreen.setZ(7f);
        imageViewHelpFullScreen.setZ(7f);
        imageViewQuickhelp.setZ(7f);
        imageViewHome.setZ(7f);
        quickintroframe.setZ(8f);
    }

    public static void setConnectionValue(int connection) {
        mConnection = connection;
        try {
            ServiceBase.getServiceBase().getVideoService().Pause("FullScreenVideoActivity");
            ServiceBase.getServiceBase().getVideoService().startLink(mIP, mConnection);
            int w, h;
            if (!hasMenuKey && !hasBackKey) {
                // Do whatever you need to do, this device has a navigation bar
                w = mainWidth;
                h = mainHeight;
            } else {
                w = videoViewMain.getWidth();
                h = videoViewMain.getHeight();
            }
            ServiceBase.getServiceBase().getVideoService().Resume(w, h, 2, "FullScreenVideoActivity", videoSurface);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void HideSettingsFragment() {
        if (settingsFragment != null) {
            if (!settingsFragment.isHidden()) {
                fragmentTransactionx = fragmentManager.beginTransaction();
                fragmentTransactionx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                        .hide(settingsFragment)
                        .commitNowAllowingStateLoss();
            }
            Log.d(TAG, "HideSettingsFragment: hide now" + " isHidden = " + settingsFragment.isHidden());
        } else {
            Log.e(TAG, "HideSettingsFragment: SettingsFragment not added yet");
        }
    }

    private void InitializeSettingsFragment() {
        settingsFragment = SettingsFragment.newInstance("test", "test2");
        fragmentManager = getSupportFragmentManager();
        fragmentTransactionx = fragmentManager.beginTransaction();
        fragmentTransactionx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                .addToBackStack("Full Screen Fragment Stack")
                .add(R.id.settingsframe, settingsFragment, "Settings Fragment")
                .hide(settingsFragment)
                .commit();
    }

    private void InitializeGridFragment() {
        videogrifrag = GridOverVideoFragment.newInstance("test", "test2");
        fragmentManager = getSupportFragmentManager();
        fragmentTransactionx = fragmentManager.beginTransaction();
        fragmentTransactionx.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .addToBackStack("Full Screen Fragment Stack")
                .add(R.id.gridframe, videogrifrag, "Grid Fragment")
                //.hide(videogrifrag)
                .commit();
    }

    private void OpenSettingsFragment() {
        if (settingsFragment == null) {
            Log.d(TAG, "OpenSettingsFragment: null");
            settingsFragment = SettingsFragment.newInstance("test", "test2");
            fragmentManager = getSupportFragmentManager();
            fragmentTransactionx = fragmentManager.beginTransaction();
            fragmentTransactionx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                    .addToBackStack("Full Screen Fragment Stack")
                    .add(R.id.settingsframe, settingsFragment, "Settings Fragment")
                    .commit();
        } else {
            if (settingsFragment.isHidden()) {
                fragmentTransactionx = fragmentManager.beginTransaction();
                fragmentTransactionx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                        .attach(settingsFragment)
                        .show(settingsFragment)
                        .commit();
                Log.d(TAG, "OpenSettingsFragment: shown");
            } else {
                Log.d(TAG, "OpenSettingsFragment: else");
            }
        }
    }

    private void setRecogniserIntent() {
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        String pkg = getApplication().getPackageName();
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, pkg);
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100);
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 100);
    }

    void doPermAudio() {
        int MY_PERMISSIONS_RECORD_AUDIO = 1;
        FullScreenVideoActivity thisActivity = this;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
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
    }

    private void initializeSpeechRecognizer() {
        if (mSpeechRecognizer != null)
            mSpeechRecognizer.destroy();
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            Log.d(TAG, "initializeSpeechRecognizer: Created");
            mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    Log.d(TAG, "onReadyForSpeech: Ready!");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech: good");
                    /*new CountDownTimer(1000, 1000) {
                        @Override
                        public void onTick(long l) {
                            Log.d(TAG, "onresults onTick: l = " + l);
                        }

                        @Override
                        public void onFinish() {
                            Log.d(TAG, "onresults onFinish: done!");
                            mSpeechRecognizer.stopListening();
                        }
                    }.start();*/
                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {
                    Log.d(TAG, "onBufferReceived: here");
                    /*
                    millisInFuture	long: The number of millis in the future from the call to start() until the countdown is done and onFinish() is called.
                    countDownInterval	long: The interval along the way to receive onTick(long) callbacks.
                     */
                    /*new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            mSpeechRecognizer.stopListening();
                        }
                    }.start();*/
                }

                @Override
                public void onEndOfSpeech() {
                    mSpeechRecognizer.stopListening();
                }

                @Override
                public void onError(int errorCode) {
                    String errorMessage = getErrorText(errorCode);
                    Log.e(TAG, "onresults Error FAILED " + errorMessage);

                    // rest voice recogniser
                    //resetSpeechRecognizer();
                    initializeSpeechRecognizer();
                    if ((RecognitionON)) {
                        while (myTTS.isSpeaking()) ;
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                    }
                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    assert results != null;
                    Log.d(TAG, "onResults: full result = " + results.get(0));
                    processResult(results.get(0));
                    while (myTTS.isSpeaking()) ;
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    /*List<String> results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    assert results != null;
                    Log.d(TAG, "onResults: partial result = " + results.get(0));
                    mSpeechRecognizer.stopListening();
                    processResult(results.get(0));*/
                }

                @Override
                public void onEvent(int i, Bundle bundle) {
                    Log.d(TAG, "onEvent: Here!");
                }
            });
        } else {
            Log.d(TAG, "initializeSpeechRecognizer: NOT available");
        }
        Log.d(TAG, "initializeSpeechRecognizer: Initialized!");
    }

    private void processResult(String command) {
        command = command.toLowerCase();
        if (command.contains("what")) {
            if (command.contains("your name")) {
                speak("This app is VuIR Tab HD, created by s.U.A.S. LLC at s.U.A.S. dot com!");
                Log.d(TAG, "processResult: speak");
            }
            if (command.contains("time")) {
                Date now = new Date();
                String time = DateUtils.formatDateTime(this, now.getTime(), DateUtils.FORMAT_SHOW_TIME);
                speak("The time now is " + time);
            }
        } else if (command.contains("how")) {
            Log.d(TAG, "processResult: how are you doing");
            if (command.contains("are") && command.contains("you")) {
                speak("Thanks I am doing great How about yourself?");
            }
        } else if (command.contains("zoom") || command.contains("resume") || command.contains("you")) {
            if (command.contains("time") || command.contains("times")) {
                float f = 1.0f;
                try {
                    f = Float.parseFloat(command.replaceAll(".*?([\\d.]+).*", "$1"));
                } catch (NumberFormatException e) {
                    Log.d(TAG, "onResults: no number found");
                }
                Log.d(TAG, "onResults processResult: f = " + f);
                if ((f >= 1.0) && (f <= 8.0)) {
                    ScaleVideo(f);
                    int i = ConverttoInt(f);
                    if (i > 0)
                        speak("Video frame zoom up " + i + " times.");
                    else
                        speak("Video frame zoom up " + f + " times.");
                }
            } else if (command.contains("unzoom") || command.contains("reset") || command.contains("out max") || command.contains("no")) {
                ScaleVideo(1.0f);
                speak("Video frame unzoomed.");
            }
        } else if (command.contains("no soon") || command.contains("no resume") || command.contains("no zuma") || command.contains("no zone")) {
            ScaleVideo(1.0f);
            speak("Video frame unzoomed.");
        } else if (command.contains("white hot")) {
            controlsFragment.SetPalette(0);
            speak("Palette changed to white hot or first palette");
        } else if (command.contains("black hot")) {
            controlsFragment.SetPalette(1);
            speak("Palette changed to black hot or second palette");
        } else if (command.contains("iron bow")) {
            controlsFragment.SetPalette(2);
            speak("Palette changed to ironbow or third palette");
        } else if (command.contains("palette") || command.contains("pallet")) {
            if (command.contains("one") || command.contains("1")) {
                controlsFragment.SetPalette(0);
                speak("Palette changed to white hot or first palette");
            } else if (command.contains("two") || command.contains("to") || command.contains("2")) {
                controlsFragment.SetPalette(1);
                speak("Palette changed to black hot or second palette");
            } else if (command.contains("three") || command.contains("3")) {
                controlsFragment.SetPalette(2);
                speak("Palette changed to iron bow or third palette");
            }
        } else if (command.contains("ffc") || command.contains("fsc") || command.contains("f f c")) {
            controlsFragment.DoFFC();
            speak("Do thermal non uniform correction");
        } else if (command.contains("grid") || command.contains("read") || command.contains("rid")) {
            if (command.contains("on") || command.contains("off") || command.contains("of")) {
                GridOnOff();
                if (gridOnBool) {
                    speak("Grid turned on");
                } else {
                    speak("Grid turned off");
                }
            }
        } else if (command.contains("open")) {
            if (command.contains("browser")) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.sUAS.com"));
                startActivity(intent);
            }
        } else if (command.contains("stop")) {
            Log.d(TAG, "processResult: Stop listening");
            RecognitionON = false;
            mSpeechRecognizer.stopListening();
        }
    }

    private int ConverttoInt(float f) {
        int i = (int) f;
        if (f - i == 0) return i;
        else return 0;
    }

    private void SaveUserSettingInt(String settingName, int settingValue) {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void SaveUserSettingBoolean(String settingName, boolean settingValue) {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    public static float mapx, mapy, mapw = 0f, maph = 0f;

    private void RestoreUserSettings() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mapx = settings.getFloat("Map X", 0f);
        mapy = settings.getFloat("Map Y", 400f);
        mapw = settings.getFloat("Map W", 0f);
        maph = settings.getFloat("Map H", 0f);
        Log.i(TAG, "RestoreUserSettings: ResizeMapView mapw = " + mapw + " maph = " + maph);
    }

    private static void ResizeMapView(int size) {
        try {
            params = new ConstraintLayout.LayoutParams(0, 0);//(ConstraintLayout.LayoutParams) mapcontainer.getLayoutParams();
            params.topToTop = R.id.mainLayout;
            params.bottomToBottom = R.id.mainLayout;
            params.leftToLeft = R.id.mainLayout;
            params.rightToRight = R.id.mainLayout;
            params.horizontalBias = 0.0f;
            if (size > 0) {
                params.width = size;
                params.height = mainHeight;
                params.verticalBias = 0.5f;
                mapcontainer.setZ(2f);
                controlsframe.setZ(3f);
                seekBarMapTransparency.setVisibility(View.GONE);
                imageViewMapOnOFF.setImageResource(R.drawable.ic_map_blue_off_36dp);
                imageViewMapOnOFF.setVisibility(View.GONE);
                mapcontainer.setVisibility(View.VISIBLE);
                mapOnBool = true;
                int intMapTransparency_temp = intMapTransparency;//Store the value here to restore after setting progress
                seekBarMapTransparency.setProgress(100);  // because within the setProgress, intMapTransparency will be set to a different value (the input value, 100 in this case)
                intMapTransparency = intMapTransparency_temp; // restore it
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int intMapTransparency_temp = intMapTransparency; //Store the value here to restore after setting progress
                        seekBarMapTransparency.setProgress(100); // In split screen mode, Map will be 100% visible again, regardless of how visible it was before this.
                        intMapTransparency = intMapTransparency_temp; // restore it
                        SettingsFragment.GetFullScreenSettingValues();
                        mapcontainer.setX(0.0f);
                        mapcontainer.setY(0.0f);
                    }
                }, 5);
            } else {
                params.width = (mapw > 0f) ? (int) mapw : mainWidth / 4;
                mapw = params.width;
                params.height = (maph > 0f) ? (int) maph : mainHeight / 3;
                maph = params.height;
                Log.i(TAG, "ResizeMapView: params.width = " + params.width + " params.height = " + params.height);
                params.verticalBias = 0.9f;
                seekBarMapTransparency.setVisibility(View.VISIBLE);
                seekBarMapTransparency.setProgress(intMapTransparency);
                imageViewMapOnOFF.setVisibility(View.VISIBLE);
            }
            mapcontainer.setLayoutParams(params);
            Log.i(TAG, "ResizeMapView: X = " + mapcontainer.getX() + " Y = " + mapcontainer.getY());

            if (size > 0) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapcontainer.setX(0.0f);
                        mapcontainer.setY(0.0f);
                    }
                }, 100);
            }
            mMapViewX = mapcontainer.getX();
            mMapViewY = mapcontainer.getY();
            mMapViewW = mapcontainer.getMaxWidth();
            mMapViewH = mapcontainer.getHeight();

        } catch (Exception e) {
            Log.e(TAG, "ResizeMapView: Exception");
        }
    }

    private static float scalefactorX = 1.0f;
    protected static int mapwidth = -1;

    public static void SetAspectRatio(float currentZoomScale, float newscale) {
        int Width = mainWidth;//videoViewMain.getWidth();
        int Height = mainHeight;//videoViewMain.getHeight();
        float originalScale = Width * 1f / Height;//(1.05f * Width) / (1.47f * Height); // 1.05 and 1.47 are set in the full_screen_video_activity.xml for videoViewMain
        //Log.i(TAG, "SetAspectRatio: Width = " + Width + " Height = " + Height + " newscale = " + newscale + " originalscale = " + originalScale);
        float width_prescale = (videoViewMain.getWidth() * scalefactorX);
        float vidX_pre = videoViewMain.getX();
        Log.i(TAG, "SetAspectRatio: vidX_pre " + vidX_pre);
        if (newscale < 0) newscale = originalScale;
        scalefactorX = newscale / originalScale;
        videoViewMain.setScaleX(currentZoomScale * scalefactorX);
        videoViewMain.setScaleY(currentZoomScale);
        float width_aftscale = (videoViewMain.getWidth() * scalefactorX);
        int displaceX = (int) (mainWidth * (1.0f - scalefactorX) / 2.0f);//(int)((width_prescale - width_aftscale)/2);
        float vidX_aft = videoViewMain.getX();
        Log.i(TAG, "SetAspectRatio: width_prescale = " + width_prescale + " width_aftscale = " + width_aftscale + " displaceX = " + displaceX);
        videoViewMain.setX(displaceX);
        int width = 2 * (int) displaceX;
        mapwidth = width;
        //if (width == 0) width = 1;
        ResizeMapView((int) width);
        ControlsFragment.ScalePTZdetectionbox(Width - width);
        ResetGridWidth(width);
    }

    private static void ResetGridWidth(final int xo) {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(mainWidth - xo, mainHeight);
        gridframe.setLayoutParams(layoutParams);
        GridOverVideoFragment.leftmarginZero = xo > 0;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gridframe.setX(xo);
            }
        }, 5);
    }

    public static void ScaleVideo(float scalefactor) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //videoViewMain.setPivotX(100);
            //videoViewMain.setPivotY(1000);
        }
        Log.d(TAG, "ScaleVideo: pivotX = " + videoViewMain.getPivotX() + " pivotY = " + videoViewMain.getPivotY());
        videoViewMain.setScaleX(scalefactor * scalefactorX);
        videoViewMain.setScaleY(scalefactor);
        //Log.i(TAG, "ScaleVideo: X = " + videoViewMain.getX() + " Y = " + videoViewMain.getY());
    }

    //TODO: Adjust instructions fragment to show correct positions of the instruction tags
    public static void ScaleVideo(float scalefactor, float VideoFocusX, float VideoFocusY) {
        float scale = scalefactor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (VideoFocusX != 0) {
                videoViewMain.setPivotX(VideoFocusX);
            } else {
                videoViewMain.resetPivot();
            }
            if (VideoFocusY != 0) {
                videoViewMain.setPivotY(VideoFocusY);
            } else {
                videoViewMain.resetPivot();
            }
            if (scale <= 1.01f) videoViewMain.resetPivot();
            videoViewMain.setScaleX(scale * scalefactorX);
            videoViewMain.setScaleY(scale);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (scale <= 1.01f) {
                videoViewMain.setPivotX(pivotXo);
                videoViewMain.setPivotY(pivotYo);
            } else {
                if (VideoFocusX != 0) {
                    videoViewMain.setPivotX(VideoFocusX);
                }
                if (VideoFocusY != 0) {
                    videoViewMain.setPivotY(VideoFocusY);
                }
            }
            videoViewMain.setScaleX(scale * scalefactorX);
            videoViewMain.setScaleY(scale);
        } else {
            if (VideoFocusX != 0) {
                videoViewMain.setPivotX(VideoFocusX);
            }
            if (VideoFocusY != 0) {
                videoViewMain.setPivotY(VideoFocusY);
            }
            if (scale <= 1.1f) {
            }
            // TODO fix scaling problem in API <= Build.VERSION_CODES.M (23). Right now it only scales the video container
            // but it doesn't scale the video. Maybe something to do with video redraw on the scaled surface
            //videoViewMain.setScaleX(scale * scalefactorX);
            //videoViewMain.setScaleY(scale);
            //videoViewMain.requestLayout();
            scale = 1f;
        }
        Log.d(TAG, "ScaleVideo: pivotX = " + videoViewMain.getPivotX() + " pivotY = " + videoViewMain.getPivotY());
    }

    public static void ScaleVideo1(float scalefactor, float VideoFocusX, float VideoFocusY) {
        videoViewMain.setScaleX(scalefactor);
        videoViewMain.setScaleY(scalefactor);
        videoViewMain.setX(VideoFocusX);// - videoViewMain.getWidth()/2);
        videoViewMain.setY(VideoFocusY);// - videoViewMain.getHeight()/2);
        //Log.d(TAG, "ScaleVideo: height = " + videoViewMain.getHeight());
        //Log.d(TAG, "ScaleVideo: width = " + videoViewMain.getWidth());
        //Log.d(TAG, "ScaleVideo: VideoFocusX = " + VideoFocusX + " VideoFocusY = " + VideoFocusY + " x = " + videoViewMain.getX() + " y = " + videoViewMain.getY());
        Log.d(TAG, "ScaleVideo: scroll X = " + videoViewMain.getScrollX() + " scroll Y = " + videoViewMain.getScrollY());
        //videoViewMain.setFocusable();
    }

    public static void ScaleVideo(float scalefactor, float scalefactor_pre, float VideoFocusX, float VideoFocusX_pre, float VideoFocusY, float VideoFocusY_pre) {
        float x_pre = videoViewMain.getX(), y_pre = videoViewMain.getY();
        videoViewMain.setScaleX(scalefactor);
        videoViewMain.setScaleY(scalefactor);
        videoViewMain.setX(VideoFocusX - (VideoFocusX_pre - x_pre) * scalefactor / scalefactor_pre);
        videoViewMain.setY(VideoFocusY - (VideoFocusY_pre - y_pre) * scalefactor / scalefactor_pre);
        //Log.d(TAG, "ScaleVideo: height = " + videoViewMain.getHeight());
        //Log.d(TAG, "ScaleVideo: width = " + videoViewMain.getWidth());
        Log.d(TAG, "ScaleVideo: VideoFocusX = " + VideoFocusX + " VideoFocusY = " + VideoFocusY + " x = " + videoViewMain.getX() + " y = " + videoViewMain.getY());
        //Log.d(TAG, "ScaleVideo: scroll X = " + videoViewMain.getScrollX() + " scroll Y = " + videoViewMain.getScrollY());
        //videoViewMain.setFocusable();
    }

    public void showHideFragment(Fragment fragment, boolean Shown) {

        androidx.fragment.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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

    public GestureDetectorCompat getmDetector() {
        return mDetector;
    }

    public static void GetmainWidthandHeight() {
        if ((mainWidth == 0) || (mainHeight == 0)) {
            View parent = (View) mainLayout.getParent();
            mainWidth = parent.getWidth();
            mainHeight = parent.getHeight();
        }
        Log.i(TAG, "GetmainWidthandHeight: mainWidth = " + FullScreenVideoActivity.mainWidth + " mainHeight = " + FullScreenVideoActivity.mainHeight);
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

    public void onResume() {
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        if ((mainWidth == 0) || (mainHeight == 0)) {
            View parent = (View) mainLayout.getParent();
            mainWidth = parent.getWidth();
            mainHeight = parent.getHeight();
        }
        pivotXo = videoViewMain.getPivotX();
        pivotYo = videoViewMain.getPivotY();
        Log.i(TAG, "onResume: mainWidth = " + FullScreenVideoActivity.mainWidth + " mainHeight = " + FullScreenVideoActivity.mainHeight);
        super.onResume();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        if ((mainWidth == 0) || (mainHeight == 0)) {
            View parent = (View) mainLayout.getParent();
            mainWidth = parent.getWidth();
            mainHeight = parent.getHeight();
        }
        Log.i(TAG, "onPostCreate: mainWidth = " + FullScreenVideoActivity.mainWidth + " mainHeight = " + FullScreenVideoActivity.mainHeight);
        super.onPostCreate(savedInstanceState);
    }

    //https://stackoverflow.com/questions/16092431/check-for-navigation-bar

    private void CheckIfTabletHasPhysicalorOnScreenBackandMenuKeys() {
        hasMenuKey = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
        hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
    }

    //Todo: move maptransparency from SettingsFragment to here together with the x, y, w, h of the map.
    public void onStop() {
        try {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            if (mapwidth < 0) {
                editor.putFloat("Map X", mapcontainer.getX());
                editor.putFloat("Map Y", mapcontainer.getY());
                editor.putFloat("Map W", mapcontainer.getWidth());
                editor.putFloat("Map H", mapcontainer.getHeight());
                editor.apply();
            }
            //ServiceBase.getServiceBase().getVideoService().Stop();
            ServiceBase.getServiceBase().getVideoService().Pause("FullScreenVideoActivity");
        } catch (Exception e) {
            Log.d("FullScreenVideoActivity", ".getVideoService().Resume got exception " + e.toString());
        }

        super.onStop();
        //Log.d("FullScreenVideoActivity", "Stop media MediaCodec");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                Log.d(DEBUG_TAG, "Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE):
                Log.d(DEBUG_TAG, "Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP):
                Log.d(DEBUG_TAG, "Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(DEBUG_TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(DEBUG_TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {

        if ((mainWidth == 0) || (mainHeight == 0)) {
            View parent = (View) mainLayout.getParent();
            mainWidth = parent.getWidth();
            mainHeight = parent.getHeight();
            Log.i(DEBUG_TAG, "onCreate: parent = " + parent + "parent.getWidth() = " + parent.getWidth() + " parent.getHeight() = " + parent.getHeight());
        } else {
            if ((event1.getX() > event2.getX()) && (event1.getX() > (int) (mainWidth * 0.9f))) {
                OpenSettingsFragment();
            } else if ((event1.getX() < event2.getX()) && (event1.getX() > mainWidth / 2)) {
                HideSettingsFragment();
            }
            Log.d(DEBUG_TAG, "onScroll: " + event1.toString() + event2.toString());
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        StatusBarShown = !StatusBarShown;
        //ControlFragShown = !ControlFragShown;
        showHideFragment(statusBarFragfragment, StatusBarShown);
        showHideFragment(controlsFragment, StatusBarShown);
        int visibility = View.INVISIBLE;
        if (StatusBarShown) visibility = View.VISIBLE;
        fab.setVisibility(visibility);
        if (imageViewMapOnOFF.getVisibility() != View.GONE) {
            if(visibility == View.VISIBLE) mapOnBool = false;
            else mapOnBool = true;
            imageViewMapOnOFF.performClick();
        }
        imageViewGridOnOFF.setVisibility(visibility);
        imageViewSettingFullScreen.setVisibility(visibility);
        imageViewHelpFullScreen.setVisibility(visibility);
        imageViewHome.setVisibility(visibility);
        imageViewQuickhelp.setVisibility(visibility);
        imageViewMapOnOFF.setVisibility(visibility);
        //showToast("Double tap again to show controls");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapView = getSupportFragmentManager().findFragmentById(R.id.fragmentGoogleMaps).getView();
        /*if(currentBestLocation != null) {
            LatLng latLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                    .title("Here it is");
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
            mMap.addMarker(markerOptions);
        } else {
            LatLng washdc;
            if (lat != 0 && lon != 0) {
                washdc = new LatLng(38.907192, -77.036873);
            } else washdc = new LatLng(38.907192, -77.036873);
            mMap.addMarker(new MarkerOptions().position(washdc).title("Marker in DC"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(washdc));
        }*/

        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentBestLocation != null) {
                    Log.i(TAG, "onMapReady onSuccess: " + currentBestLocation.getLatitude() + "" + currentBestLocation.getLongitude());
                    latLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
                    markerOptions = new MarkerOptions().position(latLng).title("Ground Control");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                    mMap.addMarker(markerOptions);
                }
            }
        }, 0);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: requestCode = " + requestCode);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //while(currentBestLocation == null) {
                    fetchLastLocation();
                    Log.i(TAG, "onRequestPermissionsResult: currentBestLocation = " + currentBestLocation);
                    //}
                }
                break;
        }
    }

    void doPermMaps() {
        int MY_PERMISSIONS_MAPS_ACCESS = 1;
        FullScreenVideoActivity thisActivity = this;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_MAPS_ACCESS);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_MAPS_ACCESS);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_MAPS_ACCESS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fetchLastLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        try {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            Log.i(TAG, "fetchLastLocation: task = " + task + " last location = " + fusedLocationProviderClient.getLastLocation());
            //TODO need to re-update location in real time
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentBestLocation = location;
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (currentBestLocation != null) {
                                    Log.i(TAG, "onSuccess: " + currentBestLocation.getLatitude() + "" + currentBestLocation.getLongitude());
                                    //Toast.makeText(getApplicationContext(), currentBestLocation.getLatitude() + "" + currentBestLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                                    //showToast("Got current location");
                                    latLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
                                    markerOptions = new MarkerOptions().position(latLng).title("Ground Control");
                                    if (mMap != null) {
                                        Log.i(TAG, "onSuccess: here mMap = " + mMap);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                                        mMap.addMarker(markerOptions);
                                    }
                                }
                            }
                        }, 1);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "fetchLastLocation: can't get location in fetchlastlocation method");
        }
    }

    public void showToast(final String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setMargin(0, 0.8f);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        //if(FullScreenVideoActivity.)
        //Log.i(TAG, "onBackPressed: getCurrentFocus() = " + getCurrentFocus());
        super.onBackPressed();
        //finish(); // This has been taken care of by the Home button in FullScreenVideoActivity (NOT in the MainActivity, as that one is not visible here).
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    private ConstraintLayout.LayoutParams parms;

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.i(TAG, "surfacemainChanged: videoSurface = " + videoSurface + " surfaceTexture = " + surfaceTexture);
        if (videoSurface != null) {
            //if (surfaceTexture != null) startVideoSurface(surfaceTexture, width, height);
            final int w = linearLayoutMainVideoContainer.getWidth();
            final int h = linearLayoutMainVideoContainer.getHeight();
            parms = (ConstraintLayout.LayoutParams) linearLayoutMainVideoContainer.getLayoutParams();
            parms.width = w - 5;
            parms.height = h - 5;
            linearLayoutMainVideoContainer.setLayoutParams(parms);
            final Handler handler2 = new Handler();
            final int delayMillis = 10;
            final int w1 = width, h1 = height;
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parms.width = w;
                    parms.height = h;
                    linearLayoutMainVideoContainer.setLayoutParams(parms);
                }
            }, delayMillis);
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parms.width = w;
                    parms.height = h;
                    if (surfaceTexture != null) startVideoSurface(surfaceTexture, w1, h1);
                }
            }, 2 * delayMillis);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        /*try {
            videoPlayer.start();
        } catch (VideoPlayerException e) {
            e.printStackTrace();
        }*/
    }
}
