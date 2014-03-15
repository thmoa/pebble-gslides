package com.thmoa.pebblegslides;

import java.util.UUID;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {

	final String TAG = "com.thmoa.pebblegslides";
	final UUID PEBBLE_APP_UUID = UUID.fromString("6679691e-09e3-41cf-a082-41230db5a0f5");
	final GSlidesWebSocketHandler gslidesWebSocketHandler = new GSlidesWebSocketHandler();
	
    private Server webServer;
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	try {
			webServer.stop();
		} catch (Exception e) {
			Log.d(TAG, "unexpected exception stopping Web server: " + e);
		}
    	
    	PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webServer = new Server(9146);
        
        
        gslidesWebSocketHandler.setHandler(new DefaultHandler());
        webServer.setHandler(gslidesWebSocketHandler);
        
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        final TextView text = (TextView) findViewById(R.id.textView);
        
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                	try {
                        webServer.start();
                        Log.d(TAG, "started Web server @" + getLocalIpAddress());
                        text.setText(getLocalIpAddress());
                    }
                    catch (Exception e) {
                        Log.d(TAG, "unexpected exception starting Web server: " + e);
                    }
                } else {
                	try {
						webServer.stop();
						text.setText("Not Running");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.d(TAG, "unexpected exception starting Web server: " + e);
					}
                }
            }
        });
        
        Button btnNext = (Button) findViewById(R.id.buttonNext);
        
        btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.d(TAG, "click next");
            	gslidesWebSocketHandler.command("next");
            	
            }
        });
        
        Button btnPrev = (Button) findViewById(R.id.buttonPrev);
        
        btnPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Log.d(TAG, "click prev");
            	gslidesWebSocketHandler.command("prev");
            }
        });
        
        
        initPebble();
    	
        
    }
	
	private void initPebble()
	{
		PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    	
    	if (PebbleKit.areAppMessagesSupported(getApplicationContext())) {
    		PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

				@Override
				public void receiveData(Context context, int transactionId,
						PebbleDictionary data) {
					Log.i(TAG, "Received value=" + data.getUnsignedInteger(1));
					if (data.getUnsignedInteger(1) == 1)
					{
						gslidesWebSocketHandler.command("next");
					}
					else if (data.getUnsignedInteger(1) == 0)
					{
						gslidesWebSocketHandler.command("prev");
					}
				}
    		});
		}
	}
	
	private String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return String.format("%d.%d.%d.%d",
				(ipAddress & 0xff),
				(ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}

}
