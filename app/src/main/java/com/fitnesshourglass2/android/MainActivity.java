package com.fitnesshourglass2.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fitnesshourglass2.android.Countdown.CountdownFragment;
import com.fitnesshourglass2.android.Countdown.CountdownService;
import com.fitnesshourglass2.android.SetTime.SetTimeFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton;

    private Button returnButton;

    private Button setButton;

    private TextView groupNumText;

    private SetTimeFragment setTimeFragment;

    private CountdownFragment countdownFragment;

    private CountdownService countdownService;

    public static final int UPDATE_REMAIN_TIME = 1;//更改剩余时间

    public static final int UPDATA_GROUP_COUT = 2;//更改组数

    public static final int MSG_CHECK = 3;//点击到时确认

    private boolean isRun;//倒计时是否开始

    private boolean isPause;//是否在暂停状态;

    private TimeSetted timeSetted;

    public static Handler handler;

    private CountdownService.CountdownBinder countdownBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            countdownBinder = (CountdownService.CountdownBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeSetted =new TimeSetted();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int groupNum = preferences.getInt("groupNum",0);
        timeSetted.setGroupCouted(groupNum);
        isRun = false;
        isPause = false;
        countdownFragment = new CountdownFragment();
        setTimeFragment = new SetTimeFragment();
        replaceFragment(setTimeFragment);
        groupNumText = (TextView) findViewById(R.id.text_group_num);
        groupNumText.setText("第" + timeSetted.getGroupCouted() + "组");
        startButton = (Button) findViewById(R.id.button_start);
        returnButton = (Button) findViewById(R.id.button_return);
        startButton.setOnClickListener(this);
        returnButton.setOnClickListener(this);
        Intent intent = new Intent(this,CountdownService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        handler = new Handler()  {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPDATE_REMAIN_TIME:
                        countdownFragment.UpdateRemainTime(countdownBinder.getRemainTime());//更新剩余时间
                        break;
                    case UPDATA_GROUP_COUT:
                        //完成计时,组数加1
                        timeSetted.addGroup();
                        countdownFragment.UpdateRemainTime(0);
                        groupNumText.setText("第" + timeSetted.getGroupCouted() + "组");
                        break;
                    case MSG_CHECK:
                        //点击到时确认;
                        isRun = false;
                        replaceFragment(setTimeFragment);
                    default:
                        break;
                }
            }
        };

    }

    @Override
    public void onClick(View view) {
        if(countdownBinder == null){
            return;
        }
        switch (view.getId()){
            case R.id.button_start:
                if (!isRun){
                    //没开始倒计时
                    timeSetted.setMinute(setTimeFragment.getTimeSetted().getMinute());
                    timeSetted.setSecond(setTimeFragment.getTimeSetted().getSecond());
                    if (timeSetted.getMinute() == 0 && timeSetted.getSecond()<3){
                        Toast.makeText(this,"连3秒都没有？",Toast.LENGTH_SHORT).show();
                    }else {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        editor.putInt("min",timeSetted.getMinute());
                        editor.putInt("second",timeSetted.getSecond());
                        editor.apply();
                        replaceFragment(countdownFragment);
                        countdownBinder.startCountdown(timeSetted.getTotalMill());
                        isRun = true;
                    }
                }else if (isPause){
                    countdownBinder.continueCountdown();//在暂停状态就继续;
                    isPause = false;
                }else {
                    countdownBinder.pauseCountdown();//不在暂停状态就暂停
                    isPause = true;
                }
                break;
            case R.id.button_return:
                if (isRun){
                    countdownBinder.cancelCountdown();
                    replaceFragment(setTimeFragment);//返回定时碎片
                    isRun = false;
                }else {
                    //不运行时点击返回，组数清零;
                    timeSetted.setGroupCouted(0);
                    groupNumText.setText("第" + timeSetted.getGroupCouted() + "组");
                }
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.part_main,fragment);
        transaction.commit();
    }

}
