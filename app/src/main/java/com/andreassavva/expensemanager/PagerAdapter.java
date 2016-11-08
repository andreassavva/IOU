package com.andreassavva.expensemanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

//Här är Adapter klassen som tillhör TabLayout, bestämmer hur tabs ska visas.

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    TabFragment1 tab1;
    TabFragment2 tab2;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        tab1 = new TabFragment1();
        tab2 = new TabFragment2();
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return tab1;
            case 1:
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
