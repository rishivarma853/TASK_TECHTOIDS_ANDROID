package ca.lcit22fw.madt.techtoids.android.nota_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ca.lcit22fw.madt.techtoids.android.nota_app.Models.Home;
public class HomeScreenAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflter;
    public HomeScreenAdapter(Context applicationContext) {
        this.context = applicationContext;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return Home.home.getBoardList().size();
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
        TextView boardTitle = view.findViewById(R.id.BoardTitle);
        icon.setImageResource(R.drawable.whiteboard); // set logo images
        boardTitle.setText(Home.home.getBoardList().get(i).getTitle());
        return view;
    }
}
