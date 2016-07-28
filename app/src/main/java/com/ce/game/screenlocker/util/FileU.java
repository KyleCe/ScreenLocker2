package com.ce.game.screenlocker.util;

import java.io.File;
import java.io.IOException;

/**
 * Created by KyleCe on 2016/7/25.
 *
 * @author: KyleCe
 */
public class FileU {

    public static void renameFile(String oldName, String newName) throws IOException {
        File srcFile = new File(oldName);
        boolean bSucceeded = false;
        try {
            File destFile = new File(newName);
            if (destFile.exists()) {
                if (!destFile.delete()) {
                    throw new IOException(oldName + " was not successfully renamed to " + newName);
                }
            }
            if (!srcFile.renameTo(destFile))        {
                throw new IOException(oldName + " was not successfully renamed to " + newName);
            } else {
                bSucceeded = true;
            }
        } finally {
            if (bSucceeded) {
                srcFile.delete();
            }
        }
    }
}
