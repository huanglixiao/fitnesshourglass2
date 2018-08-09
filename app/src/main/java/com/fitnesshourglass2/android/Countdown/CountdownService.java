package com.fitnesshourglass2.android.Countdown;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fitnesshourglass2.android.MainActivity;
import com.fitnesshourglass2.android.R;
import com.fitnesshourglass2.android.TimeSetted;

public class CountdownService extends Service {

    private static final int NOTICE_ID = 1;

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
                    startForeground(NOTICE_ID,getNotification("计时器",mTotalTime));
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
            stopForeground(true);
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
        getNotificationManager().notify(NOTICE_ID,getNotification("计时器",mMillisUntilFinish));//更新通知栏
    }

    private void onFinished(){
        Message message = new Message();
        message.what = MainActivity.UPDATA_GROUP_COUT;
        MainActivity.handler.sendMessage(message);
        Log.d(TAG,"Countdown finished");
        stopForeground(true);//关闭去前台，创建下载成功的通知
        getNotificationManager().notify(NOTICE_ID,getNotification("计时结束",-1));
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, long progress){

        NotificationCompat.Builder builder = null;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("myChannal","whatever", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this,"myChannal");
        }else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentIntent(pi);
        if (progress > 0){
            int remainMin =(int) (progress/TimeSetted.SECOND_TO_MILL) / 60;
            if (remainMin > 0 ){
                builder.setContentText("还剩" + remainMin + "分钟");
            }else {
                builder.setContentText("还剩不到1分钟");
                builder.setAutoCancel(true);
            }
        }
        return builder.build();
    }

}
