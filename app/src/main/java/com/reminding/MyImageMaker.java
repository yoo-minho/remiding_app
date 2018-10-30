package com.reminding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

public class MyImageMaker {

    // size 200*150 적용시, 최대 1600*1200 이미지 커버, 이미지 최대용량 0.11MB
    // size 400*300 적용시, 최대 6400*4800 이미지 커버, 이미지 최대용량 0.46MB
    // size 800*600 적용시, 최대 25600*19200 이미지 커버, 이미지 최대용량 1.84MB
    // 한 어플당 2.8MB 이상쓰면 OOM 발생한다는 개인 자료 찾음 (기기마다 다를 듯)
    // Dalvik Heap 메모리 공간은 최소 16MB, 최대 24MB 라고 한다. (공식홈페이지 상)
    // 한 화면에 최소 34개의 이미지를 활용할 수 있다.
    // 리사이클뷰는 재사용하는 것이므로 다 사용한다 볼 수 없고
    // 뷰페이저는 모든 프래그먼트의 이미지를 다 사용한다 볼 수 있다.
    // 고려하여 사이즈값을 설정하면 될 것 같다.

    //비트맵 사이즈 조정하기
    @NonNull
    public Bitmap getImage(Uri uri, int degrees, int size){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16; //16배 줄여
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try{
                Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                if ( bitmap.getWidth() * bitmap.getHeight() < size) { //너무 작으면
                    options.inSampleSize = 8; //8배 줄이자
                    bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                    if (  bitmap.getWidth() * bitmap.getHeight() < size ) { //근데도 작으면
                        options.inSampleSize = 4; //4배 줄이자
                        bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                        if ( bitmap.getWidth() * bitmap.getHeight() < size ) { //근데도 작자나
                            options.inSampleSize = 2; //2배 줄이자
                            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                            if ( bitmap.getHeight() * bitmap.getHeight() < size ) {
                                options.inSampleSize = 1; //줄이지마
                                bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                            }
                        }
                    }
                } else {
                    bitmap = null;
                }
                if( bitmap != null ){
                    Matrix matrix = new Matrix();
                    matrix.postRotate(degrees); //회전값을 주고
                    bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                }

                return bitmap;

            } catch (OutOfMemoryError e){ //혹시나 OOM 나도 꺼지지마
                e.printStackTrace();
            } catch (NullPointerException e) { //혹시나 Null 나도 꺼지지마
                e.printStackTrace();
            }
        return null;
    }

    //실제경로 만들기
    public Uri getRealPath(Context context, Uri uri){

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        @SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
        cursor.moveToFirst();
        String realPath = cursor.getString(columnIndex);
        Uri realPathUri = Uri.parse(realPath);

        return realPathUri;
    };


}
