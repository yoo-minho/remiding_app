package com.reminding;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class ScreenReceiver extends BroadcastReceiver {

    private KeyguardManager km = null;
    private KeyguardManager.KeyguardLock keyLock = null;
    private TelephonyManager telephonyManager = null;
    private boolean isPhoneIdle = true;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { //스크린을 끌 때
            if ( km == null ){
                km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            }
            if ( keyLock == null ){
                keyLock = km.newKeyguardLock(Context.KEYGUARD_SERVICE);
            }
            if (telephonyManager == null){
                telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

            if(isPhoneIdle){
                disableKeyguard();
                Intent i = new Intent(context, ScreenActivity.class); //액티비티를 연결하고
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //액티비티가 아니라 에러가 뜨므로 넣어줌
                context.startActivity(i); //실행시킨다.
            }
        }
    }

    public void disableKeyguard(){
        keyLock.disableKeyguard();
    }

    private PhoneStateListener phoneListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE : //전화발신중일때
                    isPhoneIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING : //전화벨울릴때
                    isPhoneIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK : //통화중일때
                    isPhoneIdle = false;
                    break;
            }
        }
    };

}
