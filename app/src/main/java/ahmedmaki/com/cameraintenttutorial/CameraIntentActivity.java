package ahmedmaki.com.cameraintenttutorial;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraIntentActivity extends AppCompatActivity {

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private ImageView mPhotoCapturedImageView;
    private String mImageFileLocation = "";
    private int REQUESTED_EXTERNAL_STORAGE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intent);

        mPhotoCapturedImageView = (ImageView) findViewById(R.id.photoCapturedImageView);

    }

    public void takePhoto(View view) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            callCameraApp();
        }else {
            if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this,"External Permission Required to save Image",Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTED_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if(requestCode==REQUESTED_EXTERNAL_STORAGE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                callCameraApp();
            }else {
                Toast.makeText(this,"Extrnal write permission not granted",Toast.LENGTH_LONG).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    private void callCameraApp(){
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile=null;
        try{
            photoFile = createImageFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
//            Toast.makeText(getApplicationContext(), "Picture  taken successful", Toast.LENGTH_LONG).show();
//            Bundle extras= data.getExtras();
//            Bitmap photoCapturedBitmap = (Bitmap) extras.get("data");
//            mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
//            Bitmap photoCapturedBitmap= BitmapFactory.decodeFile(mImageFileLocation);
//            mPhotoCapturedImageView.setImageBitmap(photoCapturedBitmap);
            rotateImage(setReducedImageSize());
        }
    }

    File createImageFile() throws IOException {
        String timeStamp= new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFIleName= "IMAGE_"+timeStamp+"_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File image = File.createTempFile(imageFIleName,".jpg",storageDirectory);
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }

    private Bitmap setReducedImageSize(){
        int targetedImageViewWidth = mPhotoCapturedImageView.getMaxWidth();
        int targetedImageViewHeight = mPhotoCapturedImageView.getMaxHeight();

        BitmapFactory.Options bmOptions= new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
        int cameraImageWidth= bmOptions.outWidth;
        int cameraImageHieght= bmOptions.outHeight;

        int scalarFactor = Math.min(cameraImageWidth/targetedImageViewWidth,cameraImageHieght/targetedImageViewHeight);
        bmOptions.inSampleSize=scalarFactor;
        bmOptions.inJustDecodeBounds=false;

//        Bitmap  photoReducedSizeBitmap= BitmapFactory.decodeFile(mImageFileLocation,bmOptions);
//        mPhotoCapturedImageView.setImageBitmap(photoReducedSizeBitmap);
        return BitmapFactory.decodeFile(mImageFileLocation,bmOptions);


    }

    private void rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(mImageFileLocation);
        }catch (IOException e){
            e.printStackTrace();
        }
        int orintion=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orintion){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            default:
        }
        Bitmap rotatedBitmap= Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        mPhotoCapturedImageView.setImageBitmap(rotatedBitmap);
    }
}
