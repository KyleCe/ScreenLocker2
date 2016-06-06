package com.ce.game.screenlocker.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.ce.game.screenlocker.common.DU;
import com.ce.game.screenlocker.common.TimeU;

import java.io.File;
import java.io.IOException;

/**
 * Created by KyleCe on 2016/5/31.
 *
 * @author: KyleCe
 */
final public class CameraHelper {

    private static String mCurrentPhotoPath;

    /**
     * Check if this device has a camera
     */
    public static boolean hasCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static void cameraStrategy(Context context, boolean withSecure) {
        if (!withSecure) {
            Intent direct = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            direct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(direct);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(cameraIntent);
            }
        }
    }

    public static void refreshImageIfFileExist(final Context context) {
        if (context == null || mCurrentPhotoPath == null) return;

        File tmp = new File(mCurrentPhotoPath);
        if (!tmp.isFile()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(tmp); //out is your output file
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
            mCurrentPhotoPath = "";
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + mCurrentPhotoPath)));
            mCurrentPhotoPath = "";
        }
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = TimeU.getCurrentTime(TimeU.TIME_FORMAT_FIVE);
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        DU.sd("path", mCurrentPhotoPath);
        return image;
    }
}
