package ru.axel.yamobdev2018.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ru.axel.yamobdev2018.ImgurApi;
import ru.axel.yamobdev2018.common.CachedResponse;
import ru.axel.yamobdev2018.data.ImageContract;
import ru.axel.yamobdev2018.imgurmodel.Datum;
import ru.axel.yamobdev2018.imgurmodel.Gallery;
import ru.axel.yamobdev2018.imgurmodel.Image;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static Response<Gallery> galleryRequest(int page) {
        ImgurApi imgurApi = ServiceGenerator.createService(ImgurApi.class);
        try {

            return imgurApi
                    .getGallery("top", "", "", page)
                    .execute();
        } catch (IOException e) {
            Log.e(TAG, "galleryRequest: request error", e);
            return null;
        }

    }

    /**
     * Parse reponse to ContentValues array with image urls
     * @param gallery
     * @param queryID
     * @return
     */
    public static ContentValues[] parseImages(Gallery gallery, long queryID) {
        ContentValues[] bulkToInsert;
        List<ContentValues> valueList = new ArrayList<>();

        for (Datum datum : gallery.getData()) {
            for (Image image : datum.getImages()) {
                if (image.getType().equals("image/jpeg") ||
                        image.getType().equals("image/png")) {
                    ContentValues newValues = new ContentValues();
                    newValues.put(ImageContract.ImageEntry.COLUMN_QUERY_ID, queryID);
                    newValues.put(ImageContract.ImageEntry.COLUMN_URL, image.getLink());
                    valueList.add(newValues);
                }
            }
        }
        bulkToInsert = new ContentValues[valueList.size()];
        valueList.toArray(bulkToInsert);

        return bulkToInsert;
    }

    private static CachedResponse getResponse(ContentResolver contentResolver, String selection, String[] selectionArgs, String sortOrder) {
        long queryId = 0;
        int page = 0;

        Cursor lastQueryCursor = contentResolver.query(ImageContract.QueryEntry.CONTENT_URI,
                new String[]{ImageContract.QueryEntry._ID, ImageContract.QueryEntry.COLUMN_PAGE},
                selection,
                selectionArgs,
                sortOrder);

        if (lastQueryCursor != null) {
            if (lastQueryCursor.moveToFirst()) {
                queryId = lastQueryCursor.getLong(lastQueryCursor.getColumnIndex(ImageContract.QueryEntry._ID));
                page = lastQueryCursor.getInt(lastQueryCursor.getColumnIndex(ImageContract.QueryEntry.COLUMN_PAGE));
            }

            lastQueryCursor.close();
        }

        return new CachedResponse(queryId, page);
    }

    public static CachedResponse getLastResponse(ContentResolver contentResolver) {
        return getResponse(contentResolver,
                ImageContract.QueryEntry.COLUMN_DATE + " > datetime('now', '-5 minutes', 'localtime')",
                null,
                ImageContract.QueryEntry.COLUMN_DATE + " DESC");
    }

    public static CachedResponse getResponseById(ContentResolver contentResolver, long id) {
        return getResponse(contentResolver,
                ImageContract.QueryEntry._ID + "=?",
                new String[]{String.valueOf(id)},
                null);
    }

    /**
     * Save request parameters and image urls from response
     * @param contentResolver
     * @param gallery
     * @param queryID
     * @param keyword
     * @param page
     * @return
     */
    public static long saveResponse(ContentResolver contentResolver, Gallery gallery, long queryID, String keyword, int page) {
        ContentValues queryCV = new ContentValues();
        queryCV.put(ImageContract.QueryEntry.COLUMN_PAGE, page);

        if (queryID == 0) {
            queryCV.put(ImageContract.QueryEntry.COLUMN_KEYWORD, keyword);
            Uri newQueryUri = contentResolver.insert(ImageContract.QueryEntry.CONTENT_URI,
                    queryCV);
            queryID = ContentUris.parseId(newQueryUri);
        } else {
            Log.d(TAG, "saveResponse: set page in db "+ page);
            contentResolver.update(ImageContract.QueryEntry.CONTENT_URI,
                    queryCV, ImageContract.QueryEntry._ID + "=?", new String[]{String.valueOf(queryID)});
        }

        ContentValues[] bulkToInsert = Utils.parseImages(gallery, queryID);
        contentResolver.bulkInsert(ImageContract.ImageEntry.CONTENT_URI, bulkToInsert);

        return queryID;
    }
}
