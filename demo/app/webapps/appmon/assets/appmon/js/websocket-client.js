function WebsocketClient(endpoint, onEndpointJoined, onEstablishCompleted, onErrorObserved) {
    let socket = null;
    let heartbeatTimer = null;
    let pendingMessages = [];
    let established = false;

    this.start = function (joinGroups) {
        openSocket(joinGroups);
    };

    this.stop = function () {
        closeSocket();
    };

    const openSocket = function (joinGroups) {
        // For test
        // onErrorObserved(endpoint);
        // return;
        closeSocket();
        let url = new URL(endpoint.url + '/' + endpoint.token, location.href);
        url.protocol = url.protocol.replace('https:', 'wss:');
        url.protocol = url.protocol.replace('http:', 'ws:');
        socket = new WebSocket(url.href);
        socket.onopen = function (event) {
            pendingMessages.push("Socket connection successful");
            socket.send("join:" + (joinGroups||""));
            heartbeatPing();
        };
        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                if (event.data === "--pong--") {
                    heartbeatPing();
                    return;
                }
                let msg = event.data;
                if (established) {
                    endpoint.viewer.processMessage(msg);
                } else if (msg.startsWith("joined:")) {
                    console.log(msg);
                    let payload = JSON.parse(msg.substring(7));
                    establish(payload);
                }
            }
        };
        socket.onclose = function (event) {
            if (event.code === 1000) {
                endpoint.viewer.printMessage("Socket connection closed.");
            } else {
                closeSocket();
                endpoint.viewer.printMessage("Socket connection closed. Please refresh this page to try again!");
            }
        };
        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            if (!endpoint.mode && onErrorObserved) {
                onErrorObserved(endpoint);
            } else {
                endpoint.viewer.printErrorMessage("Could not connect to WebSocket server.");
            }
        };
    };

    const closeSocket = function () {
        if (socket) {
            socket.close();
            socket = null;
        }
    };

    const establish = function (payload) {
        if (onEndpointJoined) {
            endpoint['mode'] = "websocket";
            onEndpointJoined(endpoint, payload);
        }
        while (pendingMessages.length) {
            endpoint.viewer.printMessage(pendingMessages.shift());
        }
        if (onEstablishCompleted) {
            onEstablishCompleted(endpoint);
        }
        while (pendingMessages.length) {
            endpoint.viewer.printMessage(pendingMessages.shift());
        }
        established = true;
        socket.send("established:");
    };

    const heartbeatPing = function () {
        if (heartbeatTimer) {
            clearTimeout(heartbeatTimer);
        }
        heartbeatTimer = setTimeout(function () {
            if (socket) {
                socket.send("--ping--");
            }
        }, 57000);
    };
}
