package com.android.rocket.util;

import android.content.Context;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    public static String getBase64FromUri(Uri mImageUri) throws IOException {
        //uri to file
        File file = new File(mImageUri.getPath());
        //file to byte[]
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        //byte[] to encoded base 64 string
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static File getFileFromBase64(Context context, String picture) {
        File pictureFile = null;
        try {
            byte[] bytes = Base64.getDecoder().decode(picture);
            File folder = new File(context.getExternalFilesDir(null), "Images");
            if (!folder.isDirectory()) //if folder does not exist
                folder.mkdir(); //make directory
            pictureFile = new File(folder, "Rocket_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) +
                    ".jpg");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pictureFile;
    }
}
