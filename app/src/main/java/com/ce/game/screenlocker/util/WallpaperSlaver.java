package com.ce.game.screenlocker.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.ce.game.screenlocker.common.Const;
import com.ce.game.screenlocker.common.ImageUtil;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KyleCe on 2016/7/18.
 *
 * @author: KyleCe
 */
public class WallpaperSlaver {

    private static final String TAG = WallpaperSlaver.class.getSimpleName();

    private static final int ALLOWED_WALLPAPER_FILE_NUMBER = 13;



    private boolean writeImageToFileAsJpeg(Context context, File f, Bitmap b) {
        try {
            f.createNewFile();
            FileOutputStream thumbFileStream =
                    context.openFileOutput(f.getName(), Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.JPEG, 95, thumbFileStream);
            thumbFileStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error while writing bitmap to file " + e);
            f.delete();
        }
        return false;
    }

    public static File getDefaultThumbFile(Context context) {
        return new File(context.getFilesDir(), Build.VERSION.SDK_INT
                + "_" + Const.DEFAULT_WALLPAPER_THUMBNAIL);
    }

    private boolean saveDefaultWallpaperThumb(Context context, Bitmap b) {
        // Delete old thumbnails.
        new File(context.getFilesDir(), Const.DEFAULT_WALLPAPER_THUMBNAIL_OLD).delete();
        new File(context.getFilesDir(), Const.DEFAULT_WALLPAPER_THUMBNAIL).delete();

        for (int i = Build.VERSION_CODES.JELLY_BEAN; i < Build.VERSION.SDK_INT; i++) {
            new File(context.getFilesDir(), i + "_"
                    + Const.DEFAULT_WALLPAPER_THUMBNAIL).delete();
        }
        return writeImageToFileAsJpeg(context, getDefaultThumbFile(context), b);
    }

