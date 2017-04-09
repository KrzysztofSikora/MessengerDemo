package pl.krzysztofsikora.messengerdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.ShareToMessengerParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 0;


    CallbackManager callbackManager;
    private View mMessengerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(this);

        setContentView(R.layout.activity_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMessengerButton = findViewById(R.id.messenger_send_button);

        mMessengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessengerButtonClicked();
            }
        });


    }
    private void onMessengerButtonClicked() {
  selectImage();
    }

    private void selectImage() {
    final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
   AlertDialog.Builder builder = new AlertDialog.Builder(ShareActivity.this);
                builder.setTitle("Select profile Photo!");
                builder.setItems(items, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int item) {
          if (items[item].equals("Take Photo")) {
                // Opens Camera to take a picture
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intent, REQUEST_CAMERA);
                        }
            else if (items[item].equals("Choose from Library")) {
                // Opens the galary to choose image
                                    Intent intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent,"Select File"),
                        SELECT_FILE);
            }
            else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        }
    });
    builder.show();
}



    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
        if (requestCode == SELECT_FILE)
                    onSelectFromGalleryResult(data); //image is chosen from gallery</p>
        else if (requestCode == REQUEST_CAMERA)
                    onCaptureImageResult(data); // image is captured using device camera<br />
        }
    }



    private void onSelectFromGalleryResult(Intent data) {
   Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
    String selectedImagePath = cursor.getString(column_index);
    Bitmap thumbnail;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
    final int REQUIRED_SIZE = 200;
        int scale = 1;
    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
            && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
    options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                thumbnail = BitmapFactory.decodeFile(selectedImagePath, options);
    String path=MediaStore.Images.Media.insertImage(getContentResolver(),
                thumbnail,
                "Image Description",null);
                Uri uri= Uri.parse(path);
                shareToMessenger(uri);
    }


    private void onCaptureImageResult(Intent data) {
    Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
    File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
    FileOutputStream fo;
        try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
        }
    catch (FileNotFoundException e) {
                e.printStackTrace();
        }
    catch (IOException e) {
                e.printStackTrace();
        }
    String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                thumbnail,"Image Description",null);
                Uri uri = Uri.parse(path);
                shareToMessenger(uri);
    }



    private void shareToMessenger(Uri imagePath){
   // Create the parameters for what we want to send to Messenger.
                ShareToMessengerParams shareToMessengerParams =
                ShareToMessengerParams.newBuilder(imagePath,"image/*")
                    .build();
   // Shares the content to Messenger. If Messenger is not installed
    // or Messenger needs to be upgraded, this will redirect
    // the user to the play store.
    MessengerUtils.shareToMessenger(
            this,
            REQUEST_CODE_SHARE_TO_MESSENGER,
            shareToMessengerParams);
}

}
