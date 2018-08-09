package com.fitnesshourglass2.android.Countdown;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.fitnesshourglass2.android.MainActivity;
import com.fitnesshourglass2.android.TimeSetted;

public class CountdownService extends Service {

    private static final String TAG = "ServiceTest";

     private Timer timer;

     private long mMillisUntilFinish;

     private CountdownBinder mBinder = new CountdownBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class CountdownBinder extends Binder{

        public void startCountdown(long TotalTime){
            final long mTotalTime = TotalTime;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    timer = new Timer(mTotalTime, TimeSetted.SECOND_TO_MILL);
                    timer.start();
                    Log.d(TAG,"Countdown start");
                    Looper.loop();
                }
            }).start();
        }

        public void pauseCountdown(){
            timer.pause();
            Log.d(TAG,"Countdown pause,还剩" + mMillisUntilFinish);
        }

        public void continueCountdown(){
            timer.start();
            Log.d(TAG,"Countdown continue");
        }

        public void cancelCountdown(){
            timer.cancel();
            stopSelf();
            Log.d(TAG,"Countdown canceled");
        }

        /**
         * 计时完成
         */
        private void finishedCountdown(){
            stopSelf();
        }

        public long getRemainTime(){
            return mMillisUntilFinish;
        }

    }

    class Timer extends MyCountdownTimer{


        public Timer(long millisInFuture, long countdownInterval) {
            super(millisInFuture, countdownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished, int percent) {
            onProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            mBinder.finishedCountdown();
            onFinished();
        }

    }

    private void onProgress(long millisUntilFinish){
        mMillisUntilFinish = millisUntilFinish;
        Message message = new Message();
        message.what = MainActivity.UPDATE_REMAIN_TIME;
        MainActivity.handler.sendMessage(message);
    }

    private void onFinished(){
        Message message = new Message();
        message.what = MainActivity.UPDATA_GROUP_COUT;
        MainActivity.handler.sendMessage(message);
        Log.d(TAG,"Countdown finished");
    }

}
