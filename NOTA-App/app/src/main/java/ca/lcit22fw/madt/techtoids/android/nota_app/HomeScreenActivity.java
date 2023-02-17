package ca.lcit22fw.madt.techtoids.android.nota_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ca.lcit22fw.madt.techtoids.android.nota_app.Models.Global;

public class HomeScreenActivity extends AppCompatActivity {

    GridView simpleGrid;
    FloatingActionButton fltbtnAddNewBoard;
//    int[] whiteboardImg = {R.drawable.whiteboard,R.drawable.whiteboard};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        simpleGrid = (GridView) findViewById(R.id.simpleGridView); // init GridView
        fltbtnAddNewBoard = findViewById(R.id.fab);
        // Create an object of CustomAdapter and set Adapter to GirdView
        HomeScreenAdapter customAdapter = new HomeScreenAdapter(getApplicationContext());
        simpleGrid.setAdapter(customAdapter);
        // implement setOnItemClickListener event on GridView
        simpleGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // set an Intent to Another Activity
                Intent intent = new Intent(HomeScreenActivity.this, TaskScreenActivity.class);
                intent.putExtra(Global.POSITION, position); // put image data in Intent
                startActivity(intent); // start Intent
            }
        });
        fltbtnAddNewBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

    }
}