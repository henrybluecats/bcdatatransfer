package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCBeacon;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by henrycheng on 19/09/2016.
 */
public class BeaconViewHolder extends RecyclerView.ViewHolder {

    TextView tv_address;
    TextView tv_name;
    TextView tv_sn;
    TextView tv_rssi;
    TextView tv_distance;
    public BeaconViewHolder(View itemView) {
        super(itemView);
        tv_address = (TextView)itemView.findViewById(R.id.tv_address);
        tv_name = (TextView)itemView.findViewById(R.id.tv_name);
        tv_sn = (TextView)itemView.findViewById(R.id.tv_sn);
        tv_rssi = (TextView)itemView.findViewById(R.id.tv_rssi);
        tv_distance = (TextView)itemView.findViewById(R.id.tv_distance);
    }
    public void populate(BCBeacon beacon) {
        tv_address.setText(beacon.getPeripheralIdentifier());
        tv_name.setText(beacon.getName());
        tv_sn.setText(beacon.getSerialNumber());
        tv_rssi.setText(beacon.getRSSI().toString());
        tv_distance.setText(String.format("%.2f", beacon.getAccuracy()));
    }
}
