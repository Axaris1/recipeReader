package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;


import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    TextView mResultEt;
    ImageView mPreviewIv;
    float suma=0;


    Uri image_uri; // zmienna do pobierania obrazów z galerii

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Click + button to insert image");

        mResultEt = findViewById(R.id.ResultEt);
        mPreviewIv = findViewById(R.id.ImageIv);

        // pozwolenie na kamere


        cameraPermission = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // pozwolenie na używanie pamięci
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    //action bar menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //obsługa przycisków


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.AddImage){
            showImageImportDialog();
        }

        else if (id == R.id.settings){

            Toast.makeText(this,"settings", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        // rzeczy do wyświetlenia
        String[] items = {"Camera","Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // tytuł
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //camera option clicked
                    //od marshmallow grzecznie sprawdzamy pozwolenie na kamerke
                    if(!checkCameraPermission()){
                        //brak pozwolenia, wyslij prosbe
                        requestCameraPermission();
                    }
                    else {
                        //pozwolenie, zrob zdjecia
                        pickCamera();
                    }
                }
                if (which == 1){
                    //gallery option clicked
                    //check permission request for storage
                    if(!checkStoragePermission()){
                        //brak pozwolenia, wyslij prosbe
                        requestStoragePermission();
                    }
                    else {
                        //pozwolenie, wybierz przedmiot z galerii
                        pickGallery();
                    }

                }
            }
        });
        dialog.create().show(); // pokaż dialog
    }

    private void pickGallery() {
//////         pobieranie obrazu z galerii
                Intent intent = new Intent(Intent.ACTION_PICK);
//////         zmiana typu intentu na obrazki
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
//
//
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickCamera() {
        // pobieranie obrazu z kamery i zapisywanie do pamieci w celu zwiekszenia jakosci obrazu
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New picture"); // tytuł obrazka
        values.put(MediaStore.Images.Media.DESCRIPTION, "Skanowany obraz"); // opis obrazka
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);


    }

    // od tej linijki w dół jest sama obsługa androidowych pozwoleń
    // nie widzę sensu pakowania tego z nowa klase, bo nic tu wiecej nie bedzie robione

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        // sprawdzenie pozwolenia na korzystanie z pamięci zewnętrznej
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // funkcja pobierająca pozwolenie na wykorzystanie kamery
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        // aby skorzystać z obrazu w wyższej jakości musi on zostać zapisany do galerii dlatego jest traktowana na równi z główną kamerą
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    // obsługa pozwolen
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean cameraAccepted =grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    //obsługa otrzymanego obrazu


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       if (resultCode == RESULT_OK){
           if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // mamy zdjecie z kamery, przycinamy je

//               CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON). //
//                        start(this);
               CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON). // linie pomocnicze
                       start(this);
           }
           if(requestCode == IMAGE_PICK_GALLERY_CODE){
               //mamy obraz z galerii, przycinamy go
               CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON). // linie pomocnicze
                       start(this);
           }

       }
  //      obrób przyciety obraz
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){



                Uri resultUri = result.getUri(); // link do obrazu
                //image -> imageView, aka wyświetl
                mPreviewIv.setImageURI(resultUri);

                // uzyskaj bitmapDrawable do rozpoznawania tekstu

                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this, "recognition function error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    //pobieraj rozpoznany tekst, az nic w nim nie bedzie
                    for(int i=0; i<items.size(); i++){
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }

                    String regex="([0-9]+[,][0-9]+)";   // wyszukiwanie wzorca liczby przecinkowej
                    String regexSuma="SUMA";            // wyszukiwanie wyrazu suma, po którym występuje interesujaca nas wartość

                    String input= sb.toString();

                    String output = new String();

                    Pattern pattern=Pattern.compile(regex);
                    Matcher matcher=pattern.matcher(input);


                    while(matcher.find()  )
                    {

                        System.out.println(matcher.group());
                        output = matcher.group();
                        break;
                    }
                    //wyswietl text na ekranie
                    String newOutput = output.replace(",",".");
                    float amount=Float.parseFloat(newOutput);
                    suma+=amount;

                    System.out.print(input);
                    mResultEt.setText("Aktualna wartość: " + newOutput + "\n" +
                                        "Suma: " + suma);
                }

            }
          else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //w przypadku bledow wyswietl o co chodzi
               Exception error = result.getError();
               Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();

            }
        }

   }
}
