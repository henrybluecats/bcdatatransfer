package com.bluecats.app.bcdatatransfer;

import com.google.gson.Gson;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconCommandCallback;
import com.bluecats.sdk.BCError;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class SendfileFragment extends Fragment implements View.OnClickListener {
    private static final int REQUEST_CHOOSE_FILE = 0x112;
    private static final String TAG = "SendfileFragment";

    private OnFragmentInteractionListener mListener;
    private String mFilePath;

    public SendfileFragment() {
        // Required empty public constructor
    }

    public static SendfileFragment newInstance() {
        SendfileFragment fragment = new SendfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Button btn_send;
    Button btn_browse;
    TextView tv_response;
    EditText et_text;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sendfile, container, false);
        btn_send = (Button)view.findViewById(R.id.btn_send);
        btn_browse = (Button)view.findViewById(R.id.btn_browse);
        tv_response = (TextView)view.findViewById(R.id.tv_response);
        et_text = (EditText)view.findViewById(R.id.et_text);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);

        tv_response.setMovementMethod(new ScrollingMovementMethod());
        btn_send.setOnClickListener(this);
        btn_browse.setOnClickListener(this);

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
                Snackbar.make(btn_send, "Please select a file", Snackbar.LENGTH_LONG).show();
                return;
            }
            File file = new File(mFilePath);
            if (!file.exists()) {
                Snackbar.make(btn_send, "The file does not exist. Please select again.", Snackbar.LENGTH_LONG).show();
                return;
            }

            byte[] data = text.getBytes();
            if (data.length > 0xff) {
                Snackbar.make(btn_send, "The file name is too long. Please select again.", Snackbar.LENGTH_LONG).show();
                return;
            }

            tv_response.setText("");
            enableUi(false);
            tv_response.append("resizing image...\n");
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mFilePath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = 1;
            scaleFactor = Math.min(photoW / 100, photoH / 100);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            Bitmap image = BitmapFactory.decodeFile(mFilePath, bmOptions);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 70, baos);

            int fileLen = baos.size();

            int bufferLen = 1 + 1 + data.length + 4 + (fileLen);
            ByteBuffer buffer = ByteBuffer.allocate(bufferLen);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(Utils.DATA_TYPE_FILE);
            buffer.put((byte)(data.length));
            buffer.put(data);
            buffer.putInt(fileLen);
            buffer.put(baos.toByteArray());
            image.recycle();

            tv_response.append("Sending File("+fileLen+"bytes):\n");
            tv_response.append("Buffer length "+bufferLen+"bytes\n");


            tv_response.append(text);
            tv_response.append("\n\n");

            Utils.transportDataArray(mListener.getBeacon(), buffer, mBeaconCallback);
        } else if (view.getId() == R.id.btn_browse) {
            showFileChooser();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");

        try {
            startActivityForResult(Intent.createChooser(intent, "Choose a file to send"), REQUEST_CHOOSE_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Please install a File Manager", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_FILE && resultCode == Activity.RESULT_OK) {
            if ("content".equalsIgnoreCase(data.getData().getScheme())) {
                String[] projection = { "_data" };
                Cursor cursor = null;

                try {
                    cursor = getContext().getContentResolver().query(data.getData(), projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        mFilePath =  cursor.getString(column_index);
                        updateInputbox();
                    }
                } catch (Exception e) {
                    // Eat it
                }
            } else if ("file".equalsIgnoreCase(data.getData().getScheme())) {
                mFilePath = data.getData().getPath();
                updateInputbox();
            }
        }
    }

    private void updateInputbox() {
        if (TextUtils.isEmpty(mFilePath)) {
            Toast.makeText(getContext(), "Choose a file to send", Toast.LENGTH_LONG).show();
            return;
        }
        int pos = mFilePath.lastIndexOf('/');
        if (pos < 0) {
            et_text.setText(mFilePath);
        } else {
            et_text.setText(mFilePath.substring(pos+1));
        }
    }

    private void enableUi(boolean b) {
        btn_send.setEnabled(b);
        btn_browse.setEnabled(b);
        if (b) {
            mListener.stopSending();
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        } else {
            mListener.startSending();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private BCBeaconCommandCallback mBeaconCallback = new BCBeaconCommandCallback() {

        @Override
        public void onDidComplete(final BCError error) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });
            }

        }

        @Override
        public void onDidUpdateProgress(int type, final int percent, final String status) {
            if (type == BCBeaconCommandCallback.PROGRESS_TYPE_DATA_TX) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(percent);
                        }
                    });
                }
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
                        for (ByteBuffer bb: resp) {
                            byte[] data = bb.array();
                            if (data[0] == Utils.DATA_TYPE_RAW) {
                                tv_response.append("Response in raw data:\n");
                                tv_response.append("[");
                                tv_response.append(new Gson().toJson(data));
                                tv_response.append("]\n");
                            } else if (data[0] == Utils.DATA_TYPE_ASCII_TEXT) {
                                tv_response.append("Response in text:\n");
                                tv_response.append(new String(data, 1, data.length - 1));
                                tv_response.append("\n\n");
                            }
                        }
                        enableUi(true);
                    }
                });
            }

        }
    };

}
