var statusElement;
var fromServerElement;

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
        this.send("getBark");
    },

    send:function (message) {
        this._ws.send(message);
    },
    onmessage:function (m) {
        if (m.data) {
            var message = m.data;
            if(message.substring(0, 4) == 'BARK'){
                var numberOfBarks = message.substring(4,5);
                fromServerElement.innerHTML = numberOfBarks;
            }

            console.log("GOT MESSAGE" + message);
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
    connection.initConnection();
}

function sendMessage(message) {
    connection.send(message);
}
      