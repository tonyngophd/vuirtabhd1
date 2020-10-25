package com.suas.vuirtab1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

//import android.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Videofrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Videofrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Videofrag extends Fragment {
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.fragment_videofrag, container, false);
        return r;
    };
}