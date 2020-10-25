package com.suas.vuirtab1;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MediaGalleryActivity extends AppCompatActivity {

    static Window window;
    RecyclerView recyclerView;
    ImageView imageView;
    VideoView videoView;
    private final int maxpics = 99;
    protected static int startindex = 0;
    private int endindex = maxpics;
    ArrayList<CreateList> createLists;
    ArrayList<CreateList> createListsFolder;
    MediaFileAdapter adapter;
    static FolderAdapter adapterFolder;
    static String TAG = "gallery";
    private long scrollupMillis = System.currentTimeMillis();
    private long scrolldownMillis = System.currentTimeMillis();
    private static RecyclerView recyclerViewFolder;
    protected static String folderClicked = "";
    RecyclerView.LayoutManager layoutManager;
    protected static RadioGroup radioGroup;
    private RelativeLayout imagelayout;
    private ScaleGestureDetector mScaleGestureDetector;
    private TextView textviewNoContent;
    private TextView textviewMediaName;
    private TextView textviewLoading;
    private ProgressBar progressBarloading;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        HideAndroidBottomNavigationBarforTrueFullScreenView();
        setContentView(R.layout.activity_media_gallery);

        recyclerView = (RecyclerView) findViewById(R.id.mediagallery);
        recyclerViewFolder = (RecyclerView) findViewById(R.id.mediafolder);
        recyclerView.setHasFixedSize(true);
        recyclerViewFolder.setHasFixedSize(true);
        imageView = (ImageView) findViewById(R.id.imageviewbigger);
        videoView = (VideoView) findViewById(R.id.videoviewbigger);
        radioGroup = (RadioGroup) findViewById(R.id.rg1);
        imagelayout = (RelativeLayout) findViewById(R.id.imagelayout);
        textviewNoContent = (TextView) findViewById(R.id.textviewNoContent);
        textviewMediaName = (TextView) findViewById(R.id.textviewMediaName);
        textviewLoading = (TextView) findViewById(R.id.textviewLoading);
        progressBarloading = (ProgressBar) findViewById(R.id.progressBarloading);

        int spanCount = 4;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels > 1080) spanCount = 6;
        layoutManager = new GridLayoutManager(this, spanCount);
        recyclerViewFolder.setLayoutManager(layoutManager);

        createListsFolder = prepareDataFolder();
        adapterFolder = new FolderAdapter(getApplicationContext(), createListsFolder);
        adapterFolder.setHasStableIds(true);
        recyclerViewFolder.setAdapter(adapterFolder);
        Log.i(TAG, "onCreate: resetFolderIcons adapterFolder.hasStableIds = " + adapterFolder.hasStableIds());
        //adapterFolder.bindViewHolder(adapterFolder.getViewHolder(), 1);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                openFolderforImageView();
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(window.getContext(), new MediaScaleListener());
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    private float mScaleFactor = 1.0f, mScaleFactor_pre = 1.0f;

    private class MediaScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor_pre = mScaleFactor;
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 8.0f));
            //Log.i(TAG, "onScale: mScaleFactor = " + mScaleFactor);
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            imageView.setPivotX(scaleGestureDetector.getFocusX());
            imageView.setPivotY(scaleGestureDetector.getFocusY());
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

    private void openFolderforImageView() {
        layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        String folder = adapterFolder.getfolderClicked();
        Log.i(TAG, "openFolderforImageView: folder = " + folder);
        createLists = prepareData(folder);
        adapter = new MediaFileAdapter(getApplicationContext(), createLists, imageView, textviewMediaName, videoView);
        recyclerView.setAdapter(adapter);

        if (!recyclerView.hasOnClickListeners()) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    //Log.i("gallery", "onScrollStateChanged: newState = " + newState);
                    //https://stackoverflow.com/questions/36127734/detect-when-recyclerview-reaches-the-bottom-most-position-while-scrolling
                    //direction integers: -1 for up, 1 for down, 0 will always return false
                    //TODO make this scroll much smoother without having to load too many images at once
                    if (!recyclerView.canScrollVertically(1)) {
                        scrolldownMillis = System.currentTimeMillis();
                        if (System.currentTimeMillis() - scrollupMillis > 500) {
                            endindex += 3;
                            if (endindex > maxpics)
                                startindex += 3;
                            createLists = prepareData(folderClicked);
                            adapter = new MediaFileAdapter(getApplicationContext(), createLists, imageView, textviewMediaName, videoView);
                            recyclerView.setAdapter(adapter);
                            recyclerView.scrollToPosition(endindex);
                            Log.i("gallery", "onScrollStateChanged: can't move Down further start = " + startindex + " end = " + endindex);
                            Log.i(TAG, "onScrollStateChanged: size = " + createLists.size());
                        }
                    } else if (!recyclerView.canScrollVertically(-1)) {
                        scrollupMillis = System.currentTimeMillis();
                        if (System.currentTimeMillis() - scrolldownMillis > 500) {
                            startindex -= 3;
                            if (startindex > 0)
                                endindex -= 3;
                            createLists = prepareData(folderClicked);
                            adapter = new MediaFileAdapter(getApplicationContext(), createLists, imageView, textviewMediaName, videoView);
                            recyclerView.setAdapter(adapter);
                            recyclerView.scrollToPosition(startindex);
                            Log.i("gallery", "onScrollStateChanged: can't move UP further start = " + startindex + " end = " + endindex);
                        }
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //Log.i("", "onScrolled: dx = " + dx + " dy = " + dy);
                }
            });
        }
    }

    protected static int checked = 1;

    protected static void resetFolderIcons(int exludeindex) {
        if (checked == 0) {
            checked = 1;
            radioGroup.check(R.id.rb1);
        } else {
            checked = 0;
            radioGroup.check(R.id.rb0);
        }
        int itemCount = Objects.requireNonNull(recyclerViewFolder.getAdapter()).getItemCount();
        Log.i(TAG, "resetFolderIcons: itemCount = " + itemCount);
        for (int i = 0; i < itemCount; i++) {
            if (i != exludeindex) {
                FolderAdapter.ViewHolder viewHolder = (FolderAdapter.ViewHolder) recyclerViewFolder.findViewHolderForPosition(i);
                Log.i(TAG, "resetFolderIcons: i = " + i + " viewHolder = " + viewHolder);
                if (viewHolder != null) {
                    viewHolder.img.setImageResource(R.drawable.ic_folder_black_48dp);
                }
            }
        }
    }

    static void HideAndroidBottomNavigationBarforTrueFullScreenView() {
        //https://stackoverflow.com/questions/16713845/permanently-hide-navigation-bar-in-an-activity/26013850
        if (window != null) {
            View decorView = window.getDecorView();
            /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private ArrayList<CreateList> prepareData(String folder) {

        ArrayList<CreateList> theimage = new ArrayList<>();

        String path = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            path = Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media/" + folder;//Environment.getRootDirectory().toString();
        } else {
            path = getExternalFilesDir(null) + "/sUAS.com/VuIR_Media/" + folder;
        }
        Log.i(TAG, "prepareData: path = " + path);
        File f = new File(path);
        File[] file = f.listFiles();
        if (file != null) {
            if (file.length < endindex) endindex = file.length;
            if (file.length < startindex) startindex = file.length;
            if (startindex >= endindex) startindex = endindex - 3;
            if (startindex < 0) startindex = 0;
            for (int i = 0; i < file.length; i++) {
                if (file[i].getName().contains("jpg")) {
                    if (i >= startindex && i < endindex) {
                        CreateList createList = new CreateList();
                        createList.setImage_title(file[i].getName());
                        createList.setImage_Location(path + "/" + file[i].getName());

                        theimage.add(createList);
                    }
                }
            }
        }

        return theimage;
    }

    private ArrayList<CreateList> prepareDataFolder() {

        ArrayList<CreateList> theimage = new ArrayList<>();
        //https://stackoverflow.com/questions/12780446/check-if-a-path-represents-a-file-or-a-folder
        String path = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            path = Environment.getExternalStorageDirectory() + "/sUAS.com/VuIR_Media";//Environment.getRootDirectory().toString();
        } else {
            path = getExternalFilesDir(null) + "/sUAS.com/VuIR_Media";
        }
        File f = new File(path);
        File[] file = f.listFiles();
        boolean foundNocontent = true;
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                if (file[i].isDirectory()) {
                    CreateList createList = new CreateList();
                    createList.setImage_title(file[i].getName());
                    //createList.setImage_Location(path + "/" + file[i].getName());
                    theimage.add(createList);
                    foundNocontent = false;
                }
            }
        }
        if (foundNocontent) textviewNoContent.setVisibility(View.VISIBLE);
        else textviewNoContent.setVisibility(View.GONE);
        return theimage;
    }
}
