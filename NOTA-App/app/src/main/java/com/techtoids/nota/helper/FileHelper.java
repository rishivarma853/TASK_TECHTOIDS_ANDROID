package com.techtoids.nota.helper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.util.Date;

public class FileHelper {
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public static String getNewFileName(Context context, Uri file) {
        String originalFilename = getFileName(context, file);
        String[] fileArr = originalFilename.split("\\.");
        String fileExtension = fileArr[fileArr.length - 1];

        String timeStamp = String.valueOf(new Date().getTime());

        return timeStamp + "." + fileExtension;
    }

    private static String getMimeTypeFromUri(Uri uri) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

    public static Intent getSelectDocumentIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.ms-excel",
                "application/vnd.ms-powerpoint",
                "text/plain"
        });
        return intent;
    }

    public static Intent getOpenDocumentIntent(Uri documentUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(documentUri, getMimeTypeFromUri(documentUri));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}
