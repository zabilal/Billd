package com.ada.android.billd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ada.android.billd.model.ImageItem;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private GalleryViewAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar myToolbar = findViewById(R.id.toolbar1);
//        myToolbar.setTitle("My Bills");
        setSupportActionBar(myToolbar);

        gridView = findViewById(R.id.gridView);
        gridAdapter = new GalleryViewAdapter(this, R.layout.gallery_item, getData());
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick (AdapterView < ? > parent, View v,int position, long id){
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                //Create intent
                Intent intent = new Intent(GalleryActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());
                //Start details activity
                startActivity(intent);
            }
        });
    }

    private ArrayList getData() {
        final ArrayList imageItems = new ArrayList();

        String root = Environment.getExternalStorageDirectory().toString();
        File path = new File(root + "/MY_BILLS");

        File[] imageFiles = path.listFiles();
        for (int i = 0; i < imageFiles.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());
            imageItems.add(new ImageItem(bitmap, imageFiles[i].getName()));
        }
        return imageItems;
    }

}
