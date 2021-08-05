package ntu.cz3004.controller.view.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class MDPPagerAdapter extends PagerAdapter {
    private boolean isTablet;
    private View[] views;

    public MDPPagerAdapter(boolean isTablet, View[] views){
        this.isTablet = isTablet;
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = views[position];
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
    @Override
    public float getPageWidth(int position) {
        if (position==1){
            return isTablet?0.6f:0.8f;
        }
        return 1;
    }
}