    public static File getAvailableStoreDirFile(String directory, final Context mContext) {
        File logfileDir;
        try {
            if (DU.isSdcardAvailable())
                logfileDir = Environment.getExternalStoragePublicDirectory(directory);
            else
                logfileDir = new File(
                        mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA).dataDir);

            return logfileDir;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encrypt(final String password) {
        int len = password.length();
        char[] words = password.toCharArray();

        for (int i = 0; i < len; i++)
            words[i] = (char) ((int) words[i] + (len - i) - '0' + 'k');

        return String.valueOf(words);
    }

    public String decrypt(final String encrypted) {
        int len = encrypted.length();
        char[] words = encrypted.toCharArray();

        for (int i = 0; i < len; i++)
            words[i] = (char) ((int) words[i] - (len - i) + '0' - 'k');

        return String.valueOf(words);
    }

    public void updateWallpaperFileNameOnWorkThreadWhenUpdatingPassword(final Context context, final String oldPassword, final String newPassword) {
        DU.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    updateWallpaperFileNameWhenUpdatingPassword(context
                            , oldPassword, newPassword);
                }
            }
        });
    }

    public void updateWallpaperFileNameWhenUpdatingPassword(Context context, String oldPassword, String newPassword) {
        Assert.assertNotNull(context);
        Assert.assertFalse(TextUtils.isEmpty(oldPassword));
        Assert.assertFalse(TextUtils.isEmpty(newPassword));

        if(oldPassword.equals(newPassword)) return;

        try {
            File fileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DIR_ROOT + Const.SCREEN_LOCK_WALLPAPER, context);

            if (fileDir == null) return;

            File file = new File(fileDir, Const.USER_WALLPAPER_NAME + encrypt(oldPassword)
                    + Const.USER_WALLPAPER_NAME_SUFFIX);

            if (!fileDir.exists() || !file.exists()) return;

            File newFile = new File(fileDir, Const.USER_WALLPAPER_NAME + encrypt(newPassword)
                    + Const.USER_WALLPAPER_NAME_SUFFIX);

            FileU.renameFile(file.getAbsolutePath(), newFile.getAbsolutePath());
        } catch (Exception e) {
            DU.sd("update wallpaper file name exception", e);
            e.printStackTrace();
        }
    }


    private void changeWallpaper(final Context context, final String password, final String myselfPassword) {
        InputStream is = null;
        try {
            File customWallpaperDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DIR_ROOT + Const.SCREEN_LOCK_WALLPAPER, context);

            File toSetWallpaper;
            if (noAvailableCustomWallpaper(customWallpaperDir)) {
                toSetWallpaper = getLauncherStoredDefaultWallpaper(context);
            } else {
                toSetWallpaper = new File(customWallpaperDir, Const.USER_WALLPAPER_NAME +
                        (password == null ? Const.GUEST_ENIGMA_RESULT : new WallpaperSlaver().encrypt(password))
                        + Const.USER_WALLPAPER_NAME_SUFFIX);

                if (!toSetWallpaper.exists())
                    toSetWallpaper = new File(customWallpaperDir, Const.USER_WALLPAPER_NAME +
                            new WallpaperSlaver().encrypt(myselfPassword) + Const.USER_WALLPAPER_NAME_SUFFIX);

                if (!toSetWallpaper.exists())
                    toSetWallpaper = getLauncherStoredDefaultWallpaper(context);
            }

            if (toSetWallpaper == null) return;

            is = new FileInputStream(toSetWallpaper);

            WallpaperManager.getInstance(context).setStream(is);
        } catch (Exception e) {
            DU.sd("set wallpaper exception when unlock screen to different user", e);
            e.printStackTrace();
        } finally {
            DU.closeSilently(is);
        }
    }

    private boolean noAvailableCustomWallpaper(File customWallpaperDir) {
        return customWallpaperDir == null || !customWallpaperDir.exists() || customWallpaperDir.listFiles().length <= 0;
    }

    @NonNull
    private File getLauncherStoredDefaultWallpaper(Context context) {
        File file;
        File defaultFileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DEFAULT_WALLPAPER_DIRECTORY, context);

        if (defaultFileDir == null || !defaultFileDir.exists()) return null;

        file = new File(defaultFileDir, Const.DEFAULT_WALLPAPER_NAME);

        if (!file.exists()) return null;

        return file;
    }

    public void storeDefaultWallpaperIfNotYet(@NonNull final Context context) {
        DU.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    File fileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DEFAULT_WALLPAPER_DIRECTORY, context);

                    if (fileDir == null) return;

                    File file = new File(fileDir, Const.DEFAULT_WALLPAPER_NAME);

                    if (fileDir.exists() && file.exists()) return;

                    if (!fileDir.exists()) file.getParentFile().mkdirs();

                    if (file.exists()) file.delete();

                    file.createNewFile();

                    fos = new FileOutputStream(file);
                    ImageUtil.drawableToBitmap(getDefaultWallpaperDrawable()).compress(Bitmap.CompressFormat.PNG, 100, fos);

                    notifyMediaCenter(fileDir);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    DU.closeSilently(fos);
                }
            }

            private Drawable getDefaultWallpaperDrawable() {
                return WallpaperManager.getInstance(context).getDrawable();
            }

            private void notifyMediaCenter(File logfileDir) {
                if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT))
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + logfileDir.getAbsolutePath())));
                else
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + logfileDir.getAbsolutePath())));
            }
        });
    }

    private String getSuffixForNotMyselfUsers(String passwordOfUserToSetWallpaper) {
        Assert.assertFalse(TextUtils.isEmpty(passwordOfUserToSetWallpaper));

        if (passwordOfUserToSetWallpaper.equals(Const.EMPTY_PASSWORD_ENIGMA_RESULT))
            return Const.EMPTY_PASSWORD_ENIGMA_RESULT;
        else if (passwordOfUserToSetWallpaper.equals(Const.GUEST_ENIGMA_RESULT))
            return Const.GUEST_ENIGMA_RESULT;
        else return new WallpaperSlaver().encrypt(passwordOfUserToSetWallpaper);
    }

}
