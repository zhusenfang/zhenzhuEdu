package com.xieruinet.zhu.xieruiedu;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * Created by Administrator on 2017/10/12.
 */

public class MyApplication extends Application {
    private SerialPort mSerialPort = null;
    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            String path = "/dev/ttyS1";//串口�? 配置如：ttyS0,ttyS1,ttyS2.....
            int baudrate = 19200;
/* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
    @Override
    public void onCreate() {
// TODO Auto-generated method stub
        super.onCreate();
    }



}
