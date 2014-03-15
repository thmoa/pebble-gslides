var connected = false;
var btn_connect = document.getElementById('connect');
var input_ip = document.getElementById('ip');
var btn_login = document.getElementById('login');

function hasClass(elem, className) {
    return new RegExp(' ' + className + ' ').test(' ' + elem.className + ' ');
}

function addClass(elem, className) {
    if (!hasClass(elem, className)) {
        elem.className += ' ' + className;
    }
}

function removeClass(elem, className) {
    var newClass = ' ' + elem.className.replace( /[\t\r\n]/g, ' ') + ' ';
    if (hasClass(elem, className)) {
        while (newClass.indexOf(' ' + className + ' ') >= 0 ) {
            newClass = newClass.replace(' ' + className + ' ', ' ');
        }
        elem.className = newClass.replace(/^\s+|\s+$/g, '');
    }
} 

function state_connect ()
{
	removeClass(btn_connect, 'btn-primary');
	addClass(btn_connect, 'btn-success');
	connected = true;
	
	btn_connect.innerText = 'DISCONNECT';
}

function state_unconnect ()
{
	removeClass(btn_connect, 'btn-success');
	addClass(btn_connect, 'btn-primary');
	connected = false;
	
	btn_connect.innerText = 'CONNECT';
}

btn_connect.addEventListener('click', function(event) {

	if (input_ip.value.match(/^(?:(?:2[0-4]\d|25[0-5]|1\d{2}|[1-9]?\d)\.){3}(?:2[0-4]\d|25[0-5]|1\d{2}|[1-9]?\d)(?:\:(?:\d|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5]))?$/))
	{
		if (!connected)
		{
			chrome.runtime.sendMessage({action: "connect", ip : input_ip.value});
		}
		else
		{		
			chrome.runtime.sendMessage({action: "unconnect"});
			state_unconnect();
		}
	}

});

chrome.runtime.sendMessage({action: "is_connected"}, function(response) {
    if (response.connected)
	{
		state_connect();
	}
	input_ip.value = response.ip;
});

