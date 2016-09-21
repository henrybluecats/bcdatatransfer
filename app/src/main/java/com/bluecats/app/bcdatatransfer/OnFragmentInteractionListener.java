package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCBeacon;

/**
 * Created by henrycheng on 20/09/2016.
 */
public interface OnFragmentInteractionListener {
    BCBeacon getBeacon();
    void startSending();
    void stopSending();
}