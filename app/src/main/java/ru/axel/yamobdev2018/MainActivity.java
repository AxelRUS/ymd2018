package ru.axel.yamobdev2018;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import retrofit2.Response;
import ru.axel.yamobdev2018.common.CachedResponse;
import ru.axel.yamobdev2018.common.LoaderResult;
import ru.axel.yamobdev2018.data.ImageContract;
import ru.axel.yamobdev2018.imgurmodel.Gallery;
import ru.axel.yamobdev2018.utils.Utils;

public class MainActivity extends AppCompatActivity
        implements ImageAdapter.ImageAdapterOnClickListener {

    private static final String TAG = AppCompatActivity.class.getSimpleName();
    public static final int SPAN_COUNT = 3;
    public static final int IMAGE_LOADER_ID = 1;
    private static final String ARG_PAGE = "page";
    private long queryId = 0;
    private RecyclerView mGridRecyclerView;
    private ImageAdapter mImageAdapter;
    private EndlessRecyclerViewScrollListener mScrollListener;

    LoaderManager.LoaderCallbacks<LoaderResult> getImages = new LoaderManager.LoaderCallbacks<LoaderResult>() {
        @Override
        public Loader<LoaderResult> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<LoaderResult>(MainActivity.this) {
                LoaderResult mLoaderResult = null;

                @Override
                protected void onStartLoading() {
                    if (mLoaderResult != null) {
                        deliverResult(mLoaderResult);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public LoaderResult loadInBackground() {
                    ContentResolver contentResolver = getContentResolver();
                    int page = args.getInt(ARG_PAGE, 1);

                    if (queryId == 0) {
                        contentResolver.delete(ImageContract.QueryEntry.CONTENT_URI,
                                ImageContract.QueryEntry.COLUMN_DATE + " <= datetime('now', '-5 minutes', 'localtime')",
                                null);

                        CachedResponse cachedResponse = Utils.getLastResponse(contentResolver);
                        queryId = cachedResponse.getQueryId();

                        if (queryId == 0) {
                            Response<Gallery> resp = Utils.galleryRequest(page);
                            if (resp.isSuccessful()) {
                                Gallery gallery = resp.body();
                                queryId = Utils.saveResponse(contentResolver, gallery, queryId, "", page);
                            }
                        } else {
                            page = cachedResponse.getPage();
                            Log.d(TAG, "loadInBackground: get page from cache " + page);
                        }
                    } else {
                        CachedResponse cachedResponse = Utils.getResponseById(contentResolver, queryId);
                        if (page > cachedResponse.getPage()) {
                            Response<Gallery> resp = Utils.galleryRequest(page);
                            if (resp.isSuccessful()) {
                                Gallery gallery = resp.body();
                                queryId = Utils.saveResponse(contentResolver, gallery, queryId, "", page);
                            }
                        }
                    }

                    Cursor imageCursor = contentResolver.query(ImageContract.ImageEntry.CONTENT_URI,
                            new String[]{ImageContract.ImageEntry._ID, ImageContract.ImageEntry.COLUMN_URL},
                            ImageContract.ImageEntry.COLUMN_QUERY_ID + "=?",
                            new String[]{String.valueOf(queryId)},
                            ImageContract.ImageEntry._ID);

                    LoaderResult loaderResult = new LoaderResult(imageCursor, page);

                    return loaderResult;
                }

                @Override
                public void deliverResult(LoaderResult data) {
                    mLoaderResult = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<LoaderResult> loader, LoaderResult data) {
            mImageAdapter.swapCursor(data.getCursor());
            mScrollListener.setCurrentPage(data.getPage());
            Log.d(TAG, "onLoadFinished: set page: " + data.getPage());
        }

        @Override
        public void onLoaderReset(Loader<LoaderResult> loader) {
            mImageAdapter.swapCursor(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridRecyclerView = findViewById(R.id.gridRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        mGridRecyclerView.setLayoutManager(gridLayoutManager);
        mGridRecyclerView.setHasFixedSize(true);
        mImageAdapter = new ImageAdapter(this);
        mGridRecyclerView.setAdapter(mImageAdapter);

        mScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        mGridRecyclerView.addOnScrollListener(mScrollListener);

        Bundle bundle = new Bundle();
        getSupportLoaderManager().initLoader(IMAGE_LOADER_ID, bundle, getImages);
    }

    @Override
    public void onClick(String url) {
        Intent imageViewIntent = new Intent(this, ImageViewActivity.class);
        imageViewIntent.putExtra(ImageViewActivity.KEY_URL, url);
        startActivity(imageViewIntent);
    }

    public void loadNextDataFromApi(int offset) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE, offset);
        getSupportLoaderManager().restartLoader(IMAGE_LOADER_ID, bundle, getImages);
    }
}
