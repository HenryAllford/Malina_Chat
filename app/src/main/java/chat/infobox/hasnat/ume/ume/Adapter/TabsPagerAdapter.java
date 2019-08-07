package chat.infobox.hasnat.ume.ume.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import chat.infobox.hasnat.ume.ume.Fragments.ChatsFragment;
import chat.infobox.hasnat.ume.ume.Fragments.RequestsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter{


    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new RequestsFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Чаты";
            case 1:
                return "Запросы на дружбу";
            default:
                return null;
        }
    }
}
