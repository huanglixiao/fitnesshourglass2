package com.fitnesshourglass2.android.Countdown;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fitnesshourglass2.android.R;
import com.fitnesshourglass2.android.TimeSetted;

public class CountdownFragment extends Fragment {

    private TextView remainTimeText;

    private String minPartString;

    private String secondPartString;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.part_remain_time,container,false);
        remainTimeText = (TextView) view.findViewById(R.id.text_remain_time);
        return view;
    }

    public void UpdateRemainTime(long remainTime){
        int min = (int) ( remainTime/ TimeSetted.SECOND_TO_MILL /60 );
        int second = (int) ((remainTime/TimeSetted.SECOND_TO_MILL) % 60);
        if (min < 10){
            minPartString = "0" + min;
        }else {
            minPartString = "" + min;
        }
        if (second < 10){
            secondPartString = "0" + second;
        }else {
            secondPartString = "" + second;
        }
        remainTimeText.setText(minPartString + ":" + secondPartString);
    }
}
