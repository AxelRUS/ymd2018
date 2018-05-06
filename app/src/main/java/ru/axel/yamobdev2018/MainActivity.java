package ru.axel.yamobdev2018;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ru.axel.yamobdev2018.data.ImageContract;
import ru.axel.yamobdev2018.imgurmodel.Datum;
import ru.axel.yamobdev2018.imgurmodel.Gallery;
import ru.axel.yamobdev2018.imgurmodel.Image;
import ru.axel.yamobdev2018.utils.ServiceGenerator;

public class MainActivity extends AppCompatActivity
        implements ImageAdapter.ImageAdapterOnClickListener {

    private static final String TAG = AppCompatActivity.class.getSimpleName();
    public static final int SPAN_COUNT = 3;
    public static final int IMAGE_LOADER_ID = 1;
    private RecyclerView mGridRecyclerView;
    private ImageAdapter mImageAdapter;

    LoaderManager.LoaderCallbacks<Cursor> getImages = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(MainActivity.this) {
                Cursor mImageCursor = null;

                @Override
                protected void onStartLoading() {
                    if (mImageCursor != null) {
                        deliverResult(mImageCursor);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    ImgurApi imgurApi = ServiceGenerator.createService(ImgurApi.class);

                    Response<Gallery> resp;
                    try {
                        resp = imgurApi.searchGallery("top", "", 1, "cats")
                                .execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // todo: сообщение пользователю
                        return null;
                    }

                    if (resp.isSuccessful()) {

                        ContentResolver contentResolver = getContentResolver();
                        contentResolver.delete(ImageContract.QueryEntry.CONTENT_URI,
                                ImageContract.QueryEntry.COLUMN_DATE + " >= date('now','-5 minutes')",
                                null);

                        Gallery gallery = resp.body();

                        ContentValues queryCV = new ContentValues();
                        queryCV.put(ImageContract.QueryEntry.COLUMN_KEYWORD, "");
                        Uri newQueryUri = contentResolver.insert(ImageContract.QueryEntry.CONTENT_URI,
                                queryCV);

                        long newQueryID = ContentUris.parseId(newQueryUri);

                        if (newQueryID == -1) {
                            Log.d(TAG, "loadInBackground: query insert error");
                            return null;
                        }

                        ContentValues[] bulkToInsert;
                        List<ContentValues> valueList = new ArrayList<>();

                        for (Datum datum : gallery.getData()) {
                            for (Image image : datum.getImages()) {
                                if (image.getType().equals("image/jpeg") ||
                                        image.getType().equals("image/png")) {
                                    ContentValues newValues = new ContentValues();
                                    newValues.put(ImageContract.ImageEntry.COLUMN_QUERY_ID, newQueryID);
                                    newValues.put(ImageContract.ImageEntry.COLUMN_URL, image.getLink());
                                    valueList.add(newValues);
                                }
                            }
                        }

                        bulkToInsert = new ContentValues[valueList.size()];
                        valueList.toArray(bulkToInsert);

                        contentResolver.bulkInsert(ImageContract.ImageEntry.CONTENT_URI, bulkToInsert);

                        return contentResolver.query(ImageContract.ImageEntry.CONTENT_URI,
                                new String[]{ImageContract.ImageEntry._ID, ImageContract.ImageEntry.COLUMN_URL},
                                ImageContract.ImageEntry.COLUMN_QUERY_ID + "=?",
                                new String[]{String.valueOf(newQueryID)},
                                ImageContract.ImageEntry._ID);
                    } else {
                        Log.e(TAG, "reqiest error: " + resp.errorBody());
                    }
                    return null;
                }

                @Override
                public void deliverResult(Cursor data) {
                    mImageCursor = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mImageAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mImageAdapter.swapCursor(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridRecyclerView = findViewById(R.id.gridRecyclerView);
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mGridRecyclerView.setLayoutManager(gridLayoutManager);
        mGridRecyclerView.setHasFixedSize(true);
        mImageAdapter = new ImageAdapter(this);
        mGridRecyclerView.setAdapter(mImageAdapter);

        getSupportLoaderManager().initLoader(IMAGE_LOADER_ID, null, getImages);
    }

    @Override
    public void onClick(String url) {
        Intent imageViewIntent = new Intent(this, ImageViewActivity.class);
        imageViewIntent.putExtra(ImageViewActivity.KEY_URL, url);
        startActivity(imageViewIntent);
    }
}
