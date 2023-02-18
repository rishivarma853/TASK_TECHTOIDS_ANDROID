package ca.lcit22fw.madt.techtoids.android.nota_app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

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
                showDialog();
            }
        });

    }
//        fltbtnAddNewBoard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(HomeScreenActivity.this);
//                LayoutInflater inflater = HomeScreenActivity.this.getLayoutInflater();
//
//                alert.setTitle("Adding a new board ");
//                alert.setMessage("Enter Name");
//                 EditText input = new EditText(HomeScreenActivity.this);
//                alert.setView(input);
//
//                alert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                String value = String.valueOf(input.getText());
//                                // Do something with value!
//                            }
//                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//
//                            }
//                        })
//                        .create().show();
//
//
//
//    }
//});
//    }
    
        private void showDialog () {
            Dialog dialog = new Dialog(this, R.style.DialogStyle);
            dialog.setContentView(R.layout.customdialogbox);

            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_window);

            ImageView btnClose = dialog.findViewById(R.id.btn_close);

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }
