package com.suas.vuirtab1;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import static com.suas.vuirtab1.MainActivity.cardViewvideogroup;
import static com.suas.vuirtab1.VideoWindow.Horizontal.H;
import static com.suas.vuirtab1.VideoWindow.Horizontal.W;
import static com.suas.vuirtab1.VideoWindow.Horizontal.surfaceView;
//import androidx.fragment.app.Fragment;

public class VideoWindow {
    private static final String HWTOKEN = "VideoWindow_h";
    private static final String TAG = VideoWindow.class.getName();
    public static final String VIDEOWINDOW_H = "videowindow_h";
    static VideoFragmentMgr _ghVideoFragmentMgr = new VideoFragmentMgr(HWTOKEN);
    private static String mServerIp = "192.168.2.220";
    private static final Object mSync = new Object();
    private static int mType;
    private static VideoFragmentMgr mWorkFragmgr;

    @SuppressWarnings("deprecation")
    public static class Horizontal extends Fragment {
        private boolean mIntenthassend = false;
        static SurfaceView surfaceView;
        static SurfaceHolder surfaceHolder;
        private Surface surface;
        static LinearLayout llvideopreview;
        static int W = 0, H = 0;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View r = inflater.inflate(R.layout.video_layout_horizontal, container, false);
            surfaceView = (SurfaceView) r.findViewById(R.id.videoViewPreview);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(VideoWindow._ghVideoFragmentMgr);
            llvideopreview = (LinearLayout) r.findViewById(R.id.llvideopreview);
            CardView vg = r.findViewById(R.id.cardviewvideogroup);
            if (cardViewvideogroup != null) {
                cardViewvideogroup.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (Horizontal.this.mIntenthassend) {
                            //Log.d(VideoWindow.TAG, "you have touched");
                            return;
                        }
                        //Log.d(VideoWindow.TAG, "will launch fullscreen");
                        Horizontal.this.mIntenthassend = true;
                        try {
                            ServiceBase.getServiceBase().getVideoService().startLink(VideoWindow.mServerIp, 0);
                        } catch (Exception e) {
                            Log.d(VideoWindow.TAG, "get exception " + e.toString());
                        }
                        //Log.d(VideoWindow.TAG, "SEND INTENT TO LAUNCH FULLSCREEN in video_h");
                        Intent intent = new Intent(getActivity(), FullScreenVideoActivity.class);
                        intent.putExtra("type", VideoWindow.mType);
                        intent.putExtra("ip", VideoWindow.mServerIp);
                        Horizontal.this.startActivity(intent);
                        //startActivity(intent);
                    }
                });
            }
            return r;
        }

        public void onStop() {
            super.onStop();
            this.mIntenthassend = false;
        }
        static void resizeVideoSurface(boolean hide){
            if(surfaceView == null) return;
            if(hide){
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 32);
                surfaceView.setLayoutParams(params);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(W, H);
                surfaceView.setLayoutParams(params);
            }
            Log.i(TAG, "resizeVideoSurface: hide = " + hide + " layout = " + surfaceView.getLayoutParams().height);
        }
    }



    public static class VideoFragmentMgr implements Callback {
        private Surface mSf;
        private int mSurface_h;
        private int mSurface_w;
        private String mToken;

        void StartVideo(String ip, int type) {
            try {
                Log.d(VideoWindow.TAG + " startvideo", "StartVideo(String ip, int type)");
                ServiceBase.getServiceBase().getVideoService().Pause(this.mToken);
                ServiceBase.getServiceBase().getVideoService().startLink(ip, type);
                Log.d("StartVideo", "this.mSurface_w = " + this.mSurface_w);
                Log.d("StartVideo", "this.mSurface_h = " + this.mSurface_h);
                ServiceBase.getServiceBase().getVideoService().Resume(this.mSurface_w, this.mSurface_h, 2, this.mToken, this.mSf);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(VideoWindow.TAG, "Got exception in VideoFragmentMgr::StartVideo " + e.toString());
            }
        }
        void ResumeVideo(String ip, int type) {
            try {
                ServiceBase.getServiceBase().getVideoService().Resume(this.mSurface_w, this.mSurface_h, 2, this.mToken, this.mSf);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(VideoWindow.TAG, "Got exception in VideoFragmentMgr::StartVideo " + e.toString());
            }
        }

        VideoFragmentMgr(String token) {
            this.mToken = token;
        }

        public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
            Log.d(VideoWindow.TAG, String.format("surfaceChanged %d %d %d", format, width, height));
            this.mSurface_w = width;
            this.mSurface_h = height;
            this.mSf = holder.getSurface();
            //StartVideo(MainActivity.mserverip, 0);
            synchronized (VideoWindow.mSync) {
                VideoWindow.mWorkFragmgr = this;
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            if(W == 0 || H == 0){
                W = Horizontal.surfaceView.getWidth();
                H = Horizontal.surfaceView.getHeight();
                Log.i(TAG, "resizeVideoSurface: W = " + W + " H = " + H);
            }
            /*this.mSf = holder.getSurface();
            this.mSurface_w = surfaceView.getWidth();
            this.mSurface_h = surfaceView.getHeight();
            synchronized (VideoWindow.mSync) {
                VideoWindow.mWorkFragmgr = this;
            }
            Log.d(VideoWindow.TAG, String.format("surfaceCreated %d %d", this.mSurface_w, this.mSurface_h));*/
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e(VideoWindow.TAG, "surfaceDestroyed");
            try {
                synchronized (VideoWindow.mSync) {
                    VideoWindow.mWorkFragmgr = null;
                    this.mSf = null;
                }
                ServiceBase.getServiceBase().getVideoService().Pause(this.mToken);
            } catch (Exception e) {
                Log.d(VideoWindow.TAG, "pause got exception " + e.toString());
            }
        }
    }

    static void StartVideo(String sip, int link) {
        synchronized (mSync) {
            if (mWorkFragmgr == null) {
                Log.e(TAG, "target window surface is not created yet. addCallback");
                return;
            }
            mType = link;
            mServerIp = sip;
            mWorkFragmgr.StartVideo(sip, link);
        }
    }
    static void ResumeVideo(String sip, int link) {
        synchronized (mSync) {
            if (mWorkFragmgr == null) {
                Log.e(TAG, "target window surface is not created yet. addCallback");
                return;
            }
            mType = link;
            mServerIp = sip;
            mWorkFragmgr.ResumeVideo(sip, link);
        }
    }

}