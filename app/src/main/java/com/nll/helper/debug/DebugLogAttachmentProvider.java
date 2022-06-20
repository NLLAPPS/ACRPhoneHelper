package com.nll.helper.debug;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;

import com.nll.helper.recorder.CLog;

import java.io.File;
import java.io.FileNotFoundException;

public class DebugLogAttachmentProvider extends ContentProvider {
    public static final int cache_code = 2;
    public static final String CACHE_LOG_PATH = "logs";
    private static final String TAG = "DebugLogAttachmentProvider";
    //TODO This is AUTHORITY must match the package id. We need to make (com.nll.cb) same as package name if we ever use differnet package name
    private static final String AUTHORITY = "com.nll.helper.debug.DebugLogAttachmentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private UriMatcher uriMatcher;


    public static Uri getAttachmentUri(boolean useFileUri, File file) {
        Uri uri;
        if (useFileUri) {
            uri = Uri.fromFile(file);
            if (CLog.INSTANCE.isDebug()) {
                CLog.INSTANCE.log(TAG, "Attachment URI is: " + uri);
            }
        } else {
            uri = DebugLogAttachmentProvider.CONTENT_URI.buildUpon().appendPath(DebugLogAttachmentProvider.CACHE_LOG_PATH).appendPath(file.getName()).build();

            if (CLog.INSTANCE.isDebug()) {
                CLog.INSTANCE.log(TAG, "Attachment URI is: " + uri);
            }
        }
        return uri;

    }

    public static File getLogPath(Context context) {
        File root = new File(context.getExternalCacheDir(), "/" + CACHE_LOG_PATH + "/");
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;

    }

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CACHE_LOG_PATH + "/*", cache_code);

        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return "application/zip";

    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (CLog.INSTANCE.isDebug()) {
            CLog.INSTANCE.log(TAG, "Called with uri: " + uri);
        }


        // Check incoming Uri against the matcher
        switch (uriMatcher.match(uri)) {
            case cache_code:
                if (CLog.INSTANCE.isDebug()) {
                    CLog.INSTANCE.log(TAG, "File to open is : " + uri.getLastPathSegment());}
                //return openAttachment(uri.getLastPathSegment());
                // Create & return a ParcelFileDescriptor pointing to the file
                // Note: I don't care what mode they ask for - they're only getting
                // read only
                return ParcelFileDescriptor.open(getLogFileUri(uri), ParcelFileDescriptor.MODE_READ_ONLY);


            // Otherwise unrecognised Uri
            default:
                if (CLog.INSTANCE.isDebug()) {
                    CLog.INSTANCE.log(TAG, "Unsupported uri: '" + uri);
                }
                throw new FileNotFoundException("Unsupported uri: " + uri.toString());
        }


    }

    private File getLogFileUri(Uri uri) {
        File root = getLogPath(getContext());
        File file = new File(root, uri.getLastPathSegment());
        if (CLog.INSTANCE.isDebug()) {
            CLog.INSTANCE.log(TAG, "getLogFileUri " + file.getAbsolutePath());
        }
        return file;
    }

    private File getFileForUri(Uri uri) {
        return new File(getLogPath(getContext()), uri.getLastPathSegment());
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final File file = getFileForUri(uri);
        if (CLog.INSTANCE.isDebug()) {
            CLog.INSTANCE.log(TAG, "query file is " + file.getAbsolutePath());
        }
        if (projection == null) {
            projection = new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = file.getName();
            } else if (OpenableColumns.SIZE.equals(col)) {
                cols[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            }
        }


        cols = copyOf(cols, i);
        values = copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }
    // //////////////////////////////////////////////////////////////
    // Not supported / used / required
    // //////////////////////////////////////////////////////////////

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }


}
