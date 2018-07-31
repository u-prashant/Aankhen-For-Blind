package com.prashant.blindwalk;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {
private static final String DTAG = "DEBUGGING---------";
private static final String ETAG = "ERROR-------------";
// SPP UUID service
private static final UUID MY_UUID =
UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
// MAC-address of Bluetooth module (you must edit this line)
private static String address = "98:D3:31:FC:58:DC";

TextToSpeech tts;
TextView tv1,tv2,tv3;
BluetoothAdapter mBluetoothAdapter;
BluetoothDevice mBluetoothDevice;
ConnectThread mConnectThread;
ConnectedThread mConnectedThread;
private Handler mHandler; // handler that gets info from Bluetooth
Service
public static final int MESSAGE_RECEIVE = 0;
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);
// Textview to display message from Arduino

// textView = (TextView) findViewById(R.id.message_box);
tv1 = (TextView) findViewById(R.id.BT);
tts = new TextToSpeech(getApplicationContext(), new
TextToSpeech.OnInitListener() {

@Override
public void onInit(int status) {
if(status != TextToSpeech.ERROR) {
tts.setLanguage(Locale.UK);
}
}
});
mHandler = new Handler() {
@Override
public void handleMessage(Message msg) {
byte[] readBuf = (byte[]) msg.obj;
String message = new String(readBuf, 0, msg.arg1);

// textView.setText(message);

Log.d(DTAG,"........String........"+message);
tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
}


};
// 1. GET THE BLUETOOTH ADAPTER
mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
if (mBluetoothAdapter == null) {
Log.e(ETAG,"... 1. Device does not support Bluetooth ...");
return;
}
else {
Log.d(DTAG,"... 1. Got the Adapter...");
}
// 2. ENABLING THE BLUETOOTH
if (!mBluetoothAdapter.isEnabled()) {
mBluetoothAdapter.enable();
Log.d(DTAG,"... 2. Bluetooth Enabled...");
}
else {
Log.d(DTAG,"... 2. Bluetooth Already ON...");
}
//tv1.setPaintFlags(tv1.getPaintFlags() |
Paint.STRIKE_THRU_TEXT_FLAG);
mConnectThread = new ConnectThread();
mConnectThread.start();
}
private class ConnectThread extends Thread {
private final BluetoothSocket mmSocket;
public ConnectThread() {
// Use a temporary object that is later assigned to mmSocket
// because mmSocket is final.
mBluetoothDevice =

mBluetoothAdapter.getRemoteDevice(address);

Log.d(DTAG,"... 3. Got Remote Device

"+mBluetoothDevice.getName()+"...");

BluetoothSocket tmp = null;
try {
// Get a BluetoothSocket to connect with the given

BluetoothDevice.

// MY_UUID is the app's UUID string, also used in the server

code.

tmp =

mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
} catch (IOException e) {
Log.e(ETAG,"... 4. Socket's create() method failed...", e);
}
mmSocket = tmp;
Log.d(DTAG,"... 4. Got the Socket "+mmSocket+"...");
}
@Override
public void run() {
// Cancel discovery because it otherwise slows down the

connection.

mBluetoothAdapter.cancelDiscovery();
Log.d(DTAG,"... 5. Making connection...");
try {
// Coonect to the remote device through the socket. This

call blocks

// until it succeeds or throws an exception.
mmSocket.connect();
Log.d(DTAG,"... 6. Connection OK ...");
} catch (IOException e) {
// Unable to connect; close the socket & return.
try {
mmSocket.close();
Log.e(DTAG,"... 6. Socket closed Successfully...");
} catch (IOException e1) {
Log.e(ETAG, "... 6. Could not close the socket...",e1);
}
return;
}
// The connection attempt succeeded. Perform work associated

with

// the connection in a separate thread.
mConnectedThread = new ConnectedThread(mmSocket);
mConnectedThread.start();


}
// Closes the client socket & causes the thread to finish.
public void cancel() {
try {
mmSocket.close();
// Log.e(DTAG,"... Socket closed Successfully...");
} catch (IOException e1) {
Log.e(ETAG, "... Could not close the socket...",e1);
}
}
}
private class ConnectedThread extends Thread {
private final BluetoothSocket mmSocket;
private final InputStream mmInStream;
private byte[] mmBuffer; // mmBuffer store for the stream
public ConnectedThread(BluetoothSocket socket) {
mmSocket = socket;
InputStream tmpIn = null;
// Get the input streams; using temp objects because
// member streams are final.
try {
tmpIn = socket.getInputStream();
} catch (IOException e) {
Log.e(ETAG, "Error occured when creating input stream", e);
}
mmInStream = tmpIn;
}
@Override
public void run() {
mmBuffer = new byte[1024];
int numBytes; // bytes returned from read()
// Keep listening to the InputStream until an exception occurs.
while(true) {
try {

// Read from the InputStream.
if (mmInStream.available() > 0) {
numBytes = mmInStream.read(mmBuffer);
// Send the message to the UI activity.
Message readMsg =
mHandler.obtainMessage(MESSAGE_RECEIVE, numBytes, -1, mmBuffer);

readMsg.sendToTarget();
}
else SystemClock.sleep(100);
} catch (IOException e) {
e.printStackTrace();
}
}
}
// Call this method from the main activity to shut down the
connection.
public void cancel() {
try {
mmSocket.close();
} catch (IOException e) {
Log.e(ETAG, "Could not close the coonect socket SERVICE

CLASS",e);
}
}
}
@Override
protected void onPause() {
if(tts !=null){
tts.stop();
tts.shutdown();
}
super.onPause();
}
@Override
protected void onDestroy() {
if(tts !=null){
tts.stop();
tts.shutdown();


}
super.onDestroy();
}
