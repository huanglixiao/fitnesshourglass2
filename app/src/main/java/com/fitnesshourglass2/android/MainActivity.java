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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fitnesshourglass2.android.Countdown.CountdownService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     *测试部分
     */
    private Button startTestButton;

    private Button continueTestButton;

    private Button pauseTestButton;

    private Button cancelTestButton;

    private TextView timeTextView;

    /**
     *
     */

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
        startTestButton = (Button) findViewById(R.id.button_start_test);
        continueTestButton = (Button) findViewById(R.id.button_continue_test);
        pauseTestButton = (Button) findViewById(R.id.button_pause_test);
        cancelTestButton = (Button) findViewById(R.id.button_cancel_test);
        timeTextView = (TextView) findViewById(R.id.text_remain_time);
        startTestButton.setOnClickListener(this);
        continueTestButton.setOnClickListener(this);
        pauseTestButton.setOnClickListener(this);
        cancelTestButton.setOnClickListener(this);
        Intent intent = new Intent(this,CountdownService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        handler = new Handler()  {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case UPDATE_REMAIN_TIME:
                        timeTextView.setText("还剩" + countdownBinder.getRemainTime());
                        break;
                    case UPDATA_GROUP_COUT:
                        timeTextView.setText("时间到了");
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
            case R.id.button_start_test:
                long totalTime = 60000;
                countdownBinder.startCountdown(totalTime);
                Message message = new Message();
                message.what = MainActivity.UPDATE_REMAIN_TIME;
                handler.sendMessage(message);
                break;
            case R.id.button_continue_test:
                countdownBinder.continueCountdown();
                break;
            case R.id.button_pause_test:
                countdownBinder.pauseCountdown();
                break;
            case R.id.button_cancel_test:
                countdownBinder.cancelCountdown();
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
