package com.fitnesshourglass2.android.Countdown;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.fitnesshourglass2.android.MainActivity;
import com.fitnesshourglass2.android.R;
import com.fitnesshourglass2.android.TimeSetted;

import java.io.File;

public class CountdownService extends Service {

    private BroadcastReceiver overBroadcastReceiver;

    private static final int NOTICE_ID = 1;

    private static final String TAG = "ServiceTest";

     private Timer timer;

     private long mMillisUntilFinish;

     private PowerManager powerManager;

     private PowerManager.WakeLock PMWakeLock;

     private CountdownBinder mBinder = new CountdownBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PMWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"RUSS");
        overBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction =intent.getAction();
                if (intentAction.equals("com.fitnesshourglass2.android.ACTION_CHECK")){
                    Message message = new Message();
                    message.what = MainActivity.MSG_CHECK;
                    MainActivity.handler.sendMessage(message);
                    stopForeground(true);
                    PMWakeLock.release();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.fitnesshourglass2.android.ACTION_CHECK");
        registerReceiver(overBroadcastReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(overBroadcastReceiver);
    }

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
                    startForeground(NOTICE_ID,getNotification("组间休息",mTotalTime));
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
        getNotificationManager().notify(NOTICE_ID,getNotification("组间休息",mMillisUntilFinish));//更新通知栏
    }

    private void onFinished(){
        Message message = new Message();
        message.what = MainActivity.UPDATA_TIME_TO_ZERO;
        MainActivity.handler.sendMessage(message);
        stopForeground(true);//关闭去前台，创建下载成功的通知
        startForeground(NOTICE_ID,getNotification("计时结束",-1));
        PMWakeLock.acquire();
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, long progress){
        NotificationCompat.Builder builder = null;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel remainTimeChannel = new NotificationChannel("myChannal","remainTime",NotificationManager.IMPORTANCE_LOW);
            NotificationChannel checkChannel = new NotificationChannel("myChannal2","check",NotificationManager.IMPORTANCE_HIGH);
            checkChannel.setSound(null,null);
            checkChannel.enableVibration(true);
            checkChannel.setVibrationPattern(new long[]{0,500,500});
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (progress > 0){
                manager.createNotificationChannel(remainTimeChannel);
                builder = new NotificationCompat.Builder(this,"myChannal");
            }else {
                manager.createNotificationChannel(checkChannel);
                builder = new NotificationCompat.Builder(this,"myChannal2");
            }
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
            }
            return builder.build();
        }else{
            builder.setFullScreenIntent(pi,true)
                    .setSound(null);
            builder.setContent(getRemoteViews());
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_INSISTENT;
            return notification;
        }
    }

    private RemoteViews getRemoteViews(){
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.notification_over);
        Intent intent = new Intent("com.fitnesshourglass2.android.ACTION_CHECK");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_check,pendingIntent);
        return remoteViews;
    }

}
