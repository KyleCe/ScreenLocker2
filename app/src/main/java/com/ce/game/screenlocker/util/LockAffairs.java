package com.ce.game.screenlocker.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.Log;

import com.ce.game.screenlocker.R;
import com.ce.game.screenlocker.common.Const;

import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by KyleCe on 2016/7/6.
 *
 * @author: KyleCe
 */
public class LockAffairs {
    private static final String TAG = LockAffairs.class.getSimpleName();

    public static void vibrate(Context mContext, long milliseconds) {
        if (mContext == null) return;
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(milliseconds == 0 ? 500 : milliseconds);
    }

    @Deprecated
    public static final void disableKeyguard(Context context) {
        Assert.assertNotNull(context);

        final KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("KeyguardLock");
        keyguardLock.disableKeyguard();
    }

    public Bitmap parseCustomBackground(Context context) {
        Assert.assertNotNull(context);

        try {
            File fileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DIR_ROOT + Const.SCREEN_LOCK_WALLPAPER, context);

            if (fileDir == null) return null;

            File file = new File(fileDir, Const.SCREEN_LOCK_WALLPAPER_THUMBNAIL);

            if (!file.exists()) {
                new File(fileDir, Const.SCREEN_LOCK_WALLPAPER_THUMBNAIL_BLURRED).delete();
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap origin = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            // This will scale it to your screen width and height. you need to pass screen width and height.
            origin = Bitmap.createScaledBitmap(origin, 1080, 1920, false);

            return origin;
        } catch (Throwable th) {
            Log.e(TAG, th.toString());
            return null;
        }
    }

    public Drawable generateBlurredDrawable(final Context context, Bitmap defaultBip) {
        DU.assertNotNull(context, defaultBip);

        Bitmap blurred = getBlurredBitmapFromFile(context);
        if (blurred != null) return new BitmapDrawable(context.getResources(), blurred);

        long start = System.currentTimeMillis();

        DU.sd("blur time take", "1", start);

//                defaultBip = sampleBitmap(2);
//                DU.sd("blur time take", 2, System.currentTimeMillis() - start);

        Bitmap blurBitmap = BlurStrategy.getBlurBitmapReturnDefaultIfFail(defaultBip);

        DU.sd("blur time take", "finish",DU.time() - start);

        saveBlurredDrawableFile(context, blurBitmap);

        return new BitmapDrawable(context.getResources(), blurBitmap);
    }

    private void saveBlurredDrawableFile(final Context context, final Bitmap blurredBitmap) {
        InputStream is = null;
        OutputStream os = null;
        try {
            File fileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DIR_ROOT + Const.SCREEN_LOCK_WALLPAPER, context);

            if (fileDir == null) return;

            if (!fileDir.exists()) fileDir.mkdirs();

            File file = new File(fileDir, Const.SCREEN_LOCK_WALLPAPER_THUMBNAIL_BLURRED);

            if (file.exists()) file.delete();

            file.createNewFile();

            DU.sd("lock screen blur", "save to file");

            final ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);

            if (blurredBitmap.compress(Bitmap.CompressFormat.PNG, 90, tmpOut)) {
                is = new ByteArrayInputStream(tmpOut.toByteArray());

                os = new FileOutputStream(file);

                int read;
                byte[] bytes = new byte[1024];

                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        } finally {
            DT.closeSilently(is, os);
        }
    }

    private Bitmap getBlurredBitmapFromFile(final Context context) {
        try {
            File fileDir = WallpaperSlaver.getAvailableStoreDirFile(Const.DIR_ROOT + Const.SCREEN_LOCK_WALLPAPER, context);

            if (fileDir == null) return null;

            File file = new File(fileDir, Const.SCREEN_LOCK_WALLPAPER_THUMBNAIL_BLURRED);

            if (!file.exists()) return null;

            DU.sd("lock screen blur", "get from file");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
        return null;
    }

    private Bitmap sampleBitmap(Context context, int sample) {
        Assert.assertNotNull(context);

        Bitmap sampledBitmap;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = false;
//                option.inPreferredConfig = Bitmap.Config.ARGB_8888;
        option.inSampleSize = sample;
                /*
                * 8 takes about 140ms on my Mi3
                * 4 takes about 280ms on my Mi3
                * 2 takes about 320ms on my Mi3
                * */

        sampledBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.general_bg
                , option
        );
        return sampledBitmap;
    }
}
