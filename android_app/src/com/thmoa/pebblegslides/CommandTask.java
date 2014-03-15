package com.thmoa.pebblegslides;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

public class CommandTask extends AsyncTask<Pair<Connection, String>, Void, Void> {

	final String TAG = "com.thmoa.pebblegslides";
	
	@Override
	protected Void doInBackground(Pair<Connection, String>... p) {
		
		try {
			p[0].first.sendMessage(p[0].second);
			Log.d(TAG, "sendMessage: " + p[0].second);
		} catch (IOException e) {
			p[0].first.disconnect();
		}
		
		return null;
	}


}
