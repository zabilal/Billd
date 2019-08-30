package com.ada.android.billd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ada.android.billd.model.QRCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private DataBaseHelper databaseHelper;
    private IntentIntegrator integrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(myToolbar);

        String username = getIntent().getStringExtra("user");
        TextView welcomeText = findViewById(R.id.textView);
        welcomeText.append(username);
    }

    public void sendMessage(View view) {
        if(view.getId()==R.id.fab1) {
//            Intent intent = new Intent(this, CustomScannerActivity.class);
//            startActivity(intent);
            integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan a barcode");
            integrator.setCameraId(0);  // Use a specific camera of the device
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();

        }
        // Do something in response to button
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.my_bills:
                showBillsGallery();
                break;
            case R.id.settings:
                showSettings();
                break;
            case R.id.logout:
                logout();
                break;
        }
        return true;
    }

    private void logout() {
        getIntent().getExtras().clear();
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        this.finish();
    }

    private void showSettings() {
        Intent login = new Intent(this, SettingActivity.class);
        startActivity(login);
    }

    private void showBillsGallery() {
        Intent login = new Intent(this, GalleryActivity.class);
        startActivity(login);
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            String filename = generateQRCode(result.getContents());
            if(result.getContents() == null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                Toast.makeText(this, filename, Toast.LENGTH_LONG).show();
                //Save content to database
                databaseHelper = new DataBaseHelper(this);
                QRCode code = new QRCode();
                code.setContent(result.getContents());
                code.setFileName(filename);
                databaseHelper.addQrcode(code);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String generateQRCode(String data){
        com.google.zxing.Writer wr = new MultiFormatWriter();
        Bitmap mBitmap = null;
        String fname = "";
        try {
            int width = 350;
            int height = 350;
            if (data != null) {
                BitMatrix bm = wr.encode(data, BarcodeFormat.QR_CODE, width, height);
                mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (mBitmap != null) {
            fname = saveImage(mBitmap);
            return fname;
        }
        return fname;
    }

    private String saveImage(Bitmap finalBitmap) {
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE, 1);
        }
        String fname = "";
        try
        {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/MY_BILLS");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            Random generator = new Random();
            int n = 10000;
//            LocalDateTime now = LocalDateTime.now();
            n = generator.nextInt(n);
            fname = new Date() +".jpg";
            File file = new File(myDir, fname);
            if (file.exists ())
                file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                String fpath = file.toString();
                galleryAddPic(fpath);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return fname;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return fname;
    }

    /**
     * Listener function which listens to a qrcode scan event
     * and the automatically add the image to the devices
     * gallery for instant availability
     * @param fpath
     */
    private void galleryAddPic(String fpath) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(fpath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}





