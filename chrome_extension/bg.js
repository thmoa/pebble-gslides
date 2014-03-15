var websocket;
var connected = false;
var ip = '';

function open(newip) {
	ip = newip;
	
	websocket = new WebSocket("ws://"+ip+":9146");

	websocket.onopen = function(evt) { 
		console.log('CONNECT');
		connected = true;

		var popup = chrome.extension.getViews({type : "popup"});
		
		if (popup) popup[0].state_connect();
	};
	
	websocket.onclose = function(evt) { 
		console.log('DISCONNECT');
		connected = false;
		
		var popup = chrome.extension.getViews({type : "popup"});
		
		if (popup) popup[0].state_unconnect();
	};
	
	websocket.onerror = function(evt) { 
		console.log('ERROR: ' + evt.data);
		connected = false;
			
		var popup = chrome.extension.getViews({type : "popup"});
		
		if (popup) popup[0].state_unconnect();
	};
	
	websocket.onmessage = function(evt) { 
		console.log('MSG: ' + evt.data);
		chrome.tabs.query({active: true, currentWindow: true}, function(tabs) {
		  chrome.tabs.sendMessage(tabs[0].id, {action: evt.data});
		});
	};
}

function close() {
	if (typeof(websocket) !== "undefined") websocket.close();
}

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
	
	if (request.action == 'connect')
	{
		open(request.ip);
	}
	else if (request.action == 'unconnect')
	{
		close();
	}
	else if (request.action == 'is_connected')
	{
		sendResponse({connected: connected, ip: ip});
	}
	
});

chrome.tabs.onUpdated.addListener(function(tabId, change, tab) {
  if (change.status == "complete")
  {
	if (tab.url.indexOf("https://docs.google.com/presentation/") == 0)
	{
		chrome.pageAction.show(tabId);
	}
  }
});		
