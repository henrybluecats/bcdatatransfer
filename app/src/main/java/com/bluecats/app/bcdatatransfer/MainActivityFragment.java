package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BeaconAdapter mBeaconAdapter;
    private BCBeaconManager mBeaconManager;
    public MainActivityFragment() {
        mBeaconManager = new BCBeaconManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.rv_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mBeaconAdapter = new BeaconAdapter(getContext(), new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int pos = mRecyclerView.getChildAdapterPosition(view);
                startTransportActivity(pos);
            }
        });
        mRecyclerView.setAdapter(mBeaconAdapter);

        mBeaconManager.registerCallback(mBeaconManagerCallback);
        return view;
    }

    private void startTransportActivity(int pos) {
        BCBeacon beacon = mBeaconAdapter.getItem(pos);
        Bundle data = new Bundle();
//        data.putString("dfdfdfdfdfd", "jkjkjkjkjkjk");
        data.putParcelable(Utils.EXTRA_APP_BEACON, beacon);
        Intent intent = new Intent(getContext(), TransportActivity.class);
        intent.putExtras(data);
//        intent.putExtra(Utils.EXTRA_APP_BEACON, beacon.copy());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        mBeaconManager.unregisterCallback(mBeaconManagerCallback);
        super.onDestroyView();
    }

    private int findItemInAdapter(BCBeacon beacon) {
        int pos = -1;
        for (int i = 0; i < mBeaconAdapter.getItemCount(); i++) {
            BCBeacon item = mBeaconAdapter.getItem(i);
            if (item.getBeaconID().equalsIgnoreCase(beacon.getBeaconID())) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {
        @Override
        public void didRangeBeacons(List<BCBeacon> beacons) {
            for (BCBeacon beacon : beacons) {
                mBeaconAdapter.replaceItem(findItemInAdapter(beacon), beacon);
            }
        }
    };
}
