package ru.axel.yamobdev2018.common;

import android.database.Cursor;

public class LoaderResult {
    private Cursor mCursor;
    private int mPage;

    public LoaderResult(Cursor cursor, int page) {
        mCursor = cursor;
        mPage = page;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getPage() {
        return mPage;
    }
}
