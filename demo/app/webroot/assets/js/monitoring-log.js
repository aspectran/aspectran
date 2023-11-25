const LogTailer = function(endpoint, tailers) {
    let socket = null;
    let heartbeatTimer = null;
    let scrollTimer = null;
    let prevLogTime = null;
    let prevSentTime = new Date().getTime();
    let prevPosition = 0;

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
            printEventMessage("Socket connection successful");
            socket.send("JOIN:" + tailers);
            heartbeatPing();
            self.switchTailBite(true);
        };
        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                if (event.data === "--pong--") {
                    heartbeatPing();
                } else {
                    parseMessage(event.data);
                }
            }
        };
        socket.onclose = function (event) {
            printEventMessage("Socket connection closed. Please refresh this page to try again!");
            self.closeSocket();
        };
        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            printErrorMessage("Could not connect to WebSocket server");
            self.switchTailBite(false);
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
        heartbeatTimer = setTimeout(function () {
            if (socket) {
                socket.send("--ping--");
                heartbeatTimer = null;
                heartbeatPing();
            }
        }, 57000);
    };

    const parseMessage = function(msg) {
        let idx = msg.indexOf(":");
        if (idx !== -1) {
            let tailerName = msg.substring(0, idx);
            let text = msg.substring(idx + 1);
            if (text.startsWith("last:")) {
                text = text.substring(5);
            } else {
                launchMissile(text);
            }
            printMessage(tailerName, text);
        }
    };

    const printMessage = function(tailer, text) {
        let line = $("<p/>").text(text);
        let logtail = $("#" + tailer);
        logtail.append(line);
        scrollToBottom(logtail);
    };

    const printEventMessage = function(text, tailer) {
        let logtail = (tailer ? $("#" + tailer) : $(".log-tail"));
        $("<p/>").addClass("event").html(text).appendTo(logtail);
        scrollToBottom(logtail);
    };

    const printErrorMessage = function(text, tailer) {
        let logtail = (tailer ? $("#" + tailer) : $(".log-tail"));
        $("<p/>").addClass("event error").html(text).appendTo(logtail);
        scrollToBottom(logtail);
    };

    this.switchTailBite = function(status, logtail) {
        if (!logtail) {
            logtail = $(".log-tail");
        }
        if (status !== true && status !== false) {
            status = !logtail.data("bite");
        }
        if (status) {
            logtail.closest(".log-container").find(".tail-status").addClass("active");
            logtail.data("bite", true);
            scrollToBottom(logtail)
        } else {
            logtail.closest(".log-container").find(".tail-status").removeClass("active");
            logtail.data("bite", false);
        }
    };

    const scrollToBottom = function(logtail) {
        if (logtail.data("bite")) {
            if (scrollTimer) {
                clearTimeout(scrollTimer);
            }
            scrollTimer = setTimeout(function () {
                logtail.scrollTop(logtail.prop("scrollHeight"));
                if (logtail.find("p").length > 11000) {
                    logtail.find("p:gt(10000)").remove();
                }
            }, 300);
        }
    };

    const pattern1 = /^DEBUG (.+) \[(.+)] Create new session id=([^\s;]+)/;
    const pattern2 = /^DEBUG (.+) \[(.+)] Session ([^\s;]+) accessed, stopping timer, active requests=(\d+)/;
    const pattern3 = /^DEBUG (.+) \[(.+)] Session ([^\s;]+) complete, active requests=(\d+)/;
    const pattern4 = /^DEBUG (.+) \[(.+)] Invalidate session id=([^\s;]+)/;
    const pattern5 = /^DEBUG (.+) \[(.+)] Reject session id=([^\s;]+)/;

    const launchMissile = function(text) {
        let matches1 = pattern1.exec(text);
        let matches2 = pattern2.exec(text);
        let matches3 = pattern3.exec(text);
        let matches4 = pattern4.exec(text);
        let matches5 = pattern5.exec(text);

        // if (matches1 || matches2 || matches3 || matches4 || matches5) {
        //     console.log(text);
        //     console.log('matches1', matches1);
        //     console.log('matches2', matches2);
        //     console.log('matches3', matches3);
        //     console.log('matches4', matches4);
        //     console.log('matches5', matches5);
        // }

        let dateTime = "";
        let sessionId = "";
        let requests = 0;
        let delay = 0;
        if (matches3 || matches4 || matches5) {
            if (matches3) {
                sessionId = matches3[3];
                requests = parseInt(matches3[4]) + 1;
            } else if (matches4) {
                sessionId = matches4[3];
                requests = 1
            } else if (matches5) {
                sessionId = matches5[3];
                requests = 1
            }
            if (requests > 3) {
                requests = 3;
            }
            let mis = $(".missile-route").find(".missile[sessionId='" + sessionId + "_" + requests + "']");
            if (mis.length > 0) {
                let dur = 650 + mis.data("delay")||0;
                if (mis.hasClass("mis-2")) {
                    dur += 250;
                } else if (mis.hasClass("mis-3")) {
                    dur += 500;
                }
                setTimeout(function () {
                    mis.remove();
                }, dur + 800);
            }
            return;
        }
        if (matches1 || matches2) {
            if (matches1) {
                dateTime = matches1[1];
                sessionId = matches1[3];
                requests = 1;
            } else if (matches2) {
                dateTime = matches2[1];
                sessionId = matches2[3];
                requests = matches2[4];
            }
            if (requests > 3) {
                requests = 3;
            }
            let logTime = moment(dateTime);
            let currTime = new Date().getTime();
            let spentTime = currTime - prevSentTime;
            if (prevLogTime) {
                delay = logTime.diff(prevLogTime);
                if (delay >= 1000 || delay < 0 || spentTime >= delay + 1000) {
                    delay = 0;
                }
            }
            prevLogTime = logTime;
            prevSentTime = currTime;
        }
        if (requests > 0) {
            let position = generateRandom(3, 120 - 3 - (requests * 4 + 8));
            if (delay < 1000 && prevPosition) {
                if (Math.abs(position - prevPosition) <= 20) {
                    position = generateRandom(3, 120 - 3 - (requests * 4 + 8));
                    if (Math.abs(position - prevPosition) <= 20) {
                        position = generateRandom(3, 120 - 3 - (requests * 4 + 8));
                    }
                }
            }
            prevPosition = position;
            let mis = $("<div/>").attr("sessionId", sessionId + "_" + requests);
            mis.data("delay", 1000 + delay);
            setTimeout(function () {
                mis.addClass("mis-" + requests).removeClass("hidden");
            }, 1000 + delay);
            mis.css("top", position + "px");
            mis.appendTo($(".missile-route")).addClass("hidden missile");
        }
    };

    const generateRandom = function(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };
}