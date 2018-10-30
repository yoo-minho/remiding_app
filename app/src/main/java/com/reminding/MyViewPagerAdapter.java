package com.reminding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyViewPagerAdapter extends FragmentPagerAdapter {

    private static int PAGE_NUMBER = 2;
    private Bundle bundle;

    public MyViewPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        this.bundle = bundle;
    }

    @Override
    public Fragment getItem(int i) {
        switch ( i ){
            case 0:
                return IdiotNoteFragment.newInstance(bundle);
            case 1:
                return InsideFragment.newInstance();
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int i) {
        switch (i ){
            case 0:
                return "리마인딩메모장";
            case 1:
                return "리마인딩갤러리";
            default:
                return null;
        }
    }

}
