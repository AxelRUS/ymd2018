package ru.axel.yamobdev2018.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ImageContentProvider extends ContentProvider {

    public static final int QUERIES = 100;
    public static final int QUERY_WITH_ID = 101;
    public static final int IMAGES = 102;
    public static final int IMAGE_WITH_ID = 103;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    private DBHelper mDBHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ImageContract.AUTHORITY, ImageContract.PATH_QUERIES, QUERIES);
        uriMatcher.addURI(ImageContract.AUTHORITY, ImageContract.PATH_QUERIES + "/#", QUERY_WITH_ID);
        uriMatcher.addURI(ImageContract.AUTHORITY, ImageContract.PATH_IMAGES, IMAGES);
        uriMatcher.addURI(ImageContract.AUTHORITY, ImageContract.PATH_IMAGES + "/#", IMAGE_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDBHelper = new DBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case QUERIES:
                tableName = ImageContract.QueryEntry.TABLE_NAME;
                break;
            case IMAGES:
                tableName = ImageContract.ImageEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor returnCursor = db.query(tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri returnUri;
        long id;

        switch (sUriMatcher.match(uri)) {
            case QUERIES:
                id = db.insert(ImageContract.QueryEntry.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(ImageContract.QueryEntry.CONTENT_URI, id);
                break;
            case IMAGES:
                id = db.insert(ImageContract.ImageEntry.TABLE_NAME, null, values);
                returnUri = ContentUris.withAppendedId(ImageContract.ImageEntry.CONTENT_URI, id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (id <= 0) {
            throw new SQLiteException("Failed to insert row into: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;

    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case IMAGES:
                int numInserted = 0;

                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        db.insert(ImageContract.ImageEntry.TABLE_NAME,
                                null,
                                value);
                    }
                    numInserted = values.length;
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return numInserted;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();

        int itemDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case QUERIES:
                db.beginTransaction();
                try {
                    Cursor queriesCursor = db.query(ImageContract.QueryEntry.TABLE_NAME,
                            new String[]{"_id"},
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null);

                    if (queriesCursor == null) {
                        return 0;
                    }

                    final int queryIdIndex = queriesCursor.getColumnIndex(ImageContract.QueryEntry._ID);
                    while (queriesCursor.moveToNext()) {
                        db.delete(ImageContract.ImageEntry.TABLE_NAME,
                                ImageContract.ImageEntry.COLUMN_QUERY_ID + "=?",
                                new String[]{queriesCursor.getString(queryIdIndex)});
                    }
                    queriesCursor.close();

                    itemDeleted = db.delete(ImageContract.QueryEntry.TABLE_NAME,
                            selection,
                            selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return itemDeleted;
            case QUERY_WITH_ID:
                long id = ContentUris.parseId(uri);
                db.beginTransaction();
                try {
                    db.delete(ImageContract.ImageEntry.TABLE_NAME,
                            ImageContract.ImageEntry.COLUMN_QUERY_ID + "=?",
                            new String[]{String.valueOf(id)});
                    itemDeleted = db.delete(ImageContract.QueryEntry.TABLE_NAME,
                            ImageContract.QueryEntry._ID + "=?",
                            new String[]{String.valueOf(id)});
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return itemDeleted;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
