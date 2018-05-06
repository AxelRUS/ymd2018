package ru.axel.yamobdev2018.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class ImageContract {

    public static final String AUTHORITY = "ru.axel.yamobdev2018";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_IMAGES = "images";
    public static final String PATH_QUERIES = "queries";

    public static final class QueryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_QUERIES)
                .build();

        public static final String TABLE_NAME = "queries";

        public static final String COLUMN_KEYWORD = "keyword";
        public static final String COLUMN_DATE = "date";
    }

    public static final class ImageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_IMAGES)
                .build();

        public static final String TABLE_NAME = "images";

        public static final String COLUMN_QUERY_ID = "query_id";
        public static final String COLUMN_URL = "url";
    }
}
