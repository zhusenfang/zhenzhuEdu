/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.xieruinet.zhu.xieruiedu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

//import com.zenkore.eclass.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;

import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {
	private static final String TAG = "SerialPortActivity";
	protected MyApplication mApplication;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private static InputStream mInputStream;
	private ReadThread mReadThread;
	// private byte[] sentToJiDianQi;发送到继电器开门
	private byte[] sentByte;
	private SendingThread mSendThread;
	private static byte[] card = new byte[4];

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				int size = 0;
				try {
					byte[] buffer = new byte[64];

					if (mInputStream == null) {
						return;
					}

					size = mInputStream.read(buffer);

					if (size == 15) {
						card[0] = buffer[8];
						card[1] = buffer[7];
						card[2] = buffer[6];
						card[3] = buffer[5];
						String cardNumber = bytesToHexString(card);
						Log.e("yee", cardNumber);
						if (!cardNumber.equals("0000000000")) {
							onDataReceived(cardNumber);

						}

					}

				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return "0000000000";
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		try {
			String carnum = new BigInteger(stringBuilder.toString(), 16).toString();
			if (carnum.length() < 10) {// 首位为0情况下 会少一位 加上0
				return getString(carnum);
			} else {
				return carnum;
			}
		} catch (NumberFormatException e) {
			return "0000000000";
		}

	}

	public static String getString(String card) {
		String str = "0000000000";
		Long intHao = Long.parseLong(card);
		DecimalFormat df = new DecimalFormat(str);
		return df.format(intHao);
	}

	private class SendingThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					sleep(1000);// 每隔1秒查询
					if (mOutputStream != null) {
						mOutputStream.write(sentByte);// 发送读卡命令
					} else {
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initSerialPort();
	}

	private void initSerialPort() {
		mApplication = (MyApplication) getApplication();

		/*
		 * sentToJiDianQi = new byte[5];//发送到继电器 sentToJiDianQi[0]=(byte)0X0A;
		 * sentToJiDianQi[1]=(byte)0X01; sentToJiDianQi[2]=(byte)0X04;
		 * sentToJiDianQi[3]=(byte)0X01; sentToJiDianQi[4]=(byte)0X05;
		 */

		sentByte = new byte[5];
		sentByte[0] = (byte) 0X0A;
		sentByte[1] = (byte) 0X01;
		sentByte[2] = (byte) 0X04;
		sentByte[3] = (byte) 0X9B;
		sentByte[4] = (byte) 0X58;

		try {
			mSerialPort = mApplication.getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			// mOutputStream.write(sentToJiDianQi);//发送命令 打开继电器

			/* Create a receiving thread */
			mSendThread = new SendingThread();
			mSendThread.start();

			/* Create a receiving thread */
			// mReadThread = new ReadThread();
			// mReadThread.start();
		} catch (SecurityException e) {
			//Log.e(TAG,getResources().getString( R.string.error_security));
		} catch (IOException e) {
			//Log.e(TAG,getResources().getString( R.string.error_unknown));
		} catch (InvalidParameterException e) {
			//Log.e(TAG,getResources().getString( R.string.error_configuration));
		}
	}

	public static String scanCardID() {
		String readCard = "";
		int size = 0;
		byte[] buffer = new byte[64];
		if (mInputStream == null) {
			return readCard;
		}
		try {
			size = mInputStream.read(buffer);
			if (size == 15) {
				card[0] = buffer[8];
				card[1] = buffer[7];
				card[2] = buffer[6];
				card[3] = buffer[5];
				String cardNumber = bytesToHexString(card);
				Log.e("yee", cardNumber);
				if (!("0000000000".equals(cardNumber) || "4278321419".equals(cardNumber))) {
					readCard = cardNumber;
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return readCard;
		}

		return readCard;
	}

	protected abstract void onDataReceived(String card);

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("SerialPortActivity","父类onDestroy");
		if (mSendThread != null) {
			mSendThread.interrupt();
		}
		if (mReadThread != null)
			mReadThread.interrupt();
		mApplication.closeSerialPort();
		mSerialPort = null;
	}
}
