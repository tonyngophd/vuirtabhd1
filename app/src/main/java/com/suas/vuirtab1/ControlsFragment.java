package com.suas.vuirtab1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.suas.vuirtab1.AirGroundCom;
import com.suas.vuirtab1.FullScreenVideoActivity;
import com.suas.vuirtab1.MainActivity;
import com.suas.vuirtab1.R;
import com.suas.vuirtab1.ScreenRecordingFragment;
import com.suas.vuirtab1.ServiceBase;
import com.suas.vuirtab1.StatusBarFrag;
import com.yuneec.videostreaming.VideoPlayerException;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import dji.thirdparty.v3.eventbus.EventBus;

import static android.content.ContentValues.TAG;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.Color.GRAY;
import static android.os.SystemClock.sleep;
import static com.suas.vuirtab1.AirGroundCom.DATE_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.HOUR_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.MINUTE_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.MONTH_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.SECOND_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.YEAR_CHANNEL;
import static com.suas.vuirtab1.AirGroundCom.sendG2Amessage;
import static com.suas.vuirtab1.FullScreenVideoActivity.DataLinkConnected;
import static com.suas.vuirtab1.FullScreenVideoActivity.initRTSPviewer;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainHeight;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainWidth;
import static com.suas.vuirtab1.FullScreenVideoActivity.mapOnBool;
import static com.suas.vuirtab1.FullScreenVideoActivity.mapcontainer;
import static com.suas.vuirtab1.FullScreenVideoActivity.mapwidth;
import static com.suas.vuirtab1.FullScreenVideoActivity.rtspSurface;
import static com.suas.vuirtab1.FullScreenVideoActivity.videoPlayer;
import static com.suas.vuirtab1.FullScreenVideoActivity.vidrecSurfaceview;

