var statusElement;
var fromServerElement;
var theImage;

var constance = {
    protocolName:"cosmo",
    serverName:"SERVER",
    web_view_client:"WEB_VIEW_CLIENT",
    barkClient:"BARK_CLIENT",
    webCamClient:"WEB_CAM_CLIENT",
    musicClient:"WEB_MUSIC_CLIENT"
};

if (!window.WebSocket) {
    window.WebSocket = window.MozWebSocket;
    if (!window.WebSocket) {
        alert("WebSocket not supported by this browser");
    }
}

var connection = {
    initConnection:function () {
        var location = document.location.toString()
            .replace('http://', 'ws://')
            .replace('https://', 'wss://')
            + constance.protocolName + "/";

        this._ws = new WebSocket(location, constance.protocolName);
        this._ws.onopen = this.onopen;
        this._ws.onmessage = this.onmessage;
        this._ws.onclose = this.onclose;
    },

    onopen:function () {
        connectionStatus.innerHTML = 'Connected';
        sendMessage(constance.serverName, constance.web_view_client);
    },
    send:function (message) {
        this._ws.send(message);
    },
    onmessage:function (m) {
        var jsonObject = eval("(" + m.data + ')');     // eval is evil use JQuery method or something like that.
        var sendFrom = jsonObject.from;
        var sentValue = jsonObject.value;

        switch (sendFrom) {
            case constance.barkClient:
                fromServerElement.innerHTML = sentValue;
                break;
            case constance.webCamClient:
                theImage.src = "data:text/jpeg;base64," + sentValue;
                break;
            case constance.serverName:
                getBarkInfo();
                getImage();
                break;
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

function getBarkInfo() {
    sendMessage(constance.barkClient, "0");
}

function getImage() {
    sendMessage(constance.webCamClient, "0");
}

function playSong(song) {
    sendMessage(constance.musicClient, song);
}

function sendMessage(to, message) {
    connection.send("{'to' : '" + to + "', 'from' : '" + constance.web_view_client + "' , 'value' : '" + message + "'}");
}
      