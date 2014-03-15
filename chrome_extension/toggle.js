var DOCS_CONSTANTS = {
	slideIndexArg: "id.p",
	slideHash: "#slide="
};

var current_slide = 1;

function prev () {

	if (current_slide > 1)
	{
		current_slide--;
		slide();
	}
};
function next () {
    current_slide++;
	slide();
};
function slide () {
    window.location.hash = DOCS_CONSTANTS.slideHash + current_slide;
};

chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
	if (request.action == 'next') {
		next();
	}
	else if (request.action == 'prev') {
		prev();
	}
});