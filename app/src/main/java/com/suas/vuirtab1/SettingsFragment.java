package com.suas.vuirtab1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.suas.vuirtab1.AirGroundCom.sendG2Amessage;
import static com.suas.vuirtab1.ControlsFragment.PWM_PORT;
import static com.suas.vuirtab1.ControlsFragment.SERIAL_PORT;
import static com.suas.vuirtab1.ControlsFragment.SetGimmeraVersion;
import static com.suas.vuirtab1.ControlsFragment.SetPanTiltConnectionType;
import static com.suas.vuirtab1.FullScreenVideoActivity.DataLinkConnected;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainHeight;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainWidth;
import static com.suas.vuirtab1.GridOverVideoFragment.HorizontalGridNo;
import static com.suas.vuirtab1.GridOverVideoFragment.VerticalGridNo;
import static com.suas.vuirtab1.GridOverVideoFragment.pixelGrid;
import static com.suas.vuirtab1.MainActivity.speak;
import static com.suas.vuirtab1.ScreenRecordingFragment.setScreenRecordResolution;

//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ToggleButton toggleButtonGeotagging;
    static ToggleButton toggleButtonMicRec;
    private ToggleButton voiceReminderToggle;
    private RadioButton radioButton1080;
    private RadioButton radioButton720;
    private RadioButton radioButton576;
    private RadioButton radioButton288;
    private RadioButton radioButtonVersion10;
    private RadioButton radioButton512s;
    private RadioButton radioButton720rec;
    private RadioButton radioButton1080rec;
    private RadioButton radioButtonScreenSize;
    private RadioGroup radioGroupResolution;
    private RadioGroup radiogroupConnectionType;
    static RadioGroup radiogroupPanTiltType;
    private RadioGroup radioGroupAspectRatio;
    private RadioGroup screenrecresgroup;
    private static RadioGroup radiogroupGimmeraVersion = null;
    private int pantiltType;
    private static int gimmeraVersion;
    private RadioButton radioButtonSerial, radioButtonPWM;
    private RadioButton radioButtonFullScreen, radioButton640, radioButton336, radioButton43;
    static Switch switchVidtoPic;
    static ImageView imageViewPicmode, imageViewVidmode;
    static SeekBar seekBarBosonVidQuality;
    static TextView seekBarBosonVidQualityText;
    boolean SwitchVid2PicChecked = false;
    private ImageView recordmicImageView, voiceReminderIV;
    private static int RECMode = 1;
    private SeekBar seekBarTiltSmoothness, seekBarPanSmoothness, seekBarHorGrid, seekBarVerGrid;
    private static int TiltSmoothness = 50, PanSmoothness = 50;
    public static final String PREFS_NAME = "VuIRPrefsFile";
    private static int AspectRatioID;
    private static int intMapTransparencySettingFrag = 100;
    private static TextView textViewGridNoH, textViewGridNoV;
    final int DarkBlue = Color.argb(0xAA, 0x25, 0x82, 0xCE);//"#AA2582CE"
    private boolean voiceReminderOn = true;
    private int userVolume = 10, maxVolume = 15;
    private Activity activity;
    private AudioManager audioManager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button closeButton = (Button) view.findViewById(R.id.buttonSettingsFragClose);
        toggleButtonGeotagging = (ToggleButton) view.findViewById(R.id.toggleButtonGeotagging);
        toggleButtonMicRec = (ToggleButton) view.findViewById(R.id.toggleButtonMicRec);
        voiceReminderToggle = (ToggleButton) view.findViewById(R.id.voiceReminderToggle);
        radioButton1080 = (RadioButton) view.findViewById(R.id.radioButton1080);
        radioButton720 = (RadioButton) view.findViewById(R.id.radioButton720);
        radioButton576 = (RadioButton) view.findViewById(R.id.radioButton576);
        radioButton288 = (RadioButton) view.findViewById(R.id.radioButton288);

        radioButton512s = (RadioButton) view.findViewById(R.id.radioButton512s);
        radioButton720rec = (RadioButton) view.findViewById(R.id.radioButton720rec);
        radioButton1080rec = (RadioButton) view.findViewById(R.id.radioButton1080rec);
        radioButtonScreenSize = (RadioButton) view.findViewById(R.id.radioButtonScreenSize);

        radioButtonFullScreen = (RadioButton) view.findViewById(R.id.radioButtonFullScreen);
        radioButton640 = (RadioButton) view.findViewById(R.id.radioButton640);
        radioButton336 = (RadioButton) view.findViewById(R.id.radioButton336);
        radioButton43 = (RadioButton) view.findViewById(R.id.radioButton43);

        radioButtonVersion10 = (RadioButton) view.findViewById(R.id.radioButtonVersion10);

        radioGroupResolution = (RadioGroup) view.findViewById(R.id.radioGroupResolution);
        radiogroupConnectionType = (RadioGroup) view.findViewById(R.id.radiogroupConnectionType);
        radiogroupPanTiltType = (RadioGroup) view.findViewById(R.id.radiogroupPanTiltType);
        radiogroupGimmeraVersion = (RadioGroup) view.findViewById(R.id.radiogroupGimmeraVersion);
        radioGroupAspectRatio = (RadioGroup) view.findViewById(R.id.radioGroupAspectRatio);
        screenrecresgroup = (RadioGroup) view.findViewById(R.id.screenrecresgroup);


        radioButtonSerial = (RadioButton) view.findViewById(R.id.radioButtonSerial);
        radioButtonPWM = (RadioButton) view.findViewById(R.id.radioButtonPWM);

        switchVidtoPic = (Switch) view.findViewById(R.id.switchVidtoPic);
        imageViewPicmode = (ImageView) view.findViewById(R.id.imageViewPicmode);
        imageViewVidmode = (ImageView) view.findViewById(R.id.imageViewVidmode);
        seekBarBosonVidQuality = (SeekBar) view.findViewById(R.id.seekBarBosonVidQuality);
        seekBarBosonVidQualityText = (TextView) view.findViewById(R.id.seekBarBosonVidQualityText);
        recordmicImageView = (ImageView) view.findViewById(R.id.recordmicImageView);
        voiceReminderIV = (ImageView) view.findViewById(R.id.voiceReminderIV);
        seekBarTiltSmoothness = (SeekBar) view.findViewById(R.id.seekBarTiltSmoothness);
        seekBarPanSmoothness = (SeekBar) view.findViewById(R.id.seekBarPanSmoothness);
        seekBarHorGrid = (SeekBar) view.findViewById(R.id.seekBarHorizontalGridNo);
        seekBarVerGrid = (SeekBar) view.findViewById(R.id.seekBarVerticalGridNo);
        textViewGridNoH = (TextView) view.findViewById(R.id.textViewGridNoH);
        textViewGridNoV = (TextView) view.findViewById(R.id.textViewGridNoV);

        RestoreUserSettings();

        seekBarBosonVidQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float minQual = 20f, maxQual = 80f;
                int progress = seekBar.getProgress();
                int quality = (int)(progress*(maxQual-minQual)/10f + 20);
                Log.d(TAG, "onStopTrackingTouch: video quality = " + quality);
                sendG2Amessage(quality, AirGroundCom.GIM_VIDQUAL_CHANNEL);
            }
        });
        seekBarHorGrid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                HorizontalGridNo = progress + 2;
                pixelGrid.setNumRows(HorizontalGridNo);
                textViewGridNoH.setText((HorizontalGridNo - 1) + "");
                SaveUserSettingInt("Horizontal Grids", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarVerGrid.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                VerticalGridNo = progress + 2;
                textViewGridNoV.setText((VerticalGridNo - 1) + "");
                pixelGrid.setNumColumns(VerticalGridNo);
                SaveUserSettingInt("Vertical Grids", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarTiltSmoothness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TiltSmoothness = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch: TiltSmoothness = " + TiltSmoothness);
                ControlsFragment.UpdateTiltSmoothness(TiltSmoothness);
                SaveUserSettingInt("TiltSmoothness", TiltSmoothness);
                Log.i(TAG, "onStopTrackingTouch: TiltSmoothness = " + TiltSmoothness);
            }
        });

        seekBarPanSmoothness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PanSmoothness = seekBar.getProgress();
                Log.d(TAG, "onStopTrackingTouch: PanSmoothness = " + PanSmoothness);
                ControlsFragment.UpdatePanSmoothness(PanSmoothness);
                SaveUserSettingInt("PanSmoothness", PanSmoothness);
                Log.i(TAG, "onStopTrackingTouch: PanSmoothness = " + PanSmoothness);
            }
        });

        switchVidtoPic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                ControlsFragment.setRecordButtonView(isChecked);
                if (isChecked) {
                    imageViewPicmode.setImageResource(R.drawable.ic_photo_camera_red_48dp);
                    imageViewVidmode.setImageResource(R.drawable.ic_videocam_black_48dp);
                    RECMode = 0;
                } else {
                    imageViewPicmode.setImageResource(R.drawable.ic_photo_camera_black_48dp);
                    imageViewVidmode.setImageResource(R.drawable.ic_videocam_red_48dp);
                    RECMode = 1;
                }
                ControlsFragment.CopyRecMode(RECMode);
                SwitchVid2PicChecked = isChecked;
                SaveUserSettingBoolean("SwitchVid2Pic", SwitchVid2PicChecked);
            }
        });

        radioGroupResolution.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                int mWidth, mHeight;
                switch (checkedId) {
                    case R.id.radioButton1080:
                        mWidth = 1920;
                        mHeight = 1080;
                        break;
                    case R.id.radioButton720:
                        mWidth = 1280;
                        mHeight = 720;
                        break;
                    case R.id.radioButton576:
                        mWidth = 720;
                        mHeight = 576;
                        break;
                    case R.id.radioButton288:
                        mWidth = 352;
                        mHeight = 288;
                        break;
                    default:
                        mWidth = 1280;
                        mHeight = 720;
                        break;
                }
                Log.d(TAG, "onCheckedChanged: id = " + checkedId + " width = " + mWidth + " height = " + mHeight);
                MainActivity.mWidth = mWidth;
                MainActivity.mHeight = mHeight;
                MainActivity.SetResolution(mWidth, mHeight);
                StatusBarFrag.UpdateStatusBar();
            }
        });

        radiogroupGimmeraVersion.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonVersion10:
                        SetGimmeraVersion(1);
                        pantiltType = R.id.radioButtonPWM;
                        SaveUserSettingInt("PanTiltConnType", pantiltType);
                        radiogroupPanTiltType.check(pantiltType);
                        SetPanTiltConnectionType(PWM_PORT);
                        break;
                    case R.id.radioButtonVersion20:
                        SetGimmeraVersion(2);
                        break;
                    default:
                        pantiltType = R.id.radioButtonPWM;
                        SaveUserSettingInt("PanTiltConnType", pantiltType);
                        radiogroupPanTiltType.check(pantiltType);
                        SetPanTiltConnectionType(PWM_PORT);
                        SetGimmeraVersion(1);
                        break;
                }
                gimmeraVersion = checkedId;
                SaveUserSettingInt("Gimmera Version", gimmeraVersion);
            }
        });
        radiogroupPanTiltType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonSerial:
                        SetPanTiltConnectionType(SERIAL_PORT);
                        break;
                    case R.id.radioButtonPWM:
                        SetPanTiltConnectionType(PWM_PORT);
                        break;
                    default:
                        SetPanTiltConnectionType(SERIAL_PORT);
                        break;
                }
                pantiltType = checkedId;
                SaveUserSettingInt("PanTiltConnType", pantiltType);
            }
        });

        radiogroupConnectionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                final int connection;
                switch (checkedId) {
                    case R.id.radioButtonTCP:
                        connection = 0;
                        break;
                    case R.id.radioButtonUDP:
                        connection = 1;
                        break;
                    default:
                        connection = 0;
                        break;
                }
                FullScreenVideoActivity.setConnectionValue(connection);
                Log.d(TAG, "will stop native first onCheckedChanged: connection = " + connection);
            }
        });


        radioGroupAspectRatio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            float desiredScale = -1f;

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonFullScreen:
                        desiredScale = -1f;
                        break;
                    case R.id.radioButton640:
                        desiredScale = 640 * 1f / 512;
                        break;
                    case R.id.radioButton336:
                        desiredScale = 336 * 1f / 256;
                        break;
                    case R.id.radioButton43:
                        desiredScale = 4 * 1f / 3;
                        break;
                }
                FullScreenVideoActivity.SetAspectRatio(ControlsFragment.realScaleFactor, desiredScale);
                AspectRatioID = checkedId;
                SaveUserSettingInt("Aspect Ratio", AspectRatioID);
            }
        });

        screenrecresgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (mainHeight < 1080 && mainHeight > 0) {
                    radioButton1080rec.setEnabled(false);
                }
                if (mainHeight > 0)
                    radioButtonScreenSize.setText("Screen size (" + mainHeight + "p)");
                int width = 640, height = 512;
                switch (checkedId) {
                    case R.id.radioButton512s:
                        height = 512;
                        break;
                    case R.id.radioButton720rec:
                        height = 720;
                        break;
                    case R.id.radioButton1080rec:
                        height = 1080;
                        break;
                    case R.id.radioButtonScreenSize:
                        height = (mainHeight > 0) ? mainHeight : 768;
                        break;
                }
                SaveUserSettingInt("Record Resolution", checkedId);
                if (mainWidth > 0 && mainHeight > 0) {
                    width = ((int) (mainWidth * height * 1f / mainHeight)) * 2 / 2;
                }
                Log.i(TAG, "onCheckedChanged screen rec: width = " + width + " height = " + height);
                setScreenRecordResolution(width, height);
                //Todo: remember user selection at every load
            }
        });

        toggleButtonMicRec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SaveUserSettingBoolean("Record Voice", isChecked);
                if (isChecked) {
                    recordmicImageView.setImageResource(R.drawable.ic_mic_black_24dp);
                    recordmicImageView.setColorFilter(DarkBlue);
                } else {
                    recordmicImageView.setImageResource(R.drawable.ic_mic_off_black_24dp);
                    recordmicImageView.setColorFilter(Color.GRAY);
                }
            }
        });

        voiceReminderToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                voiceReminderOn = isChecked;//
                SaveUserSettingBoolean("Voice Reminder", isChecked);
                if (isChecked) {
                    voiceReminderIV.setColorFilter(DarkBlue);
                } else {
                    voiceReminderIV.setColorFilter(Color.GRAY);
                }
            }
        });
        //VideoWindow.StartVideo(this.mserverip, type);
        toggleButtonGeotagging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MainActivity.Geotagging = isChecked;
                StatusBarFrag.UpdateStatusBar();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullScreenVideoActivity.HideSettingsFragment();
            }
        });

        /* //The following listener sometimes causes crash when the app is running and the connection reestablishes after a disconnection
        // So it's replaced with SetGimeraVersion_RealTimeUpdate(boolean connected) instead, which is called within MainActivity when
        // there is a change in connection status
        MainActivity.ConnectStatustextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i(TAG, "onTextChanged: charSequence = " + charSequence);
                SetGimeraVersion_RealTimeUpdate(MainActivity.ConnectionStatus);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });*/

        getAudioVolume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (voiceReminderOn) {
                    if (reminderInt >= 5) {
                        if (audioManager != null) {
                            if (!audioManager.isVolumeFixed()) {
                                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVolume, AudioManager.FLAG_PLAY_SOUND);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
                                Log.i(TAG, "run: maxvolume = " + maxVolume);
                            }
                        }

                        confirm();
                        speak("Please set your drone in 2.4 gigahertz mode to minimize interference. Thanks!");
                    }
                }
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, userVolume, AudioManager.FLAG_PLAY_SOUND);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, AudioManager.FLAG_PLAY_SOUND);
            }
        }, 10000);

        return view;
    }

    private void getAudioVolume() {
        activity = getActivity();
        if (activity != null) {
            audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (!audioManager.isVolumeFixed()) {
                    //userVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    Log.i(TAG, "run: maxvolume userVolume = " + userVolume);
                }
            }
            SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            reminderInt = settings.getInt("Reminder Snooze", 5);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("Reminder Snooze", reminderInt + 1); //Read the saved value, increase 1 and save it back in for
            editor.apply();
        }
    }

    private int reminderInt = 5;

    private void confirm() {
        //https://stackoverflow.com/questions/13675822/android-alertdialog-builder with my own modifications
        //https://stackoverflow.com/questions/18346920/change-the-background-color-of-a-pop-up-dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme);//AlertDialog.THEME_HOLO_LIGHT);

        // set title
        alertDialogBuilder.setTitle("Set Radio Frequency");
        alertDialogBuilder.setIcon(R.drawable.ic_wifi_black_24dp);

        // set dialog message
        alertDialogBuilder
                .setMessage("Please switch the drone to 2.4ghz to minimize interference with VuIR system!")
                .setCancelable(false)
                .setNegativeButton(R.string.snooze, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        //dialog.cancel();
                        //selecttoggle.setChecked(false);
                        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        reminderInt = 1;
                        editor.putInt("Reminder Snooze", reminderInt);
                        editor.apply();
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                })
                .setPositiveButton(R.string.ok_long, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        HideAndroidBottomNavigationBarforTrueFullScreenView();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        //https://stackoverflow.com/questions/4406804/how-to-control-the-width-and-height-of-the-default-alert-dialog-in-android
        //Objects.requireNonNull(alertDialog.getWindow()).setLayout(400, 200);
    }

    private void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        if (activity == null) return;
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    static void GetFullScreenSettingValues() {
        intMapTransparencySettingFrag = FullScreenVideoActivity.intMapTransparency;
    }

    private void SaveUserSettingInt(String settingName, int settingValue) {
        try {
            SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void SaveUserSettingBoolean(String settingName, boolean settingValue) {
        try {
            SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(settingName, settingValue);
            editor.apply();
        } catch (Exception ignored) {
        }
    }

    private void SetGimeraVersion_RealTimeUpdate(String connectionStatus) {
        SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (connectionStatus.contains(getString(R.string.connected))) {
            gimmeraVersion = R.id.radioButtonVersion20;
            SaveUserSettingInt("Gimmera Version", gimmeraVersion);
            DataLinkConnected = true;
            //radioButtonVersion10.setEnabled(false);
        } else if (connectionStatus.contains(getString(R.string.disconnected))) {
            gimmeraVersion = R.id.radioButtonVersion10;
            SaveUserSettingInt("Gimmera Version", gimmeraVersion);
            DataLinkConnected = false;
        } else {
            gimmeraVersion = settings.getInt("Gimmera Version", R.id.radioButtonVersion10);
            DataLinkConnected = false;
        }
    }

    static void SetGimeraVersion_RealTimeUpdate(boolean connected) {
        if (radiogroupGimmeraVersion != null) { // must check this because sometimes, SettingsFragment has not been instantiated
            if (connected) {
                gimmeraVersion = R.id.radioButtonVersion20;
                DataLinkConnected = true;
            } else {
                gimmeraVersion = R.id.radioButtonVersion10;
                DataLinkConnected = false;
            }
            radiogroupGimmeraVersion.check(gimmeraVersion);
        }
    }

    @SuppressLint("SetTextI18n")
    private void RestoreUserSettings() {
        // Restore preferences
        SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        PanSmoothness = settings.getInt("PanSmoothness", 50);
        TiltSmoothness = settings.getInt("TiltSmoothness", 50);
        AspectRatioID = settings.getInt("Aspect Ratio", R.id.radioButtonFullScreen);
        SwitchVid2PicChecked = settings.getBoolean("SwitchVid2Pic", false);
        pantiltType = settings.getInt("PanTiltConnType", R.id.radioButtonSerial);
        SetGimeraVersion_RealTimeUpdate(MainActivity.ConnectionStatus);
        intMapTransparencySettingFrag = settings.getInt("Map transparency", 100);
        HorizontalGridNo = settings.getInt("Horizontal Grids", 6) + 2;
        seekBarHorGrid.setProgress(HorizontalGridNo - 2);
        VerticalGridNo = settings.getInt("Vertical Grids", 4) + 2;
        seekBarVerGrid.setProgress(VerticalGridNo - 2);
        textViewGridNoH.setText((HorizontalGridNo - 1) + "");
        textViewGridNoV.setText((VerticalGridNo - 1) + "");

        final int screenRecID = settings.getInt("Record Resolution", R.id.radioButton512s);
        final boolean micRec = settings.getBoolean("Record Voice", true);
        final boolean reminder = settings.getBoolean("Voice Reminder", true);

        //radioGroupAspectRatio.check(-1);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                radioGroupAspectRatio.check(AspectRatioID);
                switchVidtoPic.setChecked(SwitchVid2PicChecked);
                radiogroupPanTiltType.check(pantiltType);
                radiogroupGimmeraVersion.check(gimmeraVersion);
                FullScreenVideoActivity.intMapTransparency = intMapTransparencySettingFrag;
                FullScreenVideoActivity.seekBarMapTransparency.setProgress(intMapTransparencySettingFrag);
            }
        }, 100);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                screenrecresgroup.check(screenRecID);
                toggleButtonMicRec.setChecked(micRec);
                voiceReminderToggle.setChecked(reminder);
            }
        }, 1000);

        seekBarPanSmoothness.setProgress(PanSmoothness);
        seekBarTiltSmoothness.setProgress(TiltSmoothness);
        //Todo: update pan and tilt smoothness has a bug after reading again saved settings
        ControlsFragment.UpdatePanSmoothness(PanSmoothness);
        ControlsFragment.UpdateTiltSmoothness(TiltSmoothness);
        Log.i(TAG, "onCreateView: PanSmoothness = " + PanSmoothness + " TiltSmoothness = " + TiltSmoothness + " AspectRatioID = " + AspectRatioID);
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
    public void onStop() {
        try {
            SharedPreferences settings = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("PanSmoothness", PanSmoothness);
            editor.putInt("TiltSmoothness", TiltSmoothness);
            editor.putInt("Aspect Ratio", AspectRatioID);
            editor.putBoolean("SwitchVid2Pic", SwitchVid2PicChecked);
            editor.putInt("PanTiltConnType", pantiltType);
            editor.putInt("Gimmera Version", gimmeraVersion);
            editor.putInt("Map transparency", intMapTransparencySettingFrag);
            editor.putInt("Horizontal Grids", HorizontalGridNo - 2);
            editor.putInt("Vertical Grids", VerticalGridNo - 2);
            editor.putInt("Record Resolution", screenrecresgroup.getCheckedRadioButtonId());
            editor.putBoolean("Record Voice", toggleButtonMicRec.isChecked());
            editor.putBoolean("Voice Reminder", voiceReminderToggle.isChecked());

            // Commit the edits!
            editor.apply();
        } catch (Exception ignored) {
        }

        super.onStop();
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
}