//import android.app.Fragment;
//import static androidx.core.content.ContextCompat.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ControlsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ControlsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlsFragment<onResume> extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    static SeekBar tiltseekBar;
    private SeekBar panseekBar;
    static SeekBar paletteseekBar;
    private SeekBar gainseekBar;
    private ImageView allBosonsAtOnceButton;
    private ImageView imageViewpalettesbkg;
    private ImageView imageViewgainbkg;
    private int panprogress_pre = 120, tiltprogress_pre = 120;
    // 120 is out of range from 0 to 100, but still smaller than 127, max of byte (in Java, byte is signed 8-bit and ranges from -128 to 127)
    // However, in Arduino C++, byte is unsigned 8 bit, ranges from 0 to 255
    private static ConstraintLayout PTZdetectionbox;
    private static ConstraintLayout MultiCamZoomDetectionbox;

    private static float mScaleFactor = 1.0f, mScaleFactor_pre = 1.0f;
    private OnFragmentInteractionListener mListener;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    public static TextView textViewZoomScale;
    private float VideoFocusX, VideoFocusY, VideoFocusX_pre, VideoFocusY_pre;
    private float minX = 10000f, minY = 100000f, maxX = -10000f, maxY = -10000f, dxMax = 0f, dyMax = 0f;
    private float viewminX = 0, viewmaxX = 0, viewminY = 0, viewmaxY = 0;
    private static float TiltSensitivityFactor = 40.0f;
    private static float PanSensitivityFactor = 56.0f;
    private static final int SamSungTab5eWidth = 2560, SamSungTab5eHeight = 1600;
    private static boolean TiltSensitivityRescaledPerScreenSize = false, PanSensitivityRescaledPerScreenSize = false;
    private View rootView;
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    public static boolean mGestureModeOn = false;
    private static ImageView imageViewRecordGimmera;
    private static ImageView imageViewRecordScreen;
    private static ImageView imageViewGestureMode, imageViewPalette1, imageViewPalette2, imageViewPalette3, imageViewFFC;
    private float[] orientations = new float[3];
    private float[] orientations_pre = new float[3];
    private float[] Deltaorientations = new float[3];
    private float Originalx, Originalz;
    private long timeMillis;
    private long pantimeMillis;
    private long tilttimeMillis;
    private long ffctimeMillis;
    private static long recbuttonmillis;
    private static long screenrecbuttonmillis;
    private long zoommillis;
    private long dragmillis, mapontopMillis;
    private static int paletteNumber = 0;
    private static int gainNumber = 0;
    private static int REC = 0, REC_Actual = 0, recblink = 0, FFC = 0;
    private static int RECMode = 1, RECscreen = 0, screenRecBlink = 0;
    private int panProgressOnDown = 50, tiltProgressOnDown = 50;
    private int FFC_Button_Pressed_Times = 0;
    Handler mHandlerThread;
    Thread blinkRec;
    private static final int START_PROGRESS = 100;
    private static final int UPDATE_COUNT = 101;

    static final int SERIAL_PORT = 0;
    static final int PWM_PORT = 1;
    private static int PanTiltConnectionType = SERIAL_PORT;
    private static final int VERSION1X = 1;
    private static final int VERSION2X = 2;
    private static int GimmeraVersion = 1;

    private float mapfragframeXo, mapfragframeYo, mapfragframeXnow, mapfragframeYnow;
    private float mapviewW, mapviewH;
    private int mapfragframeRo, mapfragframeBo;
    private float Xo, Yo, Xnow, Ynow;
    private boolean MapWindowDraggable = false;
    private boolean MapWindowResizeable = false;
    private static ConstraintLayout.LayoutParams parms;
    private int MapViewMinWidth_inFullScreenAspectRatio = 300, MapViewMinHeight_inFullScreenAspectRatio = 200;
    private static boolean MapOnTop = false;
    private boolean CantToastShown = false;
    private long CantToastMillis = System.currentTimeMillis();
    private boolean cantMoveMapFurtherHorizontal = false, isCantMoveMapFurtherVertical = false;
    static RadioGroup radiovisibility;
    private ImageView screenshot;
    private static ImageView screenshotbimap;
    private ImageView screenshotpreview;
    private RelativeLayout screenshotpreviewlayout;
    private boolean isBoson = false;
    private boolean isBosonPi = false;
    private boolean isBosonPiM = false;
    private long screenrecOnOFFmillis = System.currentTimeMillis();
    private boolean allCameraAtOnce = false;


    public ControlsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ControlsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlsFragment newInstance(String param1, String param2) {
        ControlsFragment fragment = new ControlsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_controls, container, false);
        tiltseekBar = (SeekBar) rootView.findViewById(R.id.tiltseekbar);
        panseekBar = (SeekBar) rootView.findViewById(R.id.panseekBar);
        paletteseekBar = (SeekBar) rootView.findViewById(R.id.paletteseekBar);
        gainseekBar = (SeekBar) rootView.findViewById(R.id.gainseekBar);
        allBosonsAtOnceButton = (ImageView) rootView.findViewById(R.id.allBosonsAtOnceButton);
        PTZdetectionbox = (ConstraintLayout) rootView.findViewById(R.id.pantiltzoomdetectionbox);
        MultiCamZoomDetectionbox = (ConstraintLayout) rootView.findViewById(R.id.multicamzoomdetectionbox);
        textViewZoomScale = (TextView) rootView.findViewById(R.id.textViewPTZintro);
        imageViewRecordScreen = (ImageView) rootView.findViewById(R.id.imageViewRecord);
        imageViewRecordGimmera = (ImageView) rootView.findViewById(R.id.imageViewRecordGimmera);
        imageViewGestureMode = (ImageView) rootView.findViewById(R.id.imageViewGesture);
        imageViewPalette1 = (ImageView) rootView.findViewById(R.id.imageViewPalette1);
        imageViewPalette2 = (ImageView) rootView.findViewById(R.id.imageViewPalette2);
        imageViewPalette3 = (ImageView) rootView.findViewById(R.id.imageViewPalette3);
        imageViewpalettesbkg = (ImageView) rootView.findViewById(R.id.imageViewpalettesbkg);
        imageViewgainbkg = (ImageView) rootView.findViewById(R.id.imageViewgainbkg);
        screenshot = (ImageView) rootView.findViewById(R.id.screenshot);
        screenshotbimap = (ImageView) rootView.findViewById(R.id.screenshotbimap);
        screenshotpreview = (ImageView) rootView.findViewById(R.id.screenshotpreview);
        imageViewFFC = (ImageView) rootView.findViewById(R.id.imageViewFFC);
        radiovisibility = (RadioGroup) rootView.findViewById(R.id.radiovisibility);
        screenshotpreviewlayout = (RelativeLayout) rootView.findViewById(R.id.screenshotpreviewlayout);

        textViewZoomScale.setVisibility(View.INVISIBLE);
        timeMillis = System.currentTimeMillis();
        pantimeMillis = timeMillis;
        tilttimeMillis = timeMillis;
        recbuttonmillis = timeMillis;
        screenrecbuttonmillis = timeMillis;
        zoommillis = timeMillis;
        dragmillis = timeMillis;
        mapontopMillis = timeMillis;
        ffctimeMillis = timeMillis;

        sensorManager = (SensorManager) Objects.requireNonNull(getContext()).getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (rotationVectorSensor == null) {
                rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//TYPE_GAME_ROTATION_VECTOR);
            }
        }
        Log.d(TAG, "onCreateView: getContext = " + getContext() + " sensorManager = " + sensorManager + " rotationVectorSensor = " + rotationVectorSensor);

        // Create a listener
        SensorEventListener rvListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (System.currentTimeMillis() - timeMillis > 20) { //50hz
                    timeMillis = System.currentTimeMillis();
                    for (int i = 0; i < 3; i++) {
                        orientations_pre[i] = orientations[i];
                    }

                    // More code goes here
                    float[] rotationMatrix = new float[16];
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                    // Remap coordinate system
                    float[] remappedRotationMatrix = new float[16];
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            remappedRotationMatrix);

                    // Convert to orientations
                    SensorManager.getOrientation(remappedRotationMatrix, orientations);
                    for (int i = 0; i < 3; i++) {
                        orientations[i] = (float) (Math.toDegrees(orientations[i]));
                        Deltaorientations[i] = orientations[i] - orientations_pre[i];
                    }
                /*if (System.currentTimeMillis() - timeMillis > 1000) {
                    timeMillis = System.currentTimeMillis();
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "onSensorChanged: o[" + i + "] = " + orientations[i]);
                    }
                }*/
                    GesturePanTilt(0.2f, 1.0f);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        // Register it
        sensorManager.registerListener(rvListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        imageViewPalette1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetPalette(0);
            }
        });
        imageViewPalette2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetPalette(1);
            }
        });
        imageViewPalette3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetPalette(2);
            }
        });
        paletteseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    SetPalette(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        gainseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                SetGain(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Handler handlerUI = new Handler();
        final int DarkRed = Color.argb(99, 255, 0, 0);
        final int DarkBlue = Color.argb(0xAA, 0x25, 0x82, 0xCE);//"#AA2582CE"

        if(!allCameraAtOnce) allBosonsAtOnceButton.setColorFilter(GRAY);
        allBosonsAtOnceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allCameraAtOnce = !allCameraAtOnce;
                if(!allCameraAtOnce) allBosonsAtOnceButton.setColorFilter(GRAY);
                else allBosonsAtOnceButton.setColorFilter(DarkBlue);
                sendG2Amessage(allCameraAtOnce?1:0, AirGroundCom.ALL_CAMS_ATONCE_CHANNEL);
            }
        });
        imageViewRecordGimmera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: just clicked + REC before = " + REC);
                /// TODO: need to get confirmation  back from Gimmera it did receive REC command
                REC_Actual = REC;
                if (REC == 0) {
                    REC = 1;
                    /*initRTSPviewer();
                    try {
                        videoPlayer.setSurface(rtspSurface);
                    } catch (VideoPlayerException e) {
                        e.printStackTrace();
                    }
                    vidrecSurfaceview.setAlpha(0.8f);
                    //recordIRVideo();
                    prepareFrames();*/
                    if (RECMode == 1) {
                        if (android.os.Build.VERSION.SDK_INT < 30) {//23//Build.VERSION_CODES.P = 28
                            imageViewRecordGimmera.setColorFilter(DarkRed);
                        } else {
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (REC == 1) {
                                        if (recblink == 0) {
                                            recblink = 1;
                                            Log.i(TAG, "run: recblink = " + recblink + " imageViewRecordGimmera = " + imageViewRecordGimmera);
                                            handlerUI.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    imageViewRecordGimmera.setColorFilter(DarkRed);
                                                }
                                            });
                                            Log.i(TAG, "run: recblink imageViewRecordGimmera.setColorFilter(Color.RED);");

                                        } else {
                                            recblink = 0;
                                            Log.i(TAG, "run: recblink = " + recblink + " imageViewRecordGimmera = " + imageViewRecordGimmera);
                                            handlerUI.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    imageViewRecordGimmera.setColorFilter(GRAY);
                                                }
                                            });
                                        }

                                    } else {
                                        timer.cancel();
                                        timer.purge();
                                    }
                                }
                            }, 0, 1000);
                        }
                    }
                } else {
                    REC = 0;
                    /*vidrecSurfaceview.setAlpha(0.2f);
                    try {
                        videoPlayer.stop();
                        videoPlayer.releasePlayer();
                        videoPlayer = null;
                    } catch (VideoPlayerException e) {
                        e.printStackTrace();
                    }*/
                    if (RECMode == 1) {
                        if (android.os.Build.VERSION.SDK_INT < 30) {
                            imageViewRecordGimmera.setColorFilter(GRAY);
                        } else {
                            handlerUI.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageViewRecordGimmera.setColorFilter(DarkRed);
                                }
                            });
                        }
                        //imageViewRecordGimmera.setImageResource(R.drawable.ic_videocam_red_48dp);
                    }
                    //stopRecord();
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            createIRVideo();
                        }
                    }).start();*/
                }
                if (RECMode == 1) {
                    StatusBarFrag.BlinkRecStatus(REC);
                }
                Log.i(TAG, "onClick: sending REC = " + REC);
                if (GimmeraVersion == VERSION1X) {
                    RecordviaPWM(REC);
                } else {
                    sendG2Amessage(REC, AirGroundCom.REC_CHANNEL);
                }
            }
        });

        imageViewRecordScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() - screenrecOnOFFmillis < 2000) {
                    //Wait at least 1 second between clicks
                    return;
                }
                screenrecOnOFFmillis = System.currentTimeMillis();

                if (RECscreen == 0) {
                    RECscreen = 1;
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (RECscreen == 1) {
                                if (screenRecBlink == 0) {
                                    screenRecBlink = 1;
                                } else {
                                    screenRecBlink = 0;
                                }
                                handlerUI.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int color = (screenRecBlink == 1) ? DarkRed : GRAY;
                                        imageViewRecordScreen.setColorFilter(color);
                                    }
                                });
                            } else {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    }, 0, 1000);
                } else {
                    RECscreen = 0;
                    handlerUI.post(new Runnable() {
                        @Override
                        public void run() {
                            imageViewRecordScreen.setColorFilter(DarkBlue);
                        }
                    });
                }
                ScreenRecordingFragment.mToggleButton.setChecked(RECscreen == 1); //TODO This is temporarily disabled so it won't crash the app in Android 10: Need to fix it
                if ((GimmeraVersion != VERSION1X) && (isBosonPi || isBosonPiM)) {//Todo need to invent a whole new button for this function. This is to share with screen recording now.
                    sendG2Amessage(REC, AirGroundCom.PI_REC_CHANNEL);
                }
            }
        });

        imageViewFFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(System.currentTimeMillis() - ffctimeMillis > 300){
                    FFC = 1;
                    DoFFC();
                    FFC_Button_Pressed_Times = 0;
                    //Log.d(TAG, "onClick: after FFC_Button_Pressed_Times = " + FFC_Button_Pressed_Times);
                } else {
                    FFC_Button_Pressed_Times++;
                    //Log.d(TAG, "onClick: after FFC_Button_Pressed_Times = " + FFC_Button_Pressed_Times);
                    if(FFC_Button_Pressed_Times >= 2){
                        FFC = 0;
                        DoFFC();
                        FFC_Button_Pressed_Times = 0;
                    }
                }
                ffctimeMillis = System.currentTimeMillis();
            }
        });

        tiltseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress != tiltprogress_pre) {
                        if (PanTiltConnectionType == PWM_PORT) {
                            PanTiltviaPWM(1, progress);
                        } else {
                            sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);
                        }
                        //SendG2AMessage(progress, AirGroundCom.TILT_CHANNEL);
                        tiltprogress_pre = progress;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (GimmeraVersion == VERSION1X) {
                    int progress = seekBar.getMax() / 2;
                    tiltseekBar.setProgress(progress);
                    tiltprogress_pre = progress;
                    if (PanTiltConnectionType == PWM_PORT) {
                        PanTiltviaPWM(1, progress);
                    } else {
                        sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);
                        //SendG2AMessage(progress, AirGroundCom.TILT_CHANNEL);
                    }
                }
            }
        });
        panseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress != panprogress_pre) {
                        if (PanTiltConnectionType == PWM_PORT) {
                            PanTiltviaPWM(0, progress);
                        } else {
                            sendG2Amessage(progress, AirGroundCom.PAN_CHANNEL);
                        }
                        //SendG2AMessage(progress, AirGroundCom.PAN_CHANNEL);
                        panprogress_pre = progress;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mScaleGestureDetector = new ScaleGestureDetector(rootView.getContext(), new ScaleListener());
        mGestureDetector = new GestureDetector(rootView.getContext(), new DragListener());

        PTZdetectionbox.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                int pointerCount = event.getPointerCount();
                Log.d(TAG, "onTouch: pointer count = " + pointerCount);
                if (pointerCount > 1) {
                    zoommillis = System.currentTimeMillis();
                    //if(System.currentTimeMillis() - dragmillis > 500) { // This is to avoid accidental pan&tilt when finishing zoom (1 finger briefly becomes 2 fingers)
                    mScaleGestureDetector.onTouchEvent(event);
                    //}
                } else {
                    dragmillis = System.currentTimeMillis();
                    if (System.currentTimeMillis() - zoommillis > 500) { // This is to avoid accidental pan&tilt when finishing zoom (2 fingers briefly become 1 finger)
                        mGestureDetector.onTouchEvent(event);
                        Log.d(TAG, "onTouch: MotionEvent.ACTION_UP " + event.getAction());
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            //do something
                            //Log.d(TAG, "onTouch: range minX = " + minX + " maxX = " + maxX + " minY = " + minY + " maxY = " + maxY);
                            //Log.d(TAG, "onTouch: range total distance: in x = " + (maxX - minX) + " in y = " + (maxY - minY));
                            if (GimmeraVersion == VERSION1X) {
                                int progress = tiltseekBar.getMax() / 2;
                                tiltseekBar.setProgress(progress);
                                tiltprogress_pre = progress;
                                if (PanTiltConnectionType == PWM_PORT) {
                                    PanTiltviaPWM(1, progress);
                                } else {
                                    sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);
                                    //SendG2AMessage(progress, AirGroundCom.TILT_CHANNEL);¼
                                }
                            }
                        }
                    }
                }
                //Log.d(TAG, "onTouch: getAction() = " + event.getAction());

                return true;
            }
        });

        MultiCamZoomDetectionbox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /// 1 = side-top, 2 = side-middle, 3 = side bottom
                int cameraView = 1;
                float onethird = MultiCamZoomDetectionbox.getHeight()/3;
                cameraView = (int)((event.getY() - MultiCamZoomDetectionbox.getY())/onethird) + 1;
                sendG2Amessage(cameraView, AirGroundCom.SELECT_CAM_CHANNEL);
                Log.d(TAG, "onTouch: " + "cameraView = " + cameraView + " event.getY() = " + event.getY() + " onethird = " + onethird);
                return false;
            }
        });

        imageViewGestureMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((rotationVectorSensor == null) || (sensorManager == null)) return;

                mGestureModeOn = !mGestureModeOn;
                if (mGestureModeOn) {
                    Originalx = orientations[1];
                    Originalz = orientations[0];
                }
                StatusBarFrag.UpdateStatusBar();
            }
        });

        radiovisibility.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                if (checkedID == R.id.radioButtonBoson) {
                    isBoson = true;
                    isBosonPi = false;
                    isBosonPiM = false;
                } else if (checkedID == R.id.radioButtonBosonPi) {
                    isBoson = true;
                    isBosonPi = true;
                    isBosonPiM = false;
                } else if (checkedID == R.id.radioButtonBosonPiM) {
                    isBoson = true;
                    isBosonPi = false;
                    isBosonPiM = true;
                } else {
                    isBoson = false;
                    isBosonPi = false;
                    isBosonPiM = false;
                }

                setButtonVisibility(isBoson, isBosonPi || isBosonPiM);
            }
        });

        //resizeScreenshotbimap640x512pixels();
        screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GimmeraVersion == VERSION1X) {
                    RecordviaPWM(1);
                } else {
                    sendG2Amessage(1, AirGroundCom.REC_CHANNEL);
                }
                //takeScreenshotPIP();
                takeScreenshot();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tiltseekBar.setProgress(tiltseekBar.getMax() / 2);
                panseekBar.setProgress(panseekBar.getMax() / 2);
                sendG2Amessage(panseekBar.getMax() / 2, AirGroundCom.PAN_CHANNEL);
            }
        }, 1200);

        inside_loop_send_date_time_to_air(1, true);
        SendDateTimeToAir();

        return rootView;
    }

    private void SendDateTimeToAir(){

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                inside_loop_send_date_time_to_air(1, false);
            }
        }, 0, 600000);
    }

    private void inside_loop_send_date_time_to_air(int times, boolean full_date_and_hour){
        @SuppressLint("SimpleDateFormat") java.text.DateFormat df = new SimpleDateFormat("yyyy MM dd HH mm ss");
        String date;
        //date = "2045 34 31 23 59 48";
        //Log.d(TAG, "SendDateTimeToAir: " + date);
        int yyyy, MM=1, dd, HH, mm, ss;
        try {
            date = df.format(Calendar.getInstance().getTime());
            if(full_date_and_hour) {
                yyyy = Integer.valueOf(date.substring(0, 4));
                yyyy -= 2000; // Send only the last two digits of the year (so to limit the range to under 100). This app will last for another 100 years? Woo hoo!
                // This will fails when the year reaches 2255, 235 years from now! WOW! Even longer than US history to date (2020)
                MM = Integer.valueOf(date.substring(5, 7));
                dd = Integer.valueOf(date.substring(8, 10));
                for(int i = 0; i < times; i++) {
                    sendG2Amessage(yyyy, YEAR_CHANNEL);
                    sleep(5);
                    sendG2Amessage(MM, MONTH_CHANNEL);
                    sleep(5);
                    sendG2Amessage(dd, DATE_CHANNEL);
                    sleep(5);
                }
            }
            date = df.format(Calendar.getInstance().getTime());
            HH = Integer.valueOf(date.substring(11, 13));
            mm = Integer.valueOf(date.substring(14, 16));
            ss = Integer.valueOf(date.substring(17, 19));
            for(int i = 0; i < times; i++) {
                sendG2Amessage(HH, HOUR_CHANNEL);
                sleep(5);
                sendG2Amessage(mm, MINUTE_CHANNEL);
                sleep(5);
                sendG2Amessage(ss, SECOND_CHANNEL);
                sleep(5);
            }
            //Log.d(TAG, "SendDateTimeToAir: break down " + yyyy + " " + MM + " " + dd + " " + HH + " " + mm + " " + ss);
        } catch (NumberFormatException e){
        }
    }

    void SetPalette(int paletteToSet) {
        paletteNumber = paletteToSet;
        if (GimmeraVersion == VERSION1X) {
            PaletteviaPWM();
        } else {
            sendG2Amessage(paletteNumber, AirGroundCom.PALETTE_CHANNEL);
        }
    }

    private void SetGain(int gainToSet) {
        switch (gainToSet) {
            case 0:
                gainNumber = gainToSet;
                break;
            case 1:
                gainNumber = 4;
                break;
            case 2:
                gainNumber = 2;
                break;
            default:
                gainNumber = 0;
                break;
        }
        if (GimmeraVersion != VERSION1X) {
            sendG2Amessage(gainNumber, AirGroundCom.THERMAL_GAINMODE_CHANNEL);
        }
    }

    void DoFFC() {
        //if (FFC == 0) FFC = 1; //Tony 7/22/2020: We don't need this now because
        //else FFC = 0;          //it's taken care of before the function is called when button is pressed
        if (GimmeraVersion == VERSION1X) {
            FFCviaPWM();
        } else {
            sendG2Amessage(FFC, AirGroundCom.FLIR4_CHANNEL);
        }
    }

    public static void ConfirmRECfromAir(int REC_Sent, int value) {
        if ((value == MainActivity.CONFIRM_NOT_OK) || (REC_Sent != REC)) {
            REC = REC_Actual; // Reset the value to the value before command was sent, because the Gimmera said it didn't receive the command well
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                if (RECMode == 1) {
                    if (REC_Actual == 1)
                        imageViewRecordGimmera.setColorFilter(Color.RED);
                    else
                        imageViewRecordGimmera.setColorFilter(GRAY);
                }
            } else {
                // if REC == 0 before confirmation, the thread already stopped. So if confirmation NOT OK, it will have to start blinking again.
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (REC_Actual == 1) {
                            if (recblink == 0) {
                                recblink = 1;
                                Log.i(TAG, "run: recblink = " + recblink + " imageViewRecordGimmera = " + imageViewRecordGimmera);
                                imageViewRecordGimmera.setColorFilter(Color.RED);
                                Log.i(TAG, "run: recblink imageViewRecordGimmera.setColorFilter(Color.RED);");

                            } else {
                                recblink = 0;
                                Log.i(TAG, "run: recblink = " + recblink + " imageViewRecordGimmera = " + imageViewRecordGimmera);
                                imageViewRecordGimmera.setColorFilter(GRAY);
                            }
                        } else {
                            timer.cancel();
                            timer.purge();
                        }
                    }
                }, 0, 1000);
            }
        }
    }

    static void SetPanTiltConnectionType(int type) {
        if (type == SERIAL_PORT) {
            PanTiltConnectionType = SERIAL_PORT;
        } else if (type == PWM_PORT) {
            PanTiltConnectionType = PWM_PORT;
        } else {
            PanTiltConnectionType = SERIAL_PORT;
        }
    }

    static void SetGimmeraVersion(int version) {
        if (version == VERSION1X) {
            GimmeraVersion = 1;
        } else if (version == VERSION2X) {
            GimmeraVersion = 2;
        } else {
            GimmeraVersion = 1;
        }
    }

    // Pan = 0, Tilt = 1;
    private int pwmPanValue = 90;

    private void PanTiltviaPWM(int axis, int progress) {
        int value;
        if (axis == 0) {
            value = (int) (136.0d - (3.0d * (progress * 0.27d)));
        } else {
            value = (int) (50.0d + (3.0d * (progress * 0.27d)));
        }
        try {
            ServiceBase.getServiceBase().getVideoService().sendpwm(axis, value);
        } catch (Exception e) {
            Log.d(TAG, "set camera " + axis + " got exception " + e.toString());
        }
    }

    private void FFCviaPWM() {
        int progress = 100;
        int pwmvalue = (int) (136.0d - (3.0d * (progress * 0.27d)));
        try {
            Log.i(TAG, "FFCviaPWM: pwmvalue = " + pwmvalue);
            ServiceBase.getServiceBase().getVideoService().sendpwm(0, pwmvalue);
        } catch (Exception e) {
            Log.d(TAG, "set camera " + 0 + " got exception " + e.toString());
        }
    }

    private void RecordviaPWM(int value) {
        int progress;
        if (value == 1) {
            progress = 95;
        } else {
            progress = 5;
        }
        int pwmvalue = ((progress / 34) * 40) + 50;
        try {
            ServiceBase.getServiceBase().getVideoService().sendpwm(2, pwmvalue);
        } catch (Exception e) {
            Log.d(TAG, "send pwm " + 2 + " got exception " + e.toString());
        }
    }

    private void PaletteviaPWM() {
        int progress = 95;
        int pwmvalue = ((progress / 34) * 40) + 50;
        try {
            Log.i(TAG, "PaletteviaPWM: pwmvalue = " + pwmvalue);
            ServiceBase.getServiceBase().getVideoService().sendpwm(3, pwmvalue);
        } catch (Exception e) {
            Log.d(TAG, "send pwm " + 3 + " got exception " + e.toString());
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int progress = 50;
                int pwmvalue = ((progress / 34) * 40) + 50;
                try {
                    Log.i(TAG, "PaletteviaPWM: pwmvalue = " + pwmvalue);
                    ServiceBase.getServiceBase().getVideoService().sendpwm(3, pwmvalue);
                } catch (Exception e) {
                    Log.d(TAG, "send pwm " + 3 + " got exception " + e.toString());
                }
            }
        }, 100);
    }

    private void ZoomviaPWM() {
        int progress = 5;
        int pwmvalue = ((progress / 34) * 40) + 50;
        try {
            Log.i(TAG, "ZoomviaPWM: pwmvalue = " + pwmvalue);
            ServiceBase.getServiceBase().getVideoService().sendpwm(3, pwmvalue);
        } catch (Exception e) {
            Log.d(TAG, "send pwm " + 3 + " got exception " + e.toString());
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int progress = 50;
                int pwmvalue = ((progress / 34) * 40) + 50;
                try {
                    Log.i(TAG, "PaletteviaPWM: pwmvalue = " + pwmvalue);
                    ServiceBase.getServiceBase().getVideoService().sendpwm(3, pwmvalue);
                } catch (Exception e) {
                    Log.d(TAG, "send pwm " + 3 + " got exception " + e.toString());
                }
            }
        }, 100);
    }

    private void SendG2AMessage(int value, int channel) {
        try {
            ServiceBase.getServiceBase().getVideoService().SendG2AMessage2(value, channel);
        } catch (Exception e) {
            Log.d(TAG, "sSendG2AMessage " + channel + " got exception " + e.toString());
        }
    }

    public static void UpdateTiltSmoothness(final int tsmoothin) {
        if ((FullScreenVideoActivity.mainWidth == 0) || (FullScreenVideoActivity.mainHeight == 0))
            FullScreenVideoActivity.GetmainWidthandHeight();
        TiltSensitivityFactor = ((78.0f - 2.0f) * tsmoothin / 100f + 2.0f) * FullScreenVideoActivity.mainHeight * 1.0f / SamSungTab5eHeight;
        TiltSensitivityRescaledPerScreenSize = true;
        //Log.d(TAG, "UpdateTiltSmoothness: onStopTrackingTouch TiltSensitivityFactor = " + TiltSensitivityFactor);
    }

    public static void UpdatePanSmoothness(final int psmoothin) {
        if ((FullScreenVideoActivity.mainWidth == 0) || (FullScreenVideoActivity.mainHeight == 0))
            FullScreenVideoActivity.GetmainWidthandHeight();
        PanSensitivityFactor = ((110.0f - 2.0f) * psmoothin / 100f + 2.0f) * FullScreenVideoActivity.mainWidth * 1.0f / SamSungTab5eWidth;
        PanSensitivityRescaledPerScreenSize = true;
        //Log.d(TAG, "UpdatePanSmoothness: onStopTrackingTouch PanSensitivityFactor = " + PanSensitivityFactor);
    }

    public static void CopyRecMode(final int recmodein) {
        RECMode = recmodein;
    }

    private boolean RotationsMoreThanErrorMargin(float ErrorMargininDegrees) {
        for (int i = 0; i < 3; i++) {
            if (Deltaorientations[i] > ErrorMargininDegrees) return true;
        }
        return false;
    }

    private void GesturePanTilt(float ErrorMargininDegrees, float smoothingfactor) {
        if (!mGestureModeOn) return;
        int x = 1;
        int Tilt;
        //if (RotationsMoreThanErrorMargin(ErrorMargininDegrees)) {
        if (Deltaorientations[x] > ErrorMargininDegrees) {
            // TODO SOMETHING ABOUT USING Originalx here
            float xmin = 10.0f, xmax = 80.0f;
            //Log.d(TAG, "GesturePanTilt: x = " + orientations[1]);

            if ((orientations[x] > xmin) && (orientations[x] < xmax)) {
                Tilt = rescale(orientations[x], xmin, xmax, smoothingfactor);
                Log.d(TAG, "GesturePanTilt: Tilt = " + Tilt);
                tiltseekBar.setProgress(Tilt);
            }
        } else {
            if (System.currentTimeMillis() - tilttimeMillis > 2000) {
                tilttimeMillis = System.currentTimeMillis();
                Originalx = orientations[x];
                Tilt = tiltseekBar.getMax() / 2;
                tiltseekBar.setProgress(Tilt);
            }
        }
        int z = 0;
        if (Deltaorientations[z] > ErrorMargininDegrees) {
            float zmin = -50f, zmax = 50.0f;
            //Log.d(TAG, "GesturePanTilt: x = " + orientations[1]);
            float relativez = orientations[z] - Originalz;
            Log.d(TAG, "GesturePanTilt: relativez = " + relativez);
            if ((relativez > zmin) && (relativez < zmax)) {
                int Pan;
                Pan = rescalePan(orientations[z], zmin, zmax, smoothingfactor);
                Log.d(TAG, "GesturePanTilt: Pan = " + Pan);
                panseekBar.setProgress(Pan);
            }
        } else {
            if (System.currentTimeMillis() - pantimeMillis > 20000) {
                // Reset initial Z orientation after a timeout of no movement above
                pantimeMillis = System.currentTimeMillis();
                Originalz = orientations[z];
            }
        }
    }

    private int rescale(float x, float min, float max, float factor) {
        int i;
        int scalemax = tiltseekBar.getMax();
        Log.d(TAG, "rescale: scalemax = " + scalemax);
        //Log.d(TAG, "rescale: GesturePanTilt x = " + x);
        i = scalemax - (int) ((x - min) * factor);
        if (i < 0) i = 0;
        else if (i > scalemax) i = scalemax;
        return i;
    }

    private int rescalePan(float x, float min, float max, float factor) {
        int i;
        //Log.d(TAG, "rescale: GesturePanTilt x = " + x);
        i = 100 - (int) ((x - min) * factor);
        if (i < 0) i = 0;
        else if (i > 100) i = 100;
        return i;
    }

    private int rescalePan1(float z, float min, float max, float factor) {
        int i;
        //Log.d(TAG, "rescale: GesturePanTilt z = " + z);
        i = (int) (z * factor);
        if (i < 0) i = 0;
        else if (i > 100) i = 100;
        return i;
    }

    public static void setRecordButtonView(boolean value) {
        if (value) {
            imageViewRecordGimmera.setImageResource(R.drawable.ic_photo_camera_red_48dp);
        } else {
            imageViewRecordGimmera.setImageResource(R.drawable.ic_videocam_red_48dp);
        }
    }

    private class DragListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            minX = minY = 100000f;
            maxX = maxY = -100000f;
            panProgressOnDown = panseekBar.getProgress();
            tiltProgressOnDown = tiltseekBar.getProgress();
            if ((FullScreenVideoActivity.mainWidth == 0) || (FullScreenVideoActivity.mainHeight == 0))
                FullScreenVideoActivity.GetmainWidthandHeight();
            if (!TiltSensitivityRescaledPerScreenSize) {
                TiltSensitivityFactor *= FullScreenVideoActivity.mainHeight * 1.0f / SamSungTab5eHeight;
                TiltSensitivityRescaledPerScreenSize = true;
            }
            if (!PanSensitivityRescaledPerScreenSize) {
                PanSensitivityFactor *= FullScreenVideoActivity.mainWidth * 1.0f / SamSungTab5eWidth;
                PanSensitivityRescaledPerScreenSize = true;
            }

            MapViewMinWidth_inFullScreenAspectRatio = FullScreenVideoActivity.mainWidth / 5;
            MapViewMinHeight_inFullScreenAspectRatio = FullScreenVideoActivity.mainHeight / 5;
            if (MapViewMinWidth_inFullScreenAspectRatio < 300)
                MapViewMinWidth_inFullScreenAspectRatio = 300;
            if (MapViewMinHeight_inFullScreenAspectRatio < 200)
                MapViewMinHeight_inFullScreenAspectRatio = 200;
            // don't return false here or else none of the other
            // gestures will work
            float HalfFingerSize = 50;
            Xo = motionEvent.getX() + PTZdetectionbox.getX(); // The motion event is recognized only within PTZdetectionbox. Therefore to get the Absolute X and Y of the touch point,
            Yo = motionEvent.getY() + PTZdetectionbox.getY(); // we need to add X & Y of PTZdetectionbox, which is contained in a match_parent-match_parent holder.
            mapfragframeXo = mapcontainer.getX();
            mapfragframeYo = mapcontainer.getY();
            mapviewW = mapcontainer.getWidth();
            mapviewH = mapcontainer.getHeight();
            parms = (ConstraintLayout.LayoutParams) mapcontainer.getLayoutParams();
            mapfragframeRo = parms.rightMargin;
            mapfragframeBo = parms.bottomMargin;
            /*showToast("Xo = " + Xo + " Yo = " + Yo + " FXo = " + mapfragframeXo + " Fyo = " + mapfragframeYo + " W = " + FullScreenVideoActivity.mapcontainer.getWidth()
                    + " H = " + FullScreenVideoActivity.mapcontainer.getHeight());*/
            if ((CloseToPoint(Xo, Yo, mapfragframeXo, mapfragframeYo, HalfFingerSize, HalfFingerSize)
                    || CloseToPoint(Xo, Yo, mapfragframeXo + mapviewW, mapfragframeYo, HalfFingerSize, HalfFingerSize)
                    || CloseToPoint(Xo, Yo, mapfragframeXo, mapfragframeYo + mapviewH, HalfFingerSize, HalfFingerSize)
                    || CloseToPoint(Xo, Yo, mapfragframeXo + mapviewW, mapfragframeYo + mapviewH, HalfFingerSize, HalfFingerSize))
                    && (mapcontainer != null) && mapOnBool) {
                MapWindowResizeable = true;
                showToast("Drag to resize map view");
            } else {
                MapWindowResizeable = false;
            }

            if ((Xo >= mapfragframeXo)
                    && (Xo <= mapfragframeXo + mapviewW)
                    && (Yo >= mapfragframeYo)
                    && (Yo <= mapfragframeYo + mapviewH)
                    && (!MapWindowResizeable)  // Resize will exclude move: we don't want move and resize at the same time.
                    && (mapcontainer != null) && mapOnBool) {
                MapWindowDraggable = true;
                showToast("Drag to move map view");
            } else {
                MapWindowDraggable = false;
            }
            //Log.i(TAG, "onDown: FXo = " + mapfragframeXo + " Xo = " + Xo + " R = " + (mapfragframeXo + mapviewW) + " FYo = " + mapfragframeYo + " Yo = " + Yo + " B = " + (mapfragframeYo + mapviewH));
            //Log.i(TAG, "onDown: MapWindowResizeable = " + MapWindowResizeable + " MapWindowDraggable = " + MapWindowDraggable);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
            if (MapWindowResizeable || MapWindowDraggable) {
                float scrollX = motionEvent1.getX() + PTZdetectionbox.getX(), scrollY = motionEvent1.getY() + PTZdetectionbox.getY();
                float dx = scrollX - Xo, dy = scrollY - Yo;
                float HalfFingerSize = 50;
                float Xnow = mapfragframeXo, Ynow = mapfragframeYo, Wnow = mapviewW, Hnow = mapviewH;
                int Rnow = parms.rightMargin, Bnow = parms.bottomMargin;
                //getAction() == MotionEvent.ACTION_UP
                if (MapWindowResizeable) {
                    if (CloseToPoint(Xo, Yo, mapfragframeXo, mapfragframeYo, HalfFingerSize, HalfFingerSize)) {
                        Xnow = mapfragframeXo + dx;
                        Ynow = mapfragframeYo + dy;
                        Wnow = mapviewW - dx;
                        Hnow = mapviewH - dy;
                    } else if (CloseToPoint(Xo, Yo, mapfragframeXo + mapviewW, mapfragframeYo, HalfFingerSize, HalfFingerSize)) {
                        Xnow = mapfragframeXo;
                        Ynow = mapfragframeYo + dy;
                        Wnow = mapviewW + dx;
                        Hnow = mapviewH - dy;
                        Rnow = mapfragframeRo - (int) dx;
                    } else if (CloseToPoint(Xo, Yo, mapfragframeXo, mapfragframeYo + mapviewH, HalfFingerSize, HalfFingerSize)) {
                        Xnow = mapfragframeXo + dx;
                        Ynow = mapfragframeYo;
                        Wnow = mapviewW - dx;
                        Hnow = mapviewH + dy;
                        Bnow = mapfragframeBo - (int) dy;
                    } else if (CloseToPoint(Xo, Yo, mapfragframeXo + mapviewW, mapfragframeYo + mapviewH, HalfFingerSize, HalfFingerSize)) {
                        Xnow = mapfragframeXo;
                        Ynow = mapfragframeYo;
                        Wnow = mapviewW + dx;
                        Hnow = mapviewH + dy;
                        Rnow = mapfragframeRo - (int) dx;
                        Bnow = mapfragframeBo - (int) dy;
                    }

                    // Limit to the minimum window dimensions, or the map view becomes too small to adjust and may cause crash
                    if ((Wnow >= MapViewMinWidth_inFullScreenAspectRatio) || (Hnow >= MapViewMinHeight_inFullScreenAspectRatio)) {
                        if (Wnow >= MapViewMinWidth_inFullScreenAspectRatio) {
                            if ((scrollX > MapViewMinWidth_inFullScreenAspectRatio / 2) && (scrollX < (mainWidth - MapViewMinWidth_inFullScreenAspectRatio / 2))) {
                                parms.width = (int) Wnow;
                                //Log.i(TAG, "onScroll: scrollX = " + scrollX);
                                mapcontainer.setX(Xnow);
                            }
                        }
                        if (Hnow >= MapViewMinHeight_inFullScreenAspectRatio) {
                            if ((scrollY > MapViewMinHeight_inFullScreenAspectRatio / 2) && (scrollY < (mainHeight - MapViewMinHeight_inFullScreenAspectRatio / 2))) {
                                parms.height = (int) Hnow;
                                //Log.i(TAG, "onScroll: scrollY = " + scrollY);
                                //mapcontainer.setLayoutParams(parms);
                                mapcontainer.setY(Ynow);
                            }
                        }
                        mapcontainer.setLayoutParams(parms);
                        try {
                            SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(FullScreenVideoActivity.PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putFloat("Map X", mapcontainer.getX());
                            editor.putFloat("Map Y", mapcontainer.getY());
                            editor.putFloat("Map W", mapcontainer.getWidth());
                            editor.putFloat("Map H", mapcontainer.getHeight());

                            editor.apply();
                            //ServiceBase.getServiceBase().getVideoService().Stop();
                        } catch (Exception e) {
                            Log.d("FullScreenVideoActivity", ".getVideoService().Resume got exception " + e.toString());
                        }

                    } else if ((Wnow < MapViewMinWidth_inFullScreenAspectRatio) && (Hnow < MapViewMinHeight_inFullScreenAspectRatio)) {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            showToast("Can't make map smaller");
                        }
                    }
                } else if (MapWindowDraggable) {
                    float xnow = mapfragframeXo + dx, ynow = mapfragframeYo + dy;
                    if ((xnow > -(mapcontainer.getWidth() - MapViewMinWidth_inFullScreenAspectRatio / 2))
                            && (xnow < mainWidth - MapViewMinWidth_inFullScreenAspectRatio / 2)) {
                        mapcontainer.setX(xnow);
                        cantMoveMapFurtherHorizontal = false;
                    } else {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            cantMoveMapFurtherHorizontal = true;
                            showToast("Can't move further");
                        }
                    }
                    if ((ynow > 0)// - MapViewMinHeight_inFullScreenAspectRatio / 2)
                            && (ynow < mainHeight - MapViewMinHeight_inFullScreenAspectRatio / 2)) {
                        mapcontainer.setY(ynow);
                        isCantMoveMapFurtherVertical = false;
                    } else {
                        if (System.currentTimeMillis() - CantToastMillis > 2000) {
                            CantToastMillis = System.currentTimeMillis();
                            isCantMoveMapFurtherVertical = true;
                            showToast("Can't move further");
                        }
                    }
                }
            } else {
                int tiltnow = tiltseekBar.getProgress(), pannow = panseekBar.getProgress();
                float X = motionEvent.getX() + PTZdetectionbox.getX(), Y = motionEvent.getY() + PTZdetectionbox.getY();
                float rangeX = 0, rangeY = 0;
                //if ((viewminX == 0) || (viewminY == 0) || (viewmaxX == 0) || (viewmaxY == 0)) {
                viewminX = PTZdetectionbox.getX();
                viewminY = PTZdetectionbox.getY();
                viewmaxX = PTZdetectionbox.getWidth() + viewminX;
                viewmaxY = PTZdetectionbox.getHeight() + viewminY;
                //}
                rangeX = (viewmaxX - viewminX);
                rangeY = (viewmaxY - viewminY);
                if (X < viewmaxX - rangeX / 4) {
                    pannow = normalizevalue(panProgressOnDown + (int) ((motionEvent1.getX() - motionEvent.getX()) / (PanSensitivityFactor * mScaleFactor)), 0, 100);
                }
                if (X > viewminX + rangeX / 4) { // overlapping middle 1/3, where it resets both
                    tiltnow = normalizevalue(tiltProgressOnDown - (int) ((motionEvent1.getY() - motionEvent.getY()) / (TiltSensitivityFactor * mScaleFactor)), 0, tiltseekBar.getMax());
                }

                tiltseekBar.setProgress(tiltnow);
                panseekBar.setProgress(pannow);
                if (tiltnow != tiltprogress_pre) {
                    if (PanTiltConnectionType == PWM_PORT) {
                        PanTiltviaPWM(1, tiltnow);
                    } else {
                        sendG2Amessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                    }
                    //SendG2AMessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                    tiltprogress_pre = tiltnow;
                }
                if (pannow != panprogress_pre) {
                    if (PanTiltConnectionType == PWM_PORT) {
                        PanTiltviaPWM(0, pannow);
                    } else {
                        sendG2Amessage(pannow, AirGroundCom.PAN_CHANNEL);
                    }
                    //SendG2AMessage(pannow, AirGroundCom.PAN_CHANNEL);
                    panprogress_pre = pannow;
                }
                //Log.d(TAG, "onScroll: (int) (distanceX / PanSensitivityFactor) = " + (distanceX / PanSensitivityFactor));
                Log.d(TAG, "onScroll: distanceX since onDown = " + (motionEvent1.getX() - motionEvent.getX()));
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            if (System.currentTimeMillis() - mapontopMillis > 500) {
                mapontopMillis = System.currentTimeMillis();
                if (MapWindowDraggable) {
                    mapcontainer.setZ(3f);
                    FullScreenVideoActivity.controlsframe.setZ(2f);
                    showToast("Control within map");
                    MapOnTop = true;
                }
            }
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
            Log.d(TAG, "onTouch onFling: velocityX = " + velocityX + " velocityY = " + velocityY);
            /*int progress = 50;
            tiltseekBar.setProgress(progress);
            AirGroundCom.sendG2Amessage(progress, AirGroundCom.TILT_CHANNEL);*/
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            float X = motionEvent.getX() + PTZdetectionbox.getX(), Y = motionEvent.getY() + PTZdetectionbox.getY();
            float rangeX = 0, rangeY = 0;
            //if ((viewminX == 0) || (viewminY == 0) || (viewmaxX == 0) || (viewmaxY == 0)) {
            viewminX = PTZdetectionbox.getX();
            viewminY = PTZdetectionbox.getY();
            viewmaxX = PTZdetectionbox.getWidth() + viewminX;
            viewmaxY = PTZdetectionbox.getHeight() + viewminY;
            //}
            rangeX = (viewmaxX - viewminX);
            rangeY = (viewmaxY - viewminY);
            Log.d(TAG, "onDoubleTap: X = " + X + " from min " + viewminX + " to " + viewmaxX);
            //Log.d(TAG, "onDoubleTap: Y = " + Y + " from min " + viewminY + " to " + viewmaxY);
            int tiltnow = tiltseekBar.getProgress(), pannow = panseekBar.getProgress();
            if (X < viewmaxX - rangeX / 3) {
                pannow = 50;
            }
            if (X > viewminX + rangeX / 3) { // overlapping middle 1/3, where it resets both
                tiltnow = tiltseekBar.getMax() / 2;
            }
            panseekBar.setProgress(pannow);
            tiltseekBar.setProgress(tiltnow);
            if (PanTiltConnectionType == PWM_PORT) {
                PanTiltviaPWM(1, tiltnow);
                PanTiltviaPWM(0, pannow);
            } else {
                sendG2Amessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                //SendG2AMessage(tiltnow, AirGroundCom.TILT_CHANNEL);
                sendG2Amessage(pannow, AirGroundCom.PAN_CHANNEL);
                //SendG2AMessage(pannow, AirGroundCom.PAN_CHANNEL);
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
        }
    }

    private boolean CloseToPoint(float x, float y, float pointx, float pointy, float marginx, float marginy) {
        if ((x >= pointx - marginx)
                && (x <= pointx + marginx)
                && (y >= pointy - marginy)
                && (y <= pointy + marginy)) {
            return true;
        } else {
            return false;
        }
    }

    public static void ScalePTZdetectionbox(int size) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) PTZdetectionbox.getLayoutParams();
        // NOTE: here setting width is enough for rescaling and no need to adjust setX();
        // This is because PTZdetectionbox's parent is a constraint layout. And PTZdetectionbox is aligned to Right of parent and with
        // Horizontal bias of 100% to the right. PTZdetectionbox will keep sticking the right to the right of its parent and
        // will just resize only from the left to adjust to the new width.
        params.width = size;
        PTZdetectionbox.setLayoutParams(params);
    }

    private int normalizevalue(int i, int min, int max) {
        if (i < min) i = min;
        if (i > max) i = max;
        return i;
    }

    private int intscale = 10, inscale_pre = 10;
    public static float realScaleFactor = 1.0f;
    private float devision = 1.0f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor_pre = mScaleFactor;
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 8.0f));
            VideoFocusX_pre = VideoFocusX;
            VideoFocusY_pre = VideoFocusY;
            VideoFocusX = scaleGestureDetector.getFocusX() + PTZdetectionbox.getX();
            VideoFocusY = scaleGestureDetector.getFocusY() + PTZdetectionbox.getY();
            @SuppressLint("DefaultLocale") String Scale1digit = String.format("%.01fX", mScaleFactor);
            textViewZoomScale.setText(Scale1digit);
            textViewZoomScale.setVisibility(View.VISIBLE);
            StatusBarFrag.UpdatetextViewZoom();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewZoomScale.setVisibility(View.INVISIBLE);
                }
            }, 1000);
            //showToast(Scale1digit);

            // TODO: if doing MAVLink Geotagging, the camera zoom is NOT available. So let's not refactor the scalefactor
            if (MainActivity.Geotagging || !DataLinkConnected) {
                //FullScreenVideoActivity.ScaleVideo(mScaleFactor);
                FullScreenVideoActivity.ScaleVideo(mScaleFactor, VideoFocusX, VideoFocusY);
            } else {
                inscale_pre = intscale;
                if (isBoson || isBosonPi || isBosonPiM) {
                    intscale = (int) (100 * (mScaleFactor - 1f) / 7f);
                    if (intscale != inscale_pre) SetCameraZoom(intscale);
                } else {
                    if (mScaleFactor < 2.0f) {
                        realScaleFactor = mScaleFactor;
                        intscale = 1;//0;
                    } else if (mScaleFactor < 4.0f) {
                        realScaleFactor = mScaleFactor / 2.0f;
                        intscale = 2;//1;
                    } else {
                        realScaleFactor = mScaleFactor / 4.0f;
                        intscale = 3;//2;
                    }
                    if (GimmeraVersion == VERSION1X) {
                        //FullScreenVideoActivity.ScaleVideo(mScaleFactor); // Let's just still do real
                        FullScreenVideoActivity.ScaleVideo(mScaleFactor, VideoFocusX, VideoFocusY);
                    } else { //TODO need to receive confirmation from air end if zoom is available
                        //FullScreenVideoActivity.ScaleVideo(realScaleFactor); // Let's just still do real
                        FullScreenVideoActivity.ScaleVideo(realScaleFactor, VideoFocusX, VideoFocusY);
                    }
                    if (intscale != inscale_pre) {
                        SetCameraZoom(intscale); // This must be mScaleFactor, as it will be sent to the camera for doing actual camera zoom, not the screen zoom
                        /*final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (realScaleFactor >= 2.0)
                                    devision = 2.0f;
                                FullScreenVideoActivity.ScaleVideo(realScaleFactor/devision); // This must be realScaleFactor instead as it will reset the screen zoom once camera zoom has kicked in
                                Log.d(TAG, "ScaleListener onScale: after delay realScaleFactor = " + realScaleFactor);
                            }
                        }, 200); // Delay is to wait for the camera to actually have time to do the zoom. This is to make the apparent zoom a bit smoother*/
                    }
                }
            }
            //FullScreenVideoActivity.ScaleVideo(mScaleFactor, VideoFocusX, VideoFocusY);
            //FullScreenVideoActivity.ScaleVideo(mScaleFactor, mScaleFactor_pre, VideoFocusX, VideoFocusX_pre, VideoFocusY, VideoFocusY_pre);
            Log.d(TAG, "ScaleListener onScale: realScaleFactor = " + realScaleFactor);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            super.onScaleBegin(scaleGestureDetector);
            Log.d(TAG, "ScaleListener onScaleBegin: ");
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "ScaleListener onScaleEnd: ");
            super.onScaleEnd(detector);
        }
    }

    private void SetCameraZoom(int intScale) {
        if (GimmeraVersion == VERSION1X) {
            //ZoomviaPWM(); // Do NOTHING as this will mess up the way how to zoom and can't zoom back down to 1X. Screen zoom is enough
        } else {
            if (MainActivity.mTcpClient != null) {
                sendG2Amessage(intScale, AirGroundCom.ZOOM_CHANNEL);
                //new MainActivity.SendMessageTask().execute(valuestring);
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void showToast(final String msg) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toast.setMargin(0, 0.8f);
        toast.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setButtonVisibility(boolean isBoson, boolean isBosonPi) {
        if (isBoson) {
            paletteseekBar.setVisibility(View.VISIBLE);
            imageViewpalettesbkg.setVisibility(View.VISIBLE);
            gainseekBar.setVisibility(View.VISIBLE);
            allBosonsAtOnceButton.setVisibility(View.VISIBLE);
            imageViewgainbkg.setVisibility(View.VISIBLE);
            imageViewPalette1.setVisibility(View.GONE);
            imageViewPalette2.setVisibility(View.GONE);
            imageViewPalette3.setVisibility(View.GONE);
            /*if (isBosonPi) {
                imageViewRecordGimmera.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(gainseekBar.getWidth(), gainseekBar.getHeight());
                        ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(imageViewgainbkg.getWidth(), imageViewgainbkg.getHeight());
                        params.endToStart = R.id.imageViewRecordGimmera;
                        params.bottomToBottom = R.id.screenshot;
                        params.rightMargin = 18;//gainseekBar.getRight();
                        params1.endToStart = R.id.imageViewRecordGimmera;
                        params1.bottomToBottom = R.id.screenshot;
                        gainseekBar.setLayoutParams(params);
                        imageViewgainbkg.setLayoutParams(params1);
                    }
                }, 1000);
            } else*/ {
                imageViewRecordGimmera.setVisibility(View.GONE);
            }
            if(isBosonPiM){
                MultiCamZoomDetectionbox.setVisibility(View.VISIBLE);
            } else {
                MultiCamZoomDetectionbox.setVisibility(View.GONE);
            }
        } else {
            paletteseekBar.setVisibility(View.GONE);
            imageViewpalettesbkg.setVisibility(View.GONE);
            gainseekBar.setVisibility(View.GONE);
            allBosonsAtOnceButton.setVisibility(View.GONE);
            imageViewgainbkg.setVisibility(View.GONE);
            imageViewPalette1.setVisibility(View.VISIBLE);
            imageViewPalette2.setVisibility(View.VISIBLE);
            imageViewPalette3.setVisibility(View.VISIBLE);
            imageViewRecordGimmera.setVisibility(View.VISIBLE);
            MultiCamZoomDetectionbox.setVisibility(View.GONE);
        }
    }

    static Activity activity = null;
    static String now;
    static String day;
    static float alt = 0;
    static double lat = 0, lon = 0;

    /*static void takeScreenshotPIP() {
        now = (String) DateFormat.format("VuIR_yyyy-MM-dd_HH:mm:ss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        //Log.i(TAG, "takeScreenshot: day = " + day + " now = " + now);

        try {
            String mfolderpath = Environment.getExternalStorageDirectory() + "/VuIR_Media/" + day;
            File mFolder = new File(mfolderpath);

            if (!mFolder.exists()) {
                if (!mFolder.mkdir()) //directory is created;
                    return; //
            }
            // image naming and path  to include sd card  appending name you choose for file
            Bitmap bitmap = ((TextureView) textureViewThermalFrag).getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            w = (int) (W * 1.1f / 1.47f); // The float numbers are from XML file (fragment_thermal_video.xml)
            h = (int) (H * 1.0f / 1.065f);
            x = (W - w) / 2;
            y = CompleteWidgetActivity.dp2px; // from 8dp to pixels, android:translationY="8dp"
            //Log.i(TAG, String.format("takeScreenshot: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));

            Bitmap cropped_bitmap = createBitmap(bitmap, x, y, w, h);
            imageviewIRshot.setImageBitmap(cropped_bitmap);

            // Now capture the whole screen (picture-in-picture mode)
            float videoZ = textureViewThermalFrag.getZ();
            float imageZ = imageviewIRshot.getZ();
            textureViewThermalFrag.setZ(imageZ);
            imageviewIRshot.setZ(videoZ);
            imageviewIRshot.setVisibility(View.VISIBLE);

            String mPath = mfolderpath + "/" + now + "_PIP.jpg";
            File imageFile = new File(mPath);
            if (activity != null) {
                View v1 = activity.getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
            }

            textureViewThermalFrag.setZ(videoZ);
            imageviewIRshot.setZ(imageZ);
            imageviewIRshot.setVisibility(View.INVISIBLE);
            if ((lat != 0) && (lon != 0)) {
                GeoTagImage(mPath, alt, lat, lon);
            }

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }*/

    private boolean createFolder(String mFolderPath) {
        File mFolder = new File(mFolderPath);
        //Log.i(TAG, "createFolder: takeScreenshot creating folder " + mfolderpath);
        if (!mFolder.exists()) {
            if (!mFolder.mkdir()) {
                Log.i(TAG, "takeScreenshot: cannot create folder " + mFolderPath);
                return false; //
            }
        }
        return true;
    }

    private static String mPath;

    private void resizeScreenshotbimap640x512pixels() {
        final int wIR = 640;
        final int hIR = 512;
        final int margin = 5;
        if (screenshotpreviewlayout != null) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(wIR + 2 * margin, hIR + 2 * margin);//(ConstraintLayout.LayoutParams) screenshotpreviewlayout.getLayoutParams();
            //params.width = 650;
            //params.height = 522;
            //params.topToTop = R.id.controlfraglayout;
            //params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            //params.verticalBias = 0;
            //params.startToStart = R.id.controlfraglayout;
            //params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            //params.horizontalBias = 0;
            //params.topMargin = DensityUtil.dip2px(Objects.requireNonNull(getContext()), 200);
            //params.leftMargin = DensityUtil.dip2px(Objects.requireNonNull(getContext()), 60);
            float x, y;
            x = DensityUtil.dip2px(Objects.requireNonNull(getContext()), 60);
            y = DensityUtil.dip2px(Objects.requireNonNull(getContext()), 40);
            Log.i(TAG, "resizeScreenshotbimap640x512pixels: topMargin = " + params.topMargin + " leftmargin = " + params.leftMargin);
            screenshotpreviewlayout.setLayoutParams(params);
            screenshotpreviewlayout.setX(x);
            screenshotpreviewlayout.setY(y);
            if (screenshotbimap != null) {
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) screenshotbimap.getLayoutParams();
                params1.width = wIR;
                params1.height = hIR;
                params1.alignWithParent = true;
                params1.rightMargin = margin;
                params1.topMargin = margin;
                screenshotbimap.setLayoutParams(params1);
            }
        }
    }

    private static String mfolderpath = "";

    private void takeScreenshot() {
        //if (day == null || now == null) {
        now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        Log.i(TAG, "takeScreenshot: day = " + day + " now = " + now);
        //}

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";
                //mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";//Environment.getRootDirectory().getParent() +
            } else {
                mfolderpath = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com";//
            }

            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/VuIR_Media";
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/" + day;
            if (!createFolder(mfolderpath)) return;
            if (!createFolder(mfolderpath + "/thumbs")) return;

            // image naming and path  to include sd card  appending name you choose for file
            mPath = mfolderpath + "/" + now + ".jpg";
            final String thumbnailPath = mfolderpath + "/thumbs/" + now + "_thumb.jpg";
            Bitmap bitmap = FullScreenVideoActivity.videoViewMain.getBitmap();

            int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
            //h = H;//512;//(int) (H * 1.0f / 1.065f);
            //w = (int) (640f / 512 * H);//640//(int) (W * 1.1f / 1.47f); // The float numbers are from XML file (fragment_thermal_video.xml)
            if (mapwidth > 0) {
                w = mainWidth - mapwidth;
            } else {
                w = mainWidth;
            }
            x = (W - w) / 2;
            y = 16; // from 16dp to pixels, android:translationY="16dp" //TODO: set this value correctly depending on screen size
            h = (int) ((H / 1.065f) - y); //512
            Log.i(TAG, String.format("takeScreenshot: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));

            Point point = new Point(560, 20);
            final Bitmap cropped_bitmap = mark(createScaledBitmap(createBitmap(bitmap, x, y, w, h), 640, 512, false),
                    "sUAS.com", point, Color.GREEN, 127, 16, false);

            screenshotpreview.setImageBitmap(cropped_bitmap);
            screenshotpreview.setScaleType(ImageView.ScaleType.FIT_XY);
            screenshotpreviewlayout.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    File imageFile = new File(mPath);

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    int quality = 100;
                    cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    try {
                        assert outputStream != null;
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if ((lat != 0) && (lon != 0)) {
                        GeoTagImage(mPath, alt, lat, lon);
                    }

                    ///Saving a thumbnail of this
                    imageFile = new File(thumbnailPath);
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap thumbbitmap = createScaledBitmap(cropped_bitmap, 80, 64, false);
                    thumbbitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 20);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    screenshotpreviewlayout.setVisibility(View.GONE);
                }
            }, 1000);

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    //https://stackoverflow.com/questions/10679445/how-might-i-add-a-watermark-effect-to-an-image-in-android
    private Bitmap mark(Bitmap src, String watermark, Point location, int color, int alpha, int size, boolean underline) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(watermark, location.x, location.y, paint);

        return result;
    }

    //https://techspread.wordpress.com/2014/04/07/write-read-geotag-jpegs-exif-data-in-android-gps/
    // with my own modifications, Tony Ngo.
    @SuppressLint("DefaultLocale")
    private static void GeoTagImage(String imagePath, float altitude, double latitude, double longitude) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, GPS.convert(altitude, 1000));
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "0"); //0 if the altitude is above sea level. 1 if the altitude is below sea level. Type is int.
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPS.convert(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPS.latitudeRef(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPS.convert(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPS.longitudeRef(longitude));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            Log.i(EventBus.TAG, "GeoTagImage: fmt_Exif.format(Calendar.getInstance().getTime()) = " + fmt_Exif.format(Calendar.getInstance().getTime()));
            exif.setAttribute(ExifInterface.TAG_DATETIME, fmt_Exif.format(Calendar.getInstance().getTime()));
            exif.saveAttributes(); // This could be an expensive action because it needs to copy an image, delete it then save as a new one.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Code to convert  Degrees to DMS unit
    private static class GPS {
        private static StringBuilder sb = new StringBuilder(25);

        /**
         * returns ref for latitude which is S or N.
         *
         * @param latitude
         * @return S or N
         */
        static String latitudeRef(final double latitude) {
            return latitude < 0.0d ? "S" : "N";
        }

        /**
         * returns ref for latitude which is S or N.
         * <p>
         * //@param latitude
         *
         * @return S or N
         */
        static String longitudeRef(final double longitude) {
            return longitude < 0.0d ? "W" : "E";
        }

        /**
         * convert latitude into DMS (degree minute second) format. For instance<br/>
         * -79.948862 becomes<br/>
         * 79/1,56/1,55903/1000<br/>
         * It works for latitude and longitude<br/>
         *
         * @param latitude could be longitude.
         * @return
         */
        static String convert(double latitude) {
            latitude = Math.abs(latitude);
            final int degree = (int) latitude;
            latitude *= 60;
            latitude -= degree * 60.0d;
            final int minute = (int) latitude;
            latitude *= 60;
            latitude -= minute * 60.0d;
            final int second = (int) (latitude * 100000.0d);

            sb.setLength(0);
            sb.append(degree)
                    .append("/1,")
                    .append(minute)
                    .append("/1,")
                    .append(second)
                    .append("/100000,");
            return sb.toString();
        }

        static String convert(float altitude, int factor) {
            final int int_altitude = (int) (altitude * factor);

            sb.setLength(0);
            sb.append(int_altitude)
                    .append("/")
                    .append(factor)
                    .append(",");
            return sb.toString();
        }
    }

    AndroidSequenceEncoder encoder;
    boolean needtoSaveThumb = true;

    private void createIRVideo() {
        SeekableByteChannel out = null;
        String mfolderpath = "";
        String name, name_check = "";

        day = (String) DateFormat.format("yyyy-MM-dd", new Date());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media/" + day + "/frames";
        } else {
            mfolderpath = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com/VuIR_Media/" + day + "/frames";
        }

        if (!createFolder(mfolderpath)) return;

        File f = new File(mfolderpath);
        File[] file = f.listFiles();
        if (file != null) {
            Arrays.sort(file);
            for (int i = 0; i < file.length; i++) {
                if (REC == 1) break;
                //Log.i(TAG, "createIRVideo: name i = " + file[i].getName());
                if (file[i].getName().contains("jpg")) {
                    name = file[i].getName().substring(0, 6);
                    if (!name.equals(name_check)) {
                        if (encoder != null) {
                            try {
                                encoder.finish();
                                encoder = null;
                                needtoSaveThumb = true;
                                Log.i(TAG, "createIRVideo: finished");
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "createIRVideo: ", e);
                            }
                        }
                        Log.i(TAG, "createIRVideo: name = " + name);
                        name_check = name;
                        String videoname = mfolderpath.substring(0, mfolderpath.lastIndexOf('/')) + "/" + name + ".mp4";
                        File videoFile = new File(videoname);
                        if (videoFile.exists()) {
                            continue; //continue to the next IMAGE file to create a different video
                        }
                        try {
                            out = NIOUtils.writableFileChannel(videoname);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.e(TAG, "createIRVideo: ", e);
                        }
                        // for Android use: AndroidSequenceEncoder
                        try {
                            int effective_FPS = 25;
                            encoder = new AndroidSequenceEncoder(out, Rational.R(effective_FPS, 1)); //TODO calculate better the FPS here.
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "createIRVideo: ", e);
                        }
                        //Log.i(TAG, "createIRVideo: encoder = " + encoder);
                    }
                    try {
                        if (encoder != null) {
                            Log.i(TAG, "createIRVideo: readying bitmap file " + file[i].getName());
                            Bitmap bitmap = BitmapFactory.decodeFile(mfolderpath + "/" + file[i].getName());
                            if (bitmap != null) {
                                encoder.encodeImage(bitmap);
                                Log.i(TAG, "createIRVideo: bitmap " + file[i].getName() + " encoded");
                            }
                            if (needtoSaveThumb) {
                                needtoSaveThumb = false;
                                File thumb = new File(mfolderpath.substring(0, mfolderpath.lastIndexOf('/')) + "/thumbs/" + name + "_thumb.jpg");
                                Bitmap thumbbitmap = createScaledBitmap(bitmap, 80, 64, false);
                                FileOutputStream outputStream = null;
                                try {
                                    outputStream = new FileOutputStream(thumb);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                int quality = 100;
                                thumbbitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                                try {
                                    assert outputStream != null;
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "createIRVideo: ", e);
                    }
                }
            }
            if (encoder != null) {
                try {
                    encoder.finish();
                    encoder = null;
                    needtoSaveThumb = true;
                    Log.i(TAG, "createIRVideo: finished");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "createIRVideo: ", e);
                }
                for (int i = 0; i < file.length; i++) {
                    if (file[i].getName().contains("jpg")) {
                        file[i].delete();
                    }
                }
            }
        }
    }

    private static boolean donotrecord = true;
    private static int frameNo = 0;
    private static int H = 512, h = 512, W = 640;

    private void prepareFrames() {
        now = (String) DateFormat.format("HHmmss", new Date());
        day = (String) DateFormat.format("yyyy-MM-dd", new Date());

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";
            } else {
                mfolderpath = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com";//
            }
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/VuIR_Media";
            if (!createFolder(mfolderpath)) return;
            mfolderpath += "/" + day;
            if (!createFolder(mfolderpath)) return;
            if (!createFolder(mfolderpath + "/frames")) return;
            donotrecord = false;
            frameNo = 0;
            //Bitmap bitmap = FullScreenVideoActivity.videoViewMain.getBitmap();
            Bitmap bitmap = FullScreenVideoActivity.vidrecSurfaceview.getBitmap();
            H = bitmap.getHeight();
            h = (int) ((H / 1.065f) - 16);
            W = bitmap.getWidth();
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        donotrecord = true;
    }

    @SuppressLint("DefaultLocale")
    static void createFrames() {
        if (donotrecord) return;
        mPath = mfolderpath + "/frames/" + now + String.format("%04d", frameNo++) + ".jpg";
        //Bitmap bitmap = FullScreenVideoActivity.videoViewMain.getBitmap();//(W, h);
        Bitmap bitmap = FullScreenVideoActivity.vidrecSurfaceview.getBitmap();//(W, h);

        int x, y, w, h, W = bitmap.getWidth(), H = bitmap.getHeight();
        w = 320;////640;
        x = (W - w) / 2;
        y = 8; // from 16dp to pixels, android:translationY="16dp" //TODO: set this value correctly depending on screen size
        h = 256;//512;
        //Log.i(TAG, String.format("createFrames: y = %d, x = %d, w = %d, h = %d, W = %d, H = %d", y, x, w, h, W, H));

        final Bitmap cropped_bitmap = createScaledBitmap(createBitmap(bitmap, x, y, w, h), w, h, false);

        File imageFile = new File(mPath);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int quality = 100;
        cropped_bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        try {
            assert outputStream != null;
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
