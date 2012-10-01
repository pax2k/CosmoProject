var statusElement;
var fromServerElement;
var theImage;

if (!window.WebSocket) {
    window.WebSocket = window.MozWebSocket;
    if (!window.WebSocket)
        alert("WebSocket not supported by this browser");
}

var connection = {
    initConnection:function () {
        var location = document.location.toString()
            .replace('http://', 'ws://')
            .replace('https://', 'wss://')
            + "cosmo";

        this._ws = new WebSocket(location, "cosmo");
        this._ws.onopen = this.onopen;
        this._ws.onmessage = this.onmessage;
        this._ws.onclose = this.onclose;
    },

    onopen:function () {
        connectionStatus.innerHTML = 'Connected';
        sendMessage("BARK_CLIENT", "0");
        getImage();
    },
    send:function (message) {
        this._ws.send(message);
    },
    onmessage:function (m) {
        var obj = eval("(" + m.data + ')');     // eval is evil use JQuery method or something like that.
        var sentTo = obj.to;
        var sendFrom = obj.from;
        var sentValue = obj.value;

        if (sentTo == 'WEB_VIEW_CLIENT') {
            if (sendFrom == 'BARK_CLIENT') {
                fromServerElement.innerHTML = sentValue;
            } else if (obj.from == 'WEB_CAM_CLIENT') {
                theImage.src = "data:text/jpeg;base64," + sentValue;
            }
        }
    },
    onclose:function () {
        connectionStatus.innerHTML = 'Closed';
        this._ws = null;
    }
};

function init() {
    statusElement = document.getElementById('connectionStatus');
    fromServerElement = document.getElementById('fromServer');
    theImage = document.getElementById('theImage');
    connection.initConnection();
}

function sendMessage(to, message) {
    connection.send("{'to' : '" + to + "', 'value' : '" + message + "'}");
}

function getImage() {
    sendMessage("WEB_CAM_CLIENT", "0");
}
      