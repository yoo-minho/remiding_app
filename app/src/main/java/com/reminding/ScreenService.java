package com.reminding;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import android.widget.Toast;

import javax.crypto.Cipher;

public class ScreenService extends Service {

    private ScreenReceiver mReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter); //필터를 가진 스크린 리시버를 등록한다.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //리모트뷰는 콘스트레인트레이아웃 적용 안됩니다.
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.custom_notification);

        //클릭할 때 인텐트 생성
        Intent i = new Intent(this, MainActivity.class);
        //클릭하기전까지 가지고 있음, 셋콘텐츠인텐트로 연결
        PendingIntent p = PendingIntent.getActivity(this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"default");
        builder.setSmallIcon(R.drawable.ic_couple_of_arrows_changing_places)
                .setContentIntent(p)
//              .setStyle(new NotificationCompat.DecoratedCustomViewStyle()) 대중적인 스타일
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build();
        Notification customNotification = builder.build();

        startForeground(1, customNotification);

        if ( intent != null ){ //인텐트가 있고
            if(intent.getAction() == null ){ //행동이 없으며
                if ( mReceiver == null ){ //스크린 리시버가 없으면
                    mReceiver = new ScreenReceiver(); //새로 만들고
                    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                    registerReceiver(mReceiver, filter); //위 같은 필터를 넣어준다
                }
            }
        }
        return START_REDELIVER_INTENT; //메모리가 여유생겼을 때 서비스 다시 살아나라! (근데 잘 안살아남)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver != null){ //스크린리시버가 있으면
            unregisterReceiver(mReceiver); //필터를 가진 스크린 리시버를 해제한다.
        }
    }

}
