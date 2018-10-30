package com.reminding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class CardItem {

    private String title;
    private long date = 0;
    private Uri uri;
    private int degrees = 0;
    private int alpha = 0;

    public CardItem(String title, long date, Uri uri, int degrees, int alpha){
        this.title = title;
        this.date = date;
        this.uri = uri;
        this.degrees =degrees;
        this.alpha = alpha;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date){
        this.date = date;
    }

    public int getDegrees() {
        return degrees;
    }

    public void setDegrees(int degrees){
            this.degrees = degrees;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri){
        this.uri = uri;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha){
        this.alpha = alpha;
    }


}


