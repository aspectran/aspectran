const SessionStats = function(endpoint, refreshInterval) {
    let socket = null;
    let heartbeatTimer = null;

    this.openSocket = function() {
        if (socket) {
            socket.close();
        }
        let url = new URL(endpoint, location.href);
        url.protocol = url.protocol.replace('https:', 'wss:');
        url.protocol = url.protocol.replace('http:', 'ws:');
        socket = new WebSocket(url.href);
        let self = this;
        socket.onopen = function (event) {
            socket.send("JOIN:" + refreshInterval);
            heartbeatPing();
        };
        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                if (event.data === "--pong--") {
                    heartbeatPing();
                    return;
                }
                let stats = JSON.parse(event.data);
                printStats(stats);
            }
        };
        socket.onclose = function (event) {
            self.closeSocket();
        };
        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            setTimeout(function () {
                self.openSocket();
            }, 60000);
        };
    };

    this.closeSocket = function() {
        if (socket) {
            socket.close();
            socket = null;
        }
    };

    const heartbeatPing = function() {
        if (heartbeatTimer) {
            clearTimeout(heartbeatTimer);
        }
        let self = this;
        heartbeatTimer = setTimeout(function () {
            if (socket) {
                socket.send("--ping--");
                heartbeatTimer = null;
                heartbeatPing();
            }
        }, 57000);
    };

    const printStats = function(stats) {
        $(".activeSessionCount").text(stats.activeSessionCount);
        $(".highestSessionCount").text(stats.highestSessionCount);
        $(".createdSessionCount").text(stats.createdSessionCount);
        $(".expiredSessionCount").text(stats.expiredSessionCount);
        $(".rejectedSessionCount").text(stats.rejectedSessionCount);
        if (stats.currentSessions) {
            $(".sessions").empty();
            stats.currentSessions.forEach(function(username) {
                let status = $("<div/>").addClass("status");
                if (username.indexOf("0:") === 0) {
                    status.addClass("logged-out")
                }
                username = username.substring(2);
                let name = $("<span/>").addClass("name").text(username);
                let li = $("<li/>").append(status).append(name);
                $(".sessions").append(li);
            });
        }
    };
}