package com.bluecats.app.bcdatatransfer;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconCommandCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Created by henrycheng on 19/09/2016.
 */
public class Utils {
    public static final String EXTRA_APP_TOKEN = "EXTRA_APP_TOKEN";
    public static final String EXTRA_APP_BEACON = "EXTRA_APP_BEACON";
    public static final String EXTRA_APP_BEACON2 = "EXTRA_APP_BEACON2";

    public static final byte DATA_TYPE_NONE = 0x00;
    public static final byte DATA_TYPE_RAW = 0x01;
    public static final byte DATA_TYPE_FILE = 0x02;
    public static final byte DATA_TYPE_ASCII_TEXT = 0x03;
    public static final byte DATA_TYPE_ASCII_JSON = 0x04;

    public static void transportDataArray(BCBeacon beacon, ByteBuffer data, BCBeaconCommandCallback cb) {
        try {
            Class<?> c = Class.forName("com.bluecats.sdk.BCBeaconInternal");
            Method m = c.getDeclaredMethod("transportDataArrayForBeacon", ByteBuffer.class, BCBeaconCommandCallback.class);
            m.setAccessible(true);
            m.invoke(beacon, data, cb);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
