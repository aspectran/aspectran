function SessionStats(endpoint, refreshInterval) {
    this.endpoint = endpoint;
    this.refreshInterval = refreshInterval;
    this.socket = null;
    this.heartbeatTimer = null;

    this.openSocket = function() {
        if (this.socket) {
            this.socket.close();
        }
        let url = new URL(this.endpoint, location.href);
        url.protocol = url.protocol.replace('https:', 'wss:');
        url.protocol = url.protocol.replace('http:', 'ws:');
        this.socket = new WebSocket(url.href);
        let self = this;
        this.socket.onopen = function (event) {
            self.socket.send("JOIN:" + self.refreshInterval);
            self.heartbeatPing();
        };
        this.socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                if (event.data === "--pong--") {
                    self.heartbeatPing();
                    return;
                }
                let stats = JSON.parse(event.data);
                self.printStats(stats);
            }
        };
        this.socket.onclose = function (event) {
            self.closeSocket();
        };
        this.socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            setTimeout(function () {
                self.openSocket();
            }, 60000);
        };
    };

    this.closeSocket = function() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
    };

    this.heartbeatPing = function() {
        if (this.heartbeatTimer) {
            clearTimeout(this.heartbeatTimer);
        }
        let self = this;
        this.heartbeatTimer = setTimeout(function () {
            if (self.socket) {
                self.socket.send("--ping--");
                self.heartbeatTimer = null;
                self.heartbeatPing();
            }
        }, 57000);
    };

    this.printStats = function(stats) {
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