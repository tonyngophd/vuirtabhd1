package com.suas.vuirtab1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import static android.graphics.Color.argb;
import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QuickInstructionsFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuickInstructionsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuickInstructionsFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ToggleButton toggleButtonAnimate;
    private TextView[] textViews = new TextView[23];
    private String[] speechtext = new String[22];
    private boolean CanAnimate = true;
    private ConstraintLayout pantiltzoomdetectionboxintro;
    private Animation animBounce;
    private int TextNoBeingAnimated = 0, AnimationCycleNo = 0;
    long AnimationDuration = 500;

    public QuickInstructionsFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuickInstructionsFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static QuickInstructionsFrag newInstance(String param1, String param2) {
        QuickInstructionsFrag fragment = new QuickInstructionsFrag();
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
        View view = inflater.inflate(R.layout.fragment_quick_instructions, container, false);

        toggleButtonAnimate = (ToggleButton) view.findViewById(R.id.toggleButtonAnimate);
        pantiltzoomdetectionboxintro = (ConstraintLayout) view.findViewById(R.id.pantiltzoomdetectionboxintro);
        AsignTextViews(view);
        AnimationDuration = 1000;

        //Todo: need to consider when the fragment is being hidden when the intro/speaking is still going on, should we need to stop everything?
        //Todo: is it still running in the background/consuming the resources in the background when hidden?

        toggleButtonAnimate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                CanAnimate = isChecked;
                if (isChecked) {
                    //Animate(textViews[0], 500, 0);
                    //TextNoBeingAnimated--;
                    //if(TextNoBeingAnimated < 0) TextNoBeingAnimated = 0;
                    AnimateAllTexts();
                } else {
                    ResetColorAllViews();
                    if (textViews[TextNoBeingAnimated].getAnimation() != null) {
                        Log.i(TAG, "onCheckedChanged: textViews[TextNoBeingAnimated].getAnimation().hasEnded() = " + textViews[TextNoBeingAnimated].getAnimation().hasEnded());
                        if (!textViews[TextNoBeingAnimated].getAnimation().hasEnded()) {
                            textViews[TextNoBeingAnimated].getAnimation().cancel();
                            textViews[TextNoBeingAnimated].clearAnimation();
                        }
                    }
                }
            }
        });

        return view;
    }

    private void AsignTextViews(View view) {
        textViews[0] = (TextView) view.findViewById(R.id.fabtext);
        speechtext[0] = "Voice commands. Press and tell me to do a few things hand-free";
        textViews[1] = (TextView) view.findViewById(R.id.imageViewRecordScreentext);
        speechtext[1] = "Screen Record button. View them in Thermal Media Gallery tab";
        textViews[2] = (TextView) view.findViewById(R.id.imageViewRecordSnaptext);
        speechtext[2] = "Take thermal pictures from screen. View them in Thermal Media Gallery tab as well";

        textViews[3] = (TextView) view.findViewById(R.id.imageViewRecordtext);
        speechtext[3] = "Gimmera Record button. Press along to see effects";
        textViews[4] = (TextView) view.findViewById(R.id.imageViewPalette3text);
        speechtext[4] = "Palette number 3. Normally Ironbow";
        textViews[5] = (TextView) view.findViewById(R.id.imageViewPalette2text);
        speechtext[5] = "Palette number 2. Normally Black hot";
        textViews[6] = (TextView) view.findViewById(R.id.imageViewPalette1text);
        speechtext[6] = "Palette number 1. Normally White hot";
        textViews[7] = (TextView) view.findViewById(R.id.imageViewFFCtext);
        speechtext[7] = "FFC or camera non-uniform correction";
        textViews[8] = (TextView) view.findViewById(R.id.imageViewGestureModetext);
        speechtext[8] = "Move gesture button";
        textViews[9] = (TextView) view.findViewById(R.id.panseekBartext);
        speechtext[9] = "Pan control bar";
        textViews[10] = (TextView) view.findViewById(R.id.textViewMapintro);
        speechtext[10] = "Google Maps view";
        textViews[11] = (TextView) view.findViewById(R.id.imageViewMapOnOFFtext);
        speechtext[11] = "Quick show or hide map button";

        textViews[12] = (TextView) view.findViewById(R.id.imageViewGridOnOFFtext);
        speechtext[12] = "Grid view button. Press along to see effects";
        textViews[13] = (TextView) view.findViewById(R.id.imageViewSettingFullScreentext);
        speechtext[13] = "Settings button";
        textViews[14] = (TextView) view.findViewById(R.id.imageViewHelpFullScreentext);
        speechtext[14] = "Full instructions button";
        textViews[15] = (TextView) view.findViewById(R.id.imageViewQuickhelptext);
        speechtext[15] = "Quick help button";
        textViews[16] = (TextView) view.findViewById(R.id.imageViewGoHometext);
        speechtext[16] = "Return to App home screen. But do NOT click it now";


        textViews[17] = (TextView) view.findViewById(R.id.textViewstatusbarintro);
        speechtext[17] = "Status bar. Double tap to show or hide";
        textViews[18] = (TextView) view.findViewById(R.id.textViewsettingswipeshow);
        speechtext[18] = "Swipe to show";
        textViews[19] = (TextView) view.findViewById(R.id.textViewsettingswipehide);
        speechtext[19] = "Swipe to hide";
        textViews[20] = (TextView) view.findViewById(R.id.tiltseekbartext);
        speechtext[20] = "Tilt control bar";
        textViews[21] = (TextView) view.findViewById(R.id.textViewPTZintro);
        speechtext[21] = "Pan, Tilt and Zoom control area";
        textViews[22] = (TextView) view.findViewById(R.id.imageViewGestureModetextmore);
        ResetColorAllViews();
    }

    private void ResetColorAllViews() {
        for (int i = 0; i < 23; i++) {
            textViews[i].setBackgroundColor(argb(200, 99, 255, 255));
            textViews[i].setTextColor(argb(255, 0, 0, 0));
        }
    }

    private void HighLightOneTextView(int i) {
        if (i == 21) {
            pantiltzoomdetectionboxintro.setBackgroundColor(argb(128, 190, 190, 190));
        } else {
            pantiltzoomdetectionboxintro.setBackgroundColor(argb(0, 190, 190, 190));
        }
        if (i == 8) {
            textViews[22].setBackgroundColor(argb(200, 99, 255, 255));
            textViews[22].setTextColor(argb(255, 0, 0, 0));
        } else {
            textViews[22].setBackgroundColor(argb(0, 255, 255, 255));
            textViews[22].setTextColor(argb(0, 255, 255, 255));
        }
        textViews[i].setBackgroundColor(argb(200, 99, 255, 255));
        textViews[i].setTextColor(argb(255, 0, 0, 0));
        for (int j = 0; j < 22; j++) {
            if (j != i) {
                textViews[j].setBackgroundColor(argb(0, 255, 255, 255));
                textViews[j].setTextColor(argb(0, 255, 255, 255));
            }
        }
    }

    private boolean Animate(View view, final long duration, int direction) {
        int ID = R.anim.bouncex;
        switch (direction) {
            case 0:
                ID = R.anim.bouncex;
                break;
            case 1:
                ID = R.anim.bouncexr;
                break;
            case 2:
                ID = R.anim.bouncey;
                break;
            case 3:
                ID = R.anim.bounceyr;
                break;
        }
        Animation animBounce = AnimationUtils.loadAnimation(getActivity(), ID);
        animBounce.setDuration(duration);
        view.startAnimation(animBounce);
        animBounce.setAnimationListener(new Animation.AnimationListener() {
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
        return false;
    }

    private void AnimateAllTexts() {
        if (TextNoBeingAnimated > 21) {
            AnimationDuration /= 2;
            TextNoBeingAnimated = 0;
            AnimationCycleNo++;
            Log.i(TAG, "AnimateAllTexts: AnimationCycleNo = " + AnimationCycleNo);
            if (AnimationCycleNo == 1) {
                MainActivity.speak("Instruction cycle repeats 2nd time, a bit faster");
            } else if (AnimationCycleNo == 2) {
                MainActivity.speak("Instruction cycle repeats final time, very fast.");
            } else if (AnimationCycleNo > 2) { // letting it repeat 3 times
                MainActivity.speak("Press question mark button to hide, or play intro to repeat");
                toggleButtonAnimate.setChecked(false);
                CanAnimate = false;
                AnimationCycleNo = 0;
                AnimationDuration = 1000;
                ResetColorAllViews();
                return;
            }
        }
        int direction;
        final int i = TextNoBeingAnimated;

        if (CanAnimate) {
            HighLightOneTextView(i);
            if (i < 10) {
                direction = 0;
            } else if (i < 17) {
                direction = 1;
            } else if (i < 18) {
                direction = 2;
            } else if (i < 19) {
                direction = 1;
            } else if (i < 20) {
                direction = 3;
            } else {
                direction = 3;
            }

            int ID = R.anim.bouncex;
            switch (direction) {
                case 0:
                    ID = R.anim.bouncey;
                    break;
                case 1:
                    ID = R.anim.bouncexr;
                    break;
                case 2:
                    ID = R.anim.bounceyr;
                    break;
                case 3:
                    ID = R.anim.bouncex;
                    break;
            }
            animBounce = AnimationUtils.loadAnimation(getActivity(), ID);
            animBounce.setDuration(AnimationDuration);
            textViews[i].startAnimation(animBounce);
            if (AnimationCycleNo == 0) {
                MainActivity.speak(speechtext[i]);
            }
            animBounce.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TextNoBeingAnimated++;
                            AnimateAllTexts();
                        }
                    }, 500);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
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
        try {
            if (context instanceof OnFragmentInteractionListener) {
                mListener = (OnFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
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
