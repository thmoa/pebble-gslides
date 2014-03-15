package com.thmoa.pebblegslides;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketHandler;

import android.util.Log;
import android.util.Pair;


public class GSlidesWebSocketHandler extends WebSocketHandler {
	
	protected Connection connection;
	
	final String TAG = "com.thmoa.pebblegslides";
	
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {

		return new GSlidesWebSocket();
	}
	
	public void command (String cmd)
	{	
		if (connection != null && connection.isOpen())
		{
    		new CommandTask().execute(Pair.create(connection, cmd));
		}
	}

	private class GSlidesWebSocket implements WebSocket.OnTextMessage {
		
        public void onOpen(Connection newConnection) {
        	connection = newConnection;
        }
 
        public void onMessage(String data) {
    		try {
				connection.sendMessage(data);
			} catch (IOException e) {
				connection.disconnect();
			}
        }
 
        public void onClose(int closeCode, String message) {
        	
        }
        
    }
}
