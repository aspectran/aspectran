<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<link rel="stylesheet" href="/assets/css/logtail.css?20191126">
<div class="row">
    <div class="columns small-12 large-5 t20">
        <h3>User Session Statistics</h3>
        <div class="panel stats">
            <dl>
                <dt>Current Active Sessions</dt>
                <dd><span class="number activeSessionCount">0</span></dd>
                <dt>Max Active Sessions</dt>
                <dd><span class="number highestSessionCount">0</span></dd>
                <dt title="Number of sessions created since system bootup">Created Sessions</dt>
                <dd><span class="number createdSessionCount">0</span></dd>
                <dt>Expired Sessions</dt>
                <dd><span class="number expiredSessionCount">0</span></dd>
                <dt>Rejected Sessions</dt>
                <dd><span class="number rejectedSessionCount">0</span></dd>
            </dl>
        </div>
    </div>
    <div class="columns small-12 large-7 t20">
        <h3>Current Users</h3>
        <div class="panel users-wrap">
            <ul class="users">
            </ul>
        </div>
    </div>
</div>
<div class="row">
    <div class="columns small-12 t20">
        <h3>Application Log</h3>
        <div class="log-container">
            <div class="log-header">
                <ul class="tab">
                    <li>app-log</li>
                </ul>
                <a class="bite-tail" title="Scroll to End of Log">
                    <span class="tail-status"></span>
                </a>
            </div>
            <div class="missile-route">
                <div class="stack"></div>
            </div>
            <pre id="app-log" class="log-tail"></pre>
        </div>
    </div>
</div>
<style>
    .stats {
        height: 220px;
    }
    .stats dt {
        float: left;
        clear: left;
        padding: 0;
        margin: 0;
        height: 40px;
        font-size: 1.1em;
    }
    .stats dd {
        text-align: right;
        font-weight: bold;
        font-size: 1.1em;
        padding: 0;
        margin: 0;
        height: 40px;
    }

    .users-wrap {
        height: 220px;
        overflow-y: auto;
        list-style-type: none;
    }
    .users {
        margin-left: 0;
    }
    .users li {
        list-style-type: none;
    }
    .users .status {
        background-color: #13CF13;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 10px;
    }
    .users .status.logged-out {
        background-color: #ccc;
    }
</style>
<script>
    $(function() {
        updateSessionStats();
        $("button.refresh").click(function() {
            $(".box21,.box22").fadeOut();
            updateSessionStats();
            $(".box21,.box22").fadeIn();
        });
    });

    var updateTimer;
    function updateSessionStats() {
        $.ajax({
            type: 'get',
            dataType: 'json',
            url: '/apm/getSessionStats',
            async: false,
            success: function (data) {
                // console.log(data);
                $(".activeSessionCount").text(data.activeSessionCount);
                $(".highestSessionCount").text(data.highestSessionCount);
                $(".createdSessionCount").text(data.createdSessionCount);
                $(".expiredSessionCount").text(data.expiredSessionCount);
                $(".rejectedSessionCount").text(data.rejectedSessionCount);
                if (data.currentUsers) {
                    $(".users").empty();
                    data.currentUsers.forEach(function(username) {
                        var status = $("<div/>").addClass("status");
                        var name = $("<span/>").addClass("name").text(username);
                        var li = $("<li/>").append(status).append(name);
                        $(".users").append(li);
                    });
                }
                if (updateTimer) {
                    clearTimeout(updateTimer);
                }
                updateTimer = setTimeout(function() {
                    updateSessionStats();
                }, 60000);
            },
            error: function (request, status, error) {
                console.log(error);
            }
        });
    }
