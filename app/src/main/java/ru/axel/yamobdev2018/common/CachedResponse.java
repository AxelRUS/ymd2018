package ru.axel.yamobdev2018.common;

public class CachedResponse {
    long mQueryId;
    int mPage;

    public CachedResponse(long queryId, int page) {
        this.mQueryId = queryId;
        this.mPage = page;
    }

    public long getQueryId() {
        return mQueryId;
    }

    public int getPage() {
        return mPage;
    }
}
