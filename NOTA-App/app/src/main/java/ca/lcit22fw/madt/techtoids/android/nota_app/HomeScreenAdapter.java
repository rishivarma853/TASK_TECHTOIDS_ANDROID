package ca.lcit22fw.madt.techtoids.android.nota_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
public class HomeScreenAdapter extends BaseAdapter {
    Context context;
    int logos[];
    LayoutInflater inflter;
    public HomeScreenAdapter(Context applicationContext, int[] logos) {
        this.context = applicationContext;
        this.logos = logos;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return logos.length;
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.activity_home_item, null); // inflate the layout
        ImageView icon = (ImageView) view.findViewById(R.id.boardImg); // get the reference of ImageView
        icon.setImageResource(logos[i]); // set logo images
        return view;
    }
}
