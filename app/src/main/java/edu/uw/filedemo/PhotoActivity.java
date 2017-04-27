package edu.uw.filedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "Photo";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri pictureFileUri;
    private Uri mediaStoreUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        //action bar "back"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void takePicture(View v){
        Log.v(TAG, "Taking picture...");

        // making a file for the camera to save the picture it takes inside
        File file = null;
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //include timestamp

            //ideally should check for permission here, skipping for time
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            file = new File(dir, "PIC_"+timestamp+".jpg");
            boolean created = file.createNewFile(); //actually make the file!
            Log.v(TAG, "File created: "+created);

        } catch (IOException ioe) {
            Log.d(TAG, Log.getStackTraceString(ioe));
        }

        if (file != null) {
            pictureFileUri = Uri.fromFile(file);

            // initially, our file path looks kinda like file://storage/emulator/0/Picture/mypic.jpg
            // that's why the emulator would crash on trying to share pictures, so the solution ends up
            // being that we make the picture have a content:// thing.  So we use a contentProvider:
            // and then in sharePicture, make sure that you are putExtra on shareIntent the mediaStoreUri and not the pictureFileUri

            MediaScannerConnection.scanFile(this, new String[] {file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            mediaStoreUri = uri;  //save the content:// Uri for later
                            Log.v(TAG, "MediaStore Uri: "+uri);
                        }
                    });


            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // the camera says: If you give me this particular extra, I'll know where to save the image to.
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap)extras.get("data");
            ImageView imageView = (ImageView)findViewById(R.id.imgThumbnail);
            imageView.setImageURI(pictureFileUri); // put in full resolution picture
//            imageView.setImageBitmap(imageBitmap);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sharePicture(View v){
        Log.v(TAG, "Sharing picture...");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*"); // put on the envelope "only handle this intent if you can handle pictures, if not, don't even bother
        shareIntent.putExtra(Intent.EXTRA_STREAM, mediaStoreUri);

        Intent chooser = Intent.createChooser(shareIntent, "Share My Picture");
        startActivity(chooser);


    }
}
