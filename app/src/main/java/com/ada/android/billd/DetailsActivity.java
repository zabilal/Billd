package com.ada.android.billd;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ada.android.billd.model.QRCode;

public class DetailsActivity extends AppCompatActivity {

    private DataBaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar myToolbar = findViewById(R.id.toolbar1);
        myToolbar.setTitle("Receipt Details");
        setSupportActionBar(myToolbar);

        databaseHelper = new DataBaseHelper(DetailsActivity.this);

        String title = getIntent().getStringExtra("title");
        Bitmap bitmap = getIntent().getParcelableExtra("image");

        TextView titleTextView =  findViewById(R.id.title);
        titleTextView.setText(title);

//        ImageView imageView =  findViewById(R.id.image);
//        imageView.setImageBitmap(bitmap);

        fetchQRCodeContents(title);
    }

    private void fetchQRCodeContents(String filename){
        QRCode qrCode = databaseHelper.getQrcode(filename);

        EditText details = findViewById(R.id.edit_text);
        details.setEnabled(false);
        details.setText(qrCode.getContent());
    }
}