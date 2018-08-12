package com.fitnesshourglass2.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.fitnesshourglass2.android.Countdown.CountdownService;
import com.fitnesshourglass2.android.SetTime.SetTimeFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button startButton;

    private SetTimeFragment setTimeFragment;

    public static final int UPDATE_REMAIN_TIME = 1;//更改剩余时间

    public static final int UPDATA_GROUP_COUT = 2;//更改组数

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
        /**
         *设定时间测试部分
         */
        setTimeFragment = (SetTimeFragment) getSupportFragmentManager().findFragmentById(R.id.part_set_time);
        Log.d("RUSS","运行正常" );
        startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(this);
        Intent intent = new Intent(this,CountdownService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        handler = new Handler()  {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPDATE_REMAIN_TIME:
                        //timeTextView.setText("还剩" + countdownBinder.getRemainTime());
                        break;
                    case UPDATA_GROUP_COUT:
                        //timeTextView.setText("时间到了");
                        break;
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
                Log.d("RUSS","时长"  + (int)setTimeFragment.getTotalTime() );
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

}
