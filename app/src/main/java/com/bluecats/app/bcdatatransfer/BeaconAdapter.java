package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCBeacon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by henrycheng on 19/09/2016.
 */
public class BeaconAdapter extends RecyclerView.Adapter<BeaconViewHolder> {

    private Context mContext;
    private View.OnClickListener mListener;
    private LayoutInflater mInflater;
    private List<BCBeacon> mData;
    public BeaconAdapter(Context ctx, View.OnClickListener l ) {
        mContext = ctx;
        mListener = l;
        mInflater = LayoutInflater.from(ctx);
        mData = new ArrayList<>();
    }
    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_beacon, parent, false);
        if (mListener != null) {
            view.setOnClickListener(mListener);
        }

        BeaconViewHolder vh = new BeaconViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(BeaconViewHolder holder, int position) {
        if (position >= mData.size()) {
            return;
        }
        BCBeacon item = mData.get(position);
        holder.populate(item);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addItem(BCBeacon beacon) {
        mData.add(beacon);
        notifyDataSetChanged();
    }

    public void replaceItem(int pos, BCBeacon beacon) {
        if (pos == -1) {
            mData.add(beacon);
        } else {
            mData.set(pos, beacon);
        }
        notifyDataSetChanged();
    }
    public BCBeacon getItem(int postion) {
        if (postion >= mData.size()) {
            return null;
        }
        return mData.get(postion);
    }
}
