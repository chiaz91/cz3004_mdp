package ntu.cz3004.controller.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import app.entity.Map;
import ntu.cz3004.controller.view.MapView;

public class MDPMapPagerAdapter extends PagerAdapter {
    private ArrayList<String> listMdf;

    public MDPMapPagerAdapter(ArrayList<String> listMdf){
        this.listMdf = listMdf;
    }

    @Override
    public int getCount() {
        return listMdf.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0){
            return "Saved";
        } else {
            return "Map "+position;
        }
    }

    private Map getMapAt(int pos){
        Map map = new Map();
        map.updateAs( listMdf.get(pos) );
        return map;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//        int padding = container.getContext().getResources().getDimensionPixelSize(R.dimen.map_padding);
        MapView mv = new MapView(container.getContext());
//        mv.setPadding(padding,padding,padding,padding);
        mv.setMap(getMapAt(position));
        container.addView(mv);
        return mv;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
