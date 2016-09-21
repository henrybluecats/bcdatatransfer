package com.bluecats.app.bcdatatransfer;

import com.google.gson.Gson;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconCallback;
import com.bluecats.sdk.BCBeaconCommandCallback;
import com.bluecats.sdk.BCBeaconInsights;
import com.bluecats.sdk.BCBeaconLoudness;
import com.bluecats.sdk.BCBeaconMode;
import com.bluecats.sdk.BCBeaconRegion;
import com.bluecats.sdk.BCBeaconVersion;
import com.bluecats.sdk.BCError;
import com.bluecats.sdk.BCTargetSpeed;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;


public class SendtextFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SendtextFragment";


    protected OnFragmentInteractionListener mListener;

    public SendtextFragment() {
        // Required empty public constructor
    }

    public static SendtextFragment newInstance() {
        SendtextFragment fragment = new SendtextFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Button btn_send;
    CheckBox cb_raw;
    CheckBox cb_clear;
    TextView tv_response;
    EditText et_text;
    ProgressBar pb_waiting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sendtext, container, false);
        btn_send = (Button)view.findViewById(R.id.btn_send);
        cb_raw = (CheckBox)view.findViewById(R.id.cb_raw);
        cb_clear = (CheckBox)view.findViewById(R.id.cb_clear);
        tv_response = (TextView)view.findViewById(R.id.tv_response);
        et_text = (EditText)view.findViewById(R.id.et_text);

        tv_response.setMovementMethod(new ScrollingMovementMethod());
        btn_send.setOnClickListener(this);

        pb_waiting = (ProgressBar)view.findViewById(R.id.pb_waiting);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send) {
            if (mListener.getBeacon() == null) {
                Snackbar.make(btn_send, "Beacon is null", Snackbar.LENGTH_LONG).show();
                return;
            }
            String text = et_text.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Snackbar.make(btn_send, "Please type in edit box", Snackbar.LENGTH_LONG).show();
                return;
            }
            if(cb_clear.isChecked()) {
                tv_response.setText("");
            }
            byte[] data = text.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(data.length +1);
            buffer.put(cb_raw.isChecked()?Utils.DATA_TYPE_RAW:Utils.DATA_TYPE_ASCII_TEXT);
            buffer.put(data);
            enableUi(false);
            tv_response.append("Sending:\n");
            tv_response.append(text);
            tv_response.append("\n\n");

            Utils.transportDataArray(mListener.getBeacon(), buffer, mBeaconCallback);
        }
    }

    protected void enableUi(boolean b) {
        btn_send.setEnabled(b);
        cb_clear.setEnabled(b);
        cb_raw.setEnabled(b);
        et_text.setEnabled(b);
        pb_waiting.setVisibility(b?View.GONE:View.VISIBLE);
        if (b) {
            mListener.stopSending();
            pb_waiting.setVisibility(View.GONE);
            pb_waiting.setProgress(0);
        } else {
            mListener.startSending();
            pb_waiting.setVisibility(View.VISIBLE);
        }
    }

    protected BCBeaconCommandCallback mBeaconCallback = new BCBeaconCommandCallback() {

        @Override
        public void onDidComplete(final BCError error) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCommandCompleted(error);
                    }
                });
            }

        }

        @Override
        public void onDidUpdateProgress(int type, final int percent, final String status) {
            if (type != BCBeaconCommandCallback.PROGRESS_TYPE_DATA_TX && type != BCBeaconCommandCallback.PROGRESS_TYPE_DATA_RX) {
                return;
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCommandProgressChanged(percent);
                    }
                });
            }
        }

        @Override
        public void onDidUpdateStatus() {

        }

        @Override
        public void onDidResponseData(final List<ByteBuffer> resp) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onCommandResponsed(resp);
                    }
                });
            }

        }
    };

    protected void onCommandResponsed(List<ByteBuffer> resp) {
        for (ByteBuffer bb: resp) {
            byte[] data = bb.array();
            if (data[0] == Utils.DATA_TYPE_RAW) {
                onRawResponsed(data);
            } else if (data[0] == Utils.DATA_TYPE_ASCII_TEXT) {
                onTextResponsed(data);
            } else if (data[0] == Utils.DATA_TYPE_FILE) {
                onFileResponsed(data);
            }
        }
        enableUi(true);
    }

    protected void onFileResponsed(byte[] data) {
        int fileNameLen = 0x000000ff & data[1];
        String fileName = new String(data, 2, fileNameLen);
        Log.d(TAG, "filenameLen:"+fileNameLen+", "+fileName);
        int offset = 1 + 1 + fileNameLen;
        int fileLen = ((data[offset + 3]&0x000000ff) << 24)|((data[offset + 2]&0x000000ff) << 16)|((data[offset + 1]&0x000000ff) << 8)|(data[offset]&0x000000ff);
        offset += 4;
        try {
            FileOutputStream fos = new FileOutputStream("/sdcard/"+fileName);
            fos.write(data, offset, fileLen);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv_response.append("File saved in /sdcard/"+fileName);
        tv_response.append("\n\n");
    }

    protected void onTextResponsed(byte[] data) {
        tv_response.append("Response in text:\n");
        tv_response.append(new String(data, 1, data.length - 1));
        tv_response.append("\n\n");
    }

    protected void onRawResponsed(byte[] data) {
        tv_response.append("Response in raw data:\n");
        tv_response.append("[");
        tv_response.append(new Gson().toJson(data));
        tv_response.append("]\n");
    }

    protected void onCommandProgressChanged(int percent) {
        pb_waiting.setProgress(percent);
    }

    protected void onCommandCompleted(BCError error) {
        if(error != null) {
            if (error.getStatusCode() == 0) {
                tv_response.append("Transfer success!\n\n\n\n");
            } else {
                tv_response.append("Error:");
                tv_response.append(error.getMessage());
                tv_response.append("\n");
            }
        }
        enableUi(true);
    }
}