</script>
<script>
    var socket;
    var heartbeatTimer;

    $(function() {
        $(".bite-tail").click(function() {
           var logtail = $(this).closest(".log-container").find(".log-tail");
           switchTailBite(logtail, !logtail.data("bite"));
        });
        try {
            openSocket();
        } catch (e) {
            printErrorMessage("Socket connection failed");
        }
    });

    function openSocket() {
        if (socket) {
            socket.close();
        }
        var url = new URL('/apm/logtail', location.href);
        url.protocol = url.protocol.replace('https:', 'wss:');
        url.protocol = url.protocol.replace('http:', 'ws:');
        socket = new WebSocket(url.href);
        socket.onopen = function (event) {
            printEventMessage("Socket connection successful");
            socket.send("JOIN:app-log");
            heartbeatPing();
            switchTailBite(false, true);
        };
        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                var msg = event.data;
                var idx = msg.indexOf(":");
                if (idx !== -1) {
                    printMessage(msg.substring(0, idx), msg.substring(idx + 1));
                }
            }
            heartbeatPing();
        };
        socket.onclose = function (event) {
            printEventMessage('Socket connection closed. Please refresh this page to try again!');
            closeSocket();
        };
        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            printErrorMessage('Could not connect to WebSocket server');
            switchTailBite(false, false);
            setTimeout(function() {
                openSocket();
            }, 60000);
        };
    }

    function closeSocket() {
        if (socket) {
            socket.close();
            socket = null;
        }
    }

    function heartbeatPing() {
        if (heartbeatTimer) {
            clearTimeout(heartbeatTimer);
        }
        heartbeatTimer = setTimeout(function () {
            if (socket) {
                socket.send("--heartbeat-ping--");
                heartbeatTimer = null;
                heartbeatPing();
            }
        }, 59000);
    }

    function printMessage(tailer, text) {
        setTimeout(function () {
            launchMissile(text);
        }, 1);
        var line = $("<p/>").text(text);
        var logtail = $("#" + tailer);
        logtail.append(line);
        scrollToBottom(logtail);
    }

    function printEventMessage(text, tailer) {
        var logtail = (tailer ? $("#" + tailer) : $(".log-tail"));
        $("<p/>").addClass("event").html(text).appendTo(logtail);
        scrollToBottom(logtail);
    }

    function printErrorMessage(text, tailer) {
        var logtail = (tailer ? $("#" + tailer) : $(".log-tail"));
        $("<p/>").addClass("event error").html(text).appendTo(logtail);
        scrollToBottom(logtail);
    }

    function switchTailBite(logtail, status) {
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
    }

    var scrollTimer;
    function scrollToBottom(logtail) {
        if (logtail.data("bite")) {
            if (scrollTimer) {
                clearTimeout(scrollTimer);
            }
            scrollTimer = setTimeout(function() {
                logtail.scrollTop(logtail.prop("scrollHeight"));
                if (logtail.find("p").length > 11000) {
                    logtail.find("p:gt(10000)").remove();
                }
            }, 300);
        }
    }

    function launchMissile(line) {
        var sessionId = "";
        var requests = 0;
        var idx = line.indexOf("Session");
        if (idx !== -1) {
            line = line.substring(idx);
            var pattern1 = /^Session ([\w\.]+) complete, active requests=(\d+)/i;
            var pattern2 = /^Session ([\w\.]+) deleted in session data store/i;
            if (pattern1.test(line) || pattern2.test(line)) {
                sessionId = RegExp.$1;
                requests = RegExp.$2;
                if (requests > 3) {
                    requests = 3;
                }
                requests++;
                var mis = $(".missile-route").find(".missile[sessionId='" + (sessionId + requests) + "']");
                if (mis.length > 0) {
                    var dur = 850;
                    if (mis.hasClass("mis-2")) {
                        dur += 250;
                    } else if (mis.hasClass("mis-3")) {
                        dur += 500;
                    }
                    setTimeout(function () {
                        mis.remove();
                    }, dur);
                }
                return;
            }
            var pattern3 = /^Session ([\w\.]+) accessed, stopping timer, active requests=(\d+)/i;
            if (pattern3.test(line)) {
                sessionId = RegExp.$1;
                requests = RegExp.$2;
                if (requests > 3) {
                    requests = 3;
                }
            }
        }
        if (requests > 0) {
            var mis = $("<div/>").attr("sessionId", sessionId + requests);
            mis.css("top", generateRandom(3, 90 - (requests * 2)) + "%");
            mis.appendTo($(".missile-route")).addClass("missile mis-" + requests);
        }
    }

    function generateRandom(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
</script>