package com.suas.vuirtab1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatusBarFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatusBarFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusBarFrag<onResume> extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static TextView textViewConnectionFull;
    private static TextView batterypercentagetextView;
    private static TextView sattelitesNotextView;
    private static ImageView imageViewBattery;
    private static ImageView imageViewRecStatus;
    private static ImageView imageViewSat;
    private static ImageView imageViewGeotagging;
    private static ImageView imageViewTabBattStatus;
    private static TextView geotaggingNotextView;
    private static TextView textViewResolution;
    private static TextView textViewGesmode;
    private static TextView textViewZoom;
    private static TextView textViewdatetime;
    private static TextView tabbatterypercentagetextView;
    private static TextView cputemptextView;
    private static int RECstatus = 0, recblink = 0;
    private static long recbuttonmillis;
    private static ProgressBar progressbarlongbatterypercentage;
    Handler updateConversationHandler;

    public StatusBarFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatusBarFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static StatusBarFrag newInstance(String param1, String param2) {
        StatusBarFrag fragment = new StatusBarFrag();
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
        // Inflate the layout for this fragment
        View r = inflater.inflate(R.layout.fragment_status_bar, container, false);
        textViewConnectionFull = (TextView) r.findViewById(R.id.textViewConnectionFull);
        batterypercentagetextView = (TextView) r.findViewById(R.id.batterypercentagetextView);
        sattelitesNotextView = (TextView) r.findViewById(R.id.sattelitesNotextView);
        imageViewSat = (ImageView) r.findViewById(R.id.imageViewSat);
        imageViewBattery = (ImageView) r.findViewById(R.id.imageViewBattery);
        ImageView imageViewSettingStatusBar = (ImageView) r.findViewById(R.id.imageViewSettingStatusBar);
        geotaggingNotextView = (TextView) r.findViewById(R.id.geotaggingNotextView);
        imageViewGeotagging = (ImageView) r.findViewById(R.id.imageViewGeotagging);
        textViewResolution = (TextView) r.findViewById(R.id.textViewResolution);
        textViewGesmode = (TextView) r.findViewById(R.id.textViewGesmode);
        imageViewRecStatus = (ImageView) r.findViewById(R.id.imageViewRecStatus);
        imageViewTabBattStatus = (ImageView) r.findViewById(R.id.imageViewTabBattStatus);
        textViewZoom = (TextView) r.findViewById(R.id.textViewZoom);
        textViewdatetime = (TextView) r.findViewById(R.id.textViewdatetime);
        tabbatterypercentagetextView = (TextView) r.findViewById(R.id.tabbatterypercentagetextView);
        cputemptextView = (TextView) r.findViewById(R.id.cputemptextView);
        imageViewRecStatus.setColorFilter(Color.GRAY);
        progressbarlongbatterypercentage = (ProgressBar) r.findViewById(R.id.progressbarlongbatterypercentage);

        Log.d(TAG, "onCreateView: textViewConnectionFull = " + textViewConnectionFull);
        recbuttonmillis = System.currentTimeMillis();

        imageViewSettingStatusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FullScreenVideoActivity.settingsFragment != null) {
                    if (FullScreenVideoActivity.settingsFragment.isHidden()) {
                        FullScreenVideoActivity.fragmentTransactionx = FullScreenVideoActivity.fragmentManager.beginTransaction();
                        FullScreenVideoActivity.fragmentTransactionx.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                                .show(FullScreenVideoActivity.settingsFragment).commit();
                    }
                }
            }
        });

        Objects.requireNonNull(getActivity()).registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Objects.requireNonNull(getActivity()).registerReceiver(m_timeChangedReceiver, s_intentFilter);
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("HH:mma MMM dd");
        String date = df.format(Calendar.getInstance().getTime());
        textViewdatetime.setText(date);

        updateConversationHandler = new Handler();

        return r;
    }

    //https://stackoverflow.com/questions/3291655/get-battery-level-and-state-in-android
    private int TabletBatteryLevel = 0, TabletBateryPlugged = 0;
    private static IntentFilter s_intentFilter;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            TabletBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            TabletBateryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            if (TabletBatteryLevel > 0) {
                tabbatterypercentagetextView.setText(TabletBatteryLevel + "%");
                imageViewTabBattStatus.setImageResource(R.drawable.ic_battery_std_black_24dp);
            }
            if (TabletBateryPlugged > 0) {
                imageViewTabBattStatus.setImageResource(R.drawable.ic_battery_charging_90_black_24dp);
            }
        }
    };

    static {
        s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    //https://stackoverflow.com/questions/5481386/date-and-time-change-listener-in-android
    private final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK)) {
                //https://stackoverflow.com/questions/5369682/get-current-time-and-date-on-android
                @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("HH:mma MMM dd");
                String date = df.format(Calendar.getInstance().getTime());
                Log.i(TAG, "onReceive: currentTime = " + date);
                textViewdatetime.setText(date);
            }
        }
    };
    private BroadcastReceiver mTimeInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
        }
    };

    public static void BlinkRecStatus(final int RECin) {
        RECstatus = RECin;
        final Handler handlerUI = new Handler();
        final int DarkRed = Color.argb(99, 255, 0, 0);
        if (android.os.Build.VERSION.SDK_INT < 23) { //Build.VERSION_CODES.P = 28
            if (RECstatus == 1) {
                imageViewRecStatus.setColorFilter(DarkRed);
            } else {
                imageViewRecStatus.setColorFilter(Color.GRAY);
            }
        } else {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (RECstatus == 1) {
                        handlerUI.post(new Runnable() {
                            @Override
                            public void run() {
                                if (recblink == 0) {
                                    recblink = 1;
                                    imageViewRecStatus.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                                    imageViewRecStatus.setColorFilter(DarkRed);
                                } else {
                                    recblink = 0;
                                    imageViewRecStatus.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                                    imageViewRecStatus.setColorFilter(Color.GRAY);
                                }
                            }
                        });
                    } else {
                        timer.cancel();
                        timer.purge();
                    }
                }
            }, 0, 1000);
        }
    }

    @SuppressLint("SetTextI18n")
    public static void UpdateStatusBar() {
        if (textViewConnectionFull != null) {
            textViewConnectionFull.setText(MainActivity.ConnectionStatus);
        }
        if (batterypercentagetextView != null) {
            String percent = rtBatteryPercentage();
            batterypercentagetextView.setText(percent);
        }
        if (sattelitesNotextView != null) {
            sattelitesNotextView.setText(MainActivity.NumberofSats + "");
        }
        if (cputemptextView != null) {
            if(MainActivity.CPUPercent > 0)
                cputemptextView.setText(MainActivity.CPUTemp + "°C " + MainActivity.CPUPercent + "%");
            else
                cputemptextView.setText(MainActivity.CPUTemp + "°C ");
        }
        if (imageViewSat != null) {
            if (MainActivity.NumberofSats < 1) {
                imageViewSat.setColorFilter(Color.GRAY);
            } else {
                imageViewSat.setColorFilter(Color.argb(190, 0x25, 0x82, 0xCE));//R.color.colorThemeBlue);
            }
        }
        if (geotaggingNotextView != null) {
            CharSequence s;
            if (MainActivity.Geotagging) s = "ON";
            else s = "OFF";
            geotaggingNotextView.setText(s);
        }

        if (imageViewGeotagging != null) {
            if (MainActivity.Geotagging) {
                imageViewGeotagging.setColorFilter(Color.argb(190, 0x25, 0x82, 0xCE));
            } else {
                imageViewGeotagging.setColorFilter(Color.GRAY);
            }
        }
        if (textViewResolution != null) {
            if (MainActivity.returnHeight() >= 720) {
                textViewResolution.setText("Res:" + MainActivity.returnHeight() + "P HD");
            } else {
                textViewResolution.setText("Res:" + MainActivity.returnWidth() + "x" + MainActivity.returnHeight());
            }
        }
        if (textViewGesmode != null) {
            CharSequence s;
            if (ControlsFragment.mGestureModeOn) s = "ON";
            else s = "OFF";
            textViewGesmode.setText(s);
        }

        if ((textViewZoom != null) && (ControlsFragment.textViewZoomScale != null)) {
            textViewZoom.setText("Zoom " + ControlsFragment.textViewZoomScale.getText());
        }
    }

    @SuppressLint("SetTextI18n")
    static void UpdatetextViewZoom() {
        if ((textViewZoom != null) && (ControlsFragment.textViewZoomScale != null)) {
            textViewZoom.setText("Zoom " + ControlsFragment.textViewZoomScale.getText());
        }
    }

    private static String rtBatteryPercentage() {
        float percentage = 0;
        //percentage = 100 * (MainActivity.BatteryVoltagePercent - emptyvoltage) / (fullvoltage - emptyvoltage);
        percentage = MainActivity.BatteryVoltagePercent;
        if (percentage > 100) percentage = 100;
        else if (percentage < 0) percentage = 0;
        if (progressbarlongbatterypercentage != null) {
            progressbarlongbatterypercentage.setProgress((int) percentage);
        }
        if (imageViewBattery != null) {
            if (percentage <= 10f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_0_black_48dp);
                imageViewBattery.setColorFilter(Color.RED);
            } else if (percentage <= 20f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_10_black_48dp);
                imageViewBattery.setColorFilter(Color.RED);
            } else if (percentage <= 30f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_20_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 249, 219, 34));
            } else if (percentage <= 40f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_30_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 164, 204, 68));
            } else if (percentage <= 50f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_40_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 60f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_50_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 70f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_60_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 80f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_70_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 90f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_80_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 100f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_90_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else if (percentage <= 110f) {
                imageViewBattery.setImageResource(R.drawable.ic_battery_full_black_48dp);
                imageViewBattery.setColorFilter(Color.argb(255, 137, 171, 13));
            } else {
                imageViewBattery.setImageResource(R.drawable.ic_battery_unknown_black_48dp);
                imageViewBattery.setColorFilter(Color.GRAY);
            }
        }
        Log.d(TAG, "rtBatteryPercentage: percentage = " + percentage);
        return ((int) (percentage)) + "%";
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: updatetask");
        //new UpdateTask(getActivity(), getView()).execute();
    }

    public void LaunchStatusBarUpdateThread1() {
        new UpdateTask(getActivity(), textViewConnectionFull).execute();
    }

    @SuppressLint("StaticFieldLeak")
    public class UpdateTask extends AsyncTask<Void, Void, Void> {

        private UpdateTask(Activity activity, View r) {
            updateConversationHandler.post(new updateUIThread());
        }

        @SuppressLint({"SetTextI18n", "WrongThread"})
        @Override
        protected Void doInBackground(Void... params) {
            updateConversationHandler.post(new updateUIThread());
            publishProgress();
            return null;
        }

        protected void onProgressUpdate(Void... params) {
            Log.d(TAG, "doInBackground: fdfdfdfdf!");
            updateConversationHandler.post(new updateUIThread());
        }
    }

    //https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    class updateUIThread implements Runnable {
        private String message;

        updateUIThread() {
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            textViewConnectionFull.setText(MainActivity.ConnectionStatus);
            batterypercentagetextView.setText(MainActivity.BatteryVoltagePercent + "");
        }
    }

    private void LaunchStatusBarUpdateThread() {
        new Thread(new Runnable() {
            public void run() {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    public void run() {
                        StatusBarUpdateThread();
                    }
                });
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void StatusBarUpdateThread() {
        Log.d(TAG, "StatusBarUpdateThread: hrere");
        if (textViewConnectionFull != null) {
            textViewConnectionFull.setText(MainActivity.ConnectionStatus);
        }
        batterypercentagetextView.setText(MainActivity.BatteryVoltagePercent + "");
    }
    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Objects.requireNonNull(getActivity()).registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Objects.requireNonNull(getActivity()).registerReceiver(m_timeChangedReceiver, s_intentFilter);
        //LaunchStatusBarUpdateThread();
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
        Objects.requireNonNull(getActivity()).unregisterReceiver(m_timeChangedReceiver);
        Objects.requireNonNull(getActivity()).unregisterReceiver(mBatInfoReceiver);
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
