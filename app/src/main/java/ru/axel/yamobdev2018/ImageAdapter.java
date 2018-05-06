package ru.axel.yamobdev2018;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ru.axel.yamobdev2018.data.ImageContract;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private int mUrlColumnIndex;

    final private ImageAdapterOnClickListener mOnClickListener;

    public interface ImageAdapterOnClickListener {
        void onClick(String url);
    }

    public ImageAdapter(ImageAdapterOnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.image_list_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        Picasso.with(mContext)
                .load(mCursor.getString(mUrlColumnIndex))
                .fit()
                .centerCrop()
                .into(holder.mImageView);
    }

    // todo: что тут вообще происходит?
    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }

        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            mUrlColumnIndex = mCursor.getColumnIndex(ImageContract.ImageEntry.COLUMN_URL);
            notifyDataSetChanged();
        }

        return temp;
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCursor.moveToPosition(getAdapterPosition());
            final int urlColumnIndex = mCursor.getColumnIndex(ImageContract.ImageEntry.COLUMN_URL);
            String url = mCursor.getString(urlColumnIndex);
            mOnClickListener.onClick(url);
        }
    }
}
