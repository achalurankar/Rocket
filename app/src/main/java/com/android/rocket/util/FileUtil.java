package com.android.rocket.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.rocket.modal.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;

public class FileUtil {

    //codes
    public static final int VERSION_MISMATCH = 1;
    public static final int VERSION_MATCHED = 2;
    public static final int NO_RECORD_FOUND = 3;
    private static final int LOCAL_FILE_NOT_FOUND = 4;

    private static final String TAG = "FileUtil";

    public static String getBase64FromUri(Uri mImageUri) throws IOException {
        //uri to file
        File file = new File(mImageUri.getPath());
        //file to byte[]
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        //byte[] to encoded base 64 string
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    public static File getImageFileUserData(Context context, User user) {
        //check record existence and its version in DB
        int fileExistsInDB = fileExistsInDB(user);
        File dir = new File(context.getExternalFilesDir(null), "Profile Pictures");
        File localFile = new File(dir, "Rocket_" + user.getUsername() + "_picture.jpg");
        //if version matched then
        if (fileExistsInDB == VERSION_MATCHED) {
            //if file exists in local storage
            if (dir.isDirectory() && localFile.exists()) {
                //then return the file
                Log.e(TAG, "getImageFileUserData: " + user.getUsername() + " version matched");
                return localFile;
            }
            fileExistsInDB = LOCAL_FILE_NOT_FOUND;
        }
        //else store new file in storage and database
        try {
            byte[] bytes = Base64.getDecoder().decode(user.getPicture()); //get bytes from base64 data
            if (!dir.isDirectory()) //if folder does not exist
                dir.mkdir(); //make directory
            FileOutputStream fos = new FileOutputStream(localFile);
            fos.write(bytes);
            ContentValues rows = new ContentValues();
            rows.put("version", user.getPictureVersion());
            if (fileExistsInDB == NO_RECORD_FOUND) {
                //insert new record
                rows.put("user_id", user.getUserId());
                Session.DbRef.insert(Constants.TABLE_USER_PROFILE_PICTURES, null, rows);
                Log.e(TAG, "getImageFileUserData: " + user.getUsername() + " new record added");
            } else {
                if (fileExistsInDB != LOCAL_FILE_NOT_FOUND) {
                    //update
                    String[] args = new String[]{user.getUserId() + ""};
                    Session.DbRef.update(Constants.TABLE_USER_PROFILE_PICTURES, rows, "user_id = ?", args);
                    Log.e(TAG, "getImageFileUserData: " + user.getUsername() + " version updated");
                } else {
                    Log.e(TAG, "getImageFileUserData: " + user.getUsername() + "picture not found in local but version matched");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localFile;
    }

    private static int fileExistsInDB(User user) {
        Cursor cursor = Session.DbRef.rawQuery(String.format(Locale.ENGLISH,
                "SELECT * from %s WHERE %s = %d;",
                Constants.TABLE_USER_PROFILE_PICTURES,
                Constants.FIELD_USER_ID,
                user.getUserId()
        ), null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            long version = cursor.getLong(1);
            cursor.close();
            if (version == user.getPictureVersion())
                return VERSION_MATCHED;
            else
                return VERSION_MISMATCH;
        } else
            return NO_RECORD_FOUND;
    }
}
