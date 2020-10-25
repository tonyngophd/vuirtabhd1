package com.suas.vuirtab1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import static com.suas.vuirtab1.FullScreenVideoActivity.mainHeight;
import static com.suas.vuirtab1.FullScreenVideoActivity.mainWidth;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GridOverVideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GridOverVideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GridOverVideoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    static int HorizontalGridNo, VerticalGridNo;
    static PixelGridView pixelGrid;
    private int gridMarginh, gridMarginv, leftgridmargin;
    static boolean leftmarginZero = false;

    public GridOverVideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GridOverVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GridOverVideoFragment newInstance(String param1, String param2) {
        GridOverVideoFragment fragment = new GridOverVideoFragment();
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
        View view = inflater.inflate(R.layout.fragment_grid_over_video, container, false);
        calculateGridMargins();

        pixelGrid = new PixelGridView(getContext());
        if (VerticalGridNo < 2) VerticalGridNo = 2;
        if (HorizontalGridNo < 2) HorizontalGridNo = 2;
        pixelGrid.setNumColumns(VerticalGridNo);
        pixelGrid.setNumRows(HorizontalGridNo);

        final ConstraintLayout gridview = (ConstraintLayout) view.findViewById(R.id.gridview);
        gridview.addView(pixelGrid);
        gridview.setAlpha(0.5f);

        return view;
    }

    private void calculateGridMargins() {
        if (mainWidth != 0) {
            gridMarginh = (int) ((mainWidth / 2560.0) * 60);
        } else {
            gridMarginh = (int) ((mainWidth / 1024.0) * 60);
        }
        if (mainHeight != 0) {
            gridMarginv = (int) ((mainHeight / 1600.0) * 65);
        } else {
            gridMarginv = (int) ((mainHeight / 768.0) * 65);
        }
        leftgridmargin = gridMarginh * 2;
        if (leftmarginZero) {
            leftgridmargin = 0;
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
        } catch (Exception ignored) {
        }
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


    //https://stackoverflow.com/questions/24842550/2d-array-grid-on-drawing-canvas
    public class PixelGridView extends View {
        private int numColumns, numRows;
        private int cellWidth, cellHeight;
        private Paint blackPaint = new Paint();
        private Paint whitePaint = new Paint();
        private boolean[][] cellChecked;

        public PixelGridView(Context context) {
            this(context, null);
        }

        public PixelGridView(Context context, AttributeSet attrs) {
            super(context, attrs);
            blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            blackPaint.setColor(0x90000000);
            whitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            whitePaint.setColor(0x90FFFFFF);
        }

        public void setNumColumns(int numColumns) {
            this.numColumns = numColumns;
            calculateDimensions();
        }

        public int getNumColumns() {
            return numColumns;
        }

        public void setNumRows(int numRows) {
            this.numRows = numRows;
            calculateDimensions();
        }

        public int getNumRows() {
            return numRows;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            calculateDimensions();
            calculateGridMargins();
        }

        private void calculateDimensions() {
            if (numColumns < 1 || numRows < 1) {
                return;
            }

            cellWidth = getWidth() / numColumns;
            cellHeight = getHeight() / numRows;

            cellChecked = new boolean[numColumns][numRows];

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //canvas.drawColor(Color.WHITE);

            if (numColumns == 0 || numRows == 0) {
                return;
            }

            int width = getWidth();
            int height = getHeight();

            /*for (int i = 0; i < numColumns; i++) {
                for (int j = 0; j < numRows; j++) {
                    if (cellChecked[i][j]) {
                        canvas.drawRect(i * cellWidth, j * cellHeight,
                                (i + 1) * cellWidth, (j + 1) * cellHeight,
                                blackPaint);
                    }
                }
            }*/

            for (int i = 1; i < numColumns; i++) {
                canvas.drawLine(i * cellWidth, 0 + gridMarginv, i * cellWidth, height - gridMarginh, whitePaint);
            }
            for (int i = 1; i < numColumns; i++) {
                canvas.drawLine(i * cellWidth + 1, 0 + gridMarginv, i * cellWidth + 1, height - gridMarginh, blackPaint);
            }

            for (int i = 1; i < numRows; i++) {
                canvas.drawLine(leftgridmargin, i * cellHeight, width - gridMarginh, i * cellHeight, whitePaint);
            }
            for (int i = 1; i < numRows; i++) {
                canvas.drawLine(leftgridmargin, i * cellHeight + 1, width - gridMarginh, i * cellHeight + 1, blackPaint);
            }
        }
    }
}
