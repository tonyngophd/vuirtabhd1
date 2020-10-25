package com.suas.vuirtab1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainHeight;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainWidth;
import static com.suas.vuirtab1.FullScreenVideoActivity.mapwidth;
import static com.suas.vuirtab1.SettingsFragment.toggleButtonMicRec;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScreenRecordingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScreenRecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScreenRecordingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "ScreenRecordingFragment";
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static int DISPLAY_WIDTH = (mainWidth > 0) ? mainWidth : 1024;//700;
    private static int DISPLAY_HEIGHT = (mainHeight > 0) ? mainHeight : 768;//700;1080;//512;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    @SuppressLint("StaticFieldLeak")
    static ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    private static Intent staticIntentData;
    private static int staticResultCode;

    private Activity parentActivity;
    private View rootView;

    private final boolean DEBUG_ON = false;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ScreenRecordingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScreenRecordingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScreenRecordingFragment newInstance(String param1, String param2) {
        ScreenRecordingFragment fragment = new ScreenRecordingFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentActivity = getActivity();
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_screen_recording, container, false);

        DisplayMetrics metrics = new DisplayMetrics();
        parentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) parentActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mToggleButton = (ToggleButton) rootView.findViewById(R.id.toggle);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (ContextCompat.checkSelfPermission(parentActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(parentActivity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    if(DEBUG_ON) Log.d(TAG, "onCheckedChanged: 1");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, android.Manifest.permission.RECORD_AUDIO)) {
                        if(DEBUG_ON) Log.d(TAG, "onCheckedChanged: 2");
                        mToggleButton.setChecked(false);
                        Snackbar.make(rootView.findViewById(android.R.id.content), R.string.label_permissions,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(DEBUG_ON) Log.d(TAG, "onClick: 3");
                                        ActivityCompat.requestPermissions(parentActivity,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        if(DEBUG_ON) Log.d(TAG, "onCheckedChanged: 4");
                        ActivityCompat.requestPermissions(parentActivity,
                                new String[]{android.Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    if(DEBUG_ON) Log.i(TAG, "onCheckedChanged: Recording ischecked = " + isChecked);
                    onToggleScreenShare(compoundButton);
                }
            }

        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(parentActivity,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mToggleButton.setChecked(false);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        //mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        if(staticResultCode == 0 && staticIntentData == null) {
            if(DEBUG_ON) Log.d(TAG, "onActivityResult: initializing mMediaProjection");
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            staticIntentData = data;
            staticResultCode = resultCode;
            if(DEBUG_ON) Log.d(TAG, "onActivityResult: mMediaProjection initiated");
        } else {
            if(DEBUG_ON) Log.d(TAG, "onActivityResult: getMediaProjection this should NOT ask user for permission again");
            mMediaProjection = mProjectionManager.getMediaProjection(staticResultCode, staticIntentData);
        }

        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        if(DEBUG_ON) Log.i(TAG, String.format("onActivityResult: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
    }

    private void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            initRecorder();
            shareScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // https://stackoverflow.com/questions/58791474/api-level-29-mediaprojection-is-always-requesting-permission
                // For Q (android 10) or above, our temporary solution is to NOT stop screen sharing to reuse everything
                // The reason is for Android to NOT re-poping the security question again, which annoys users
                // The security questions just needs to be asked once in a session of FullScreenActivity only.
                // Subsequence screen recording should happen smoothly without the question being popped again
                // TODO: in the future, for Android Q and above, look for a way to stop screen share to reduce pending resources
                stopScreenSharing();
            }
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null || staticIntentData == null) {
            if(DEBUG_ON) Log.d(TAG, "shareScreen: mMediaProjection = " + mMediaProjection + " staticIntentData = " + staticIntentData);
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        if(DEBUG_ON) Log.d(TAG, "shareScreen: 1");
        mVirtualDisplay = createVirtualDisplay();
        //if(DEBUG_ON) Log.i(TAG, String.format("shareScreen: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
        mMediaRecorder.start();
        if(DEBUG_ON) Log.d(TAG, "shareScreen: 2");
    }

    static void setScreenRecordResolution(int width, int height) {
        DISPLAY_WIDTH = (width > 0) ? width : 1024;
        DISPLAY_HEIGHT = (height > 0) ? height : 768;
        //if(DEBUG_ON) Log.i(TAG, String.format("setScreenRecordResolution: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("parentActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private String now;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecorder() {
        if(DEBUG_ON) Log.d(TAG, "initRecorder: starting now");
        now = (String) DateFormat.format("VuIR_yyyyMMdd_HHmmss", new Date());
        String day = (String) DateFormat.format("yyyy-MM-dd", new Date());

        String mfolderpath;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mfolderpath = Environment.getExternalStorageDirectory() + "/sUAS.com";
        } else {
            mfolderpath = Objects.requireNonNull(getActivity()).getExternalFilesDir(null) + "/sUAS.com";//
        }
        if(DEBUG_ON) Log.d(TAG, "initRecorder: mfolderpath = " + mfolderpath);
        if (!createFolder(mfolderpath)) {
            if(DEBUG_ON) Log.d(TAG, "initRecorder: couldn't create folder " + mfolderpath);
            return;
        }
        mfolderpath += "/VuIR_Media";
        if (!createFolder(mfolderpath)) {
            if(DEBUG_ON) Log.d(TAG, "initRecorder: couldn't create folder " + mfolderpath);
            return;
        }
        mfolderpath += "/" + day;
        if (!createFolder(mfolderpath)) {
            if(DEBUG_ON) Log.d(TAG, "initRecorder: couldn't create folder " + mfolderpath);
            return;
        }

        boolean recordMic = toggleButtonMicRec.isChecked();

        try {
            if (recordMic) {
                //AudioManager am = (AudioManager) parentActivity.getSystemService(Context.AUDIO_SERVICE);
                //assert am != null;
                //am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_PLAY_SOUND);
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//MPEG_4
            mMediaRecorder.setOutputFile(mfolderpath + "/" + now + ".mp4");
            //if(DEBUG_ON) Log.i(TAG, "initRecorder: frameno recording to file" + (mfolderpath + "/" + now + ".mp4"));
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            if(DEBUG_ON) Log.i(TAG, String.format("initRecorder setVideoSize: DISPLAY_WIDTH = %d, DISPLAY_HEIGHT = %d", DISPLAY_WIDTH, DISPLAY_HEIGHT));
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (recordMic) {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//DEFAULT);// .
                //mMediaRecorder.setAudioEncodingBitRate(512000); //256000 bps = 256 kbps
            }
            //mMediaRecorder.setAudioEncodingBitRate
            //if (android.os.Build.VERSION.SDK_INT > 26)
            if (mainWidth > 1024)
                mMediaRecorder.setVideoEncodingBitRate(512 * 1000 * 22);
            else
                mMediaRecorder.setVideoEncodingBitRate(512 * 1000 * 8);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = parentActivity.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            makeThumbnail(mfolderpath);
        } catch (IOException e) {
            Log.e(TAG, "initRecorder: ", e);
            e.printStackTrace();
        }
    }

    private void makeThumbnail(String mfolderpath) {
        try {
            if (!createFolder(mfolderpath + "/thumbs")) return;
            // image naming and path  to include sd card  appending name you choose for file
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

            final Bitmap thumbbitmap = createScaledBitmap(createBitmap(bitmap, x, y, w, h), 80, 64, false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    ///Saving a thumbnail of this
                    File imageFile = new File(thumbnailPath);
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (outputStream != null) {
                        thumbbitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        try {
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 20);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private boolean createFolder(String mFolderPath) {
        File mFolder = new File(mFolderPath);
        //if(DEBUG_ON) Log.i(TAG, "createFolder: takeScreenshot creating folder " + mfolderpath);
        if (!mFolder.exists()) {
            if (!mFolder.mkdir()) {
                if(DEBUG_ON) Log.i(TAG, "takeScreenshot: cannot create folder " + mFolderPath);
                return false; //
            }
        }
        return true;
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
            if(DEBUG_ON) Log.d(TAG, "onStop: ScreenRecordingFragment");
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
        if(DEBUG_ON) Log.d(TAG, "onDestroy: ScreenRecordingFragment");
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if(DEBUG_ON) Log.i(TAG, "ScreenRecordingFragment MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if(DEBUG_ON) Log.d(TAG, "onRequestPermissionsResult: 1");
            if ((grantResults.length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                if(DEBUG_ON) Log.d(TAG, "onRequestPermissionsResult: 2");
                onToggleScreenShare(mToggleButton);
            } else {
                if(DEBUG_ON) Log.d(TAG, "onRequestPermissionsResult: 3");
                mToggleButton.setChecked(false);
                Snackbar.make(rootView.findViewById(android.R.id.content), R.string.label_permissions,
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setData(Uri.parse("package:" + parentActivity.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        View decorView = parentActivity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
