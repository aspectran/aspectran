<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="row">
    <div id="title" class="columns small-12">
        <h2>Chat <span id="totalPeople"></span></h2>
        <a class="leave" onclick="leaveRoom();">Leave</a>
    </div>
    <div id="contacts" class="columns medium-4 large-3 hide-for-small-only"></div>
    <div id="room" class="columns small-12 medium-8 large-9">
        <form id="signin" onsubmit="return false;">
            <h3>Type your username</h3>
            <input type="text" id="username" maxlength="50" placeholder="Username" autocomplete="off" autofocus/>
            <button class="button" onclick="signIn()">Start Chatting</button>
        </form>
        <div id="messages"></div>
        <form id="chat-controls">
            <div class="input-group">
                <input class="input-group-field" type="text" id="message" placeholder="Type a message..."/>
                <div class="input-group-button">
                    <button type="submit" class="button">Send</button>
                </div>
            </div>
        </form>
    </div>
</div>
<style>
    #title {
        background-color: #35505B;
        padding: 10px 20px 10px 20px;
        color: #fff;
    }
    #title h2 {
        float: left;
        font-size: 28px;
    }
    #title a {
        float: right;
        line-height: 38px;
        vertical-align: bottom;
        display: none;
    }

    #room {
        background-color: #f5f5f5;
        height: 580px;
    }
    #signin {
        padding: 180px 0 0 0;
        background-color: #f5f5f5;
    }
    #signin * {
        display: block;
        margin-left: auto;
        margin-right: auto;
        margin-bottom: 20px;
    }
    #signin h3 {
        text-align: center;
        margin-bottom: 30px;
    }
    #signin input {
        max-width: 300px;
    }

    #contacts {
        float: left;
        padding: 15px;
        background-color: #bbedfe;
        height: 580px;
        overflow: auto;
    }
    #contacts .contact .status {
        background-color: #13CF13;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 10px;
    }
    #contacts .contact .name {
        line-height: 35px;
        vertical-align: super;
    }

    #messages {
        height: 500px;
        overflow-y: auto;
        display: none;
    }
    #messages .message {
        margin-bottom: 3px;
    }
    #messages .message .content {
        border-radius: 20px;
        padding: 10px;
        display: table;
    }
    #messages .message.event .content {
        background-color: #bbedfe;
        padding: 7px 20px;
        border-radius: 6px;
        width: 100%;
        text-align: center;
    }
    #messages .message.event .content strong {
        font-weight: bold;
    }
    #messages .message.event.error .content {
        background-color: palevioletred;
        color: yellow;
    }
    #messages .message.received {
        left: 0;
        text-align: left;
    }
    #messages .message.received .content {
        background-color: #0084FF;
        color: #fff;
        border-radius: 0 20px 20px;
    }
    #messages .message.received .sender {
        font-weight: bold;
        color: #0084FF;
    }
    #messages .message.sent {
        right: 0;
        text-align: right;
    }
    #messages .message.sent .content {
        background-color: #ccc;
        margin-left: auto;
        margin-right: 0;
        text-align: right;
        border-radius: 20px 0 20px 20px;
    }
    #messages .message.sent .sender {
        font-weight: bold;
    }

    #chat-controls {
        height: 40px;
        padding: 20px 0;
        display: none;
    }

    #messages .message.sent.same-sender-previous-message .sender,
    #messages .message.received.same-sender-previous-message .sender {
        display: none;
    }
    #messages .message:not(.same-sender-previous-message) {
        margin-top: 10px;
    }
</style>
<script>
    var socket;
    var currentUser;

    $(function() {
        $("form#chat-controls").submit(function() {
            sendMessage();
            return false;
        });
    });

    function signIn() {
        currentUser = $("#username").val().trim();
        $("#username").val("");
        if (currentUser) {
            $("#signin").hide();
            $("#messages").show();
            $("#chat-controls").show();
            $("a.leave").show();
            $("#message").focus();
            openSocket();
        }
    }

    function openSocket() {
        if (socket) {
            socket.close();
        }
        var url = new URL('/chat', location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        socket = new WebSocket(url.href);

        socket.onopen = function(event) {
            var chatMessage = {
                sendTextMessage: {
                    type: 'JOIN',
                    username: currentUser
                }
            };
            socket.send(JSON.stringify(chatMessage));
        };

        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                var chatMessage = JSON.parse(event.data);
                Object.getOwnPropertyNames(chatMessage).forEach(function(val, idx, array) {
                    var payload = chatMessage[val];
                    if (payload) {
                        switch (val) {
                            case "welcomeUser":
                                displayConnectedUserMessage(payload.username);
                                break;
                            case "duplicatedUser":
                                socket.close();
                                alert("Duplicated user: " + payload.username);
                                location.reload();
                                break;
                            case "broadcastTextMessage":
                                displayMessage(payload.username, payload.content);
                                break;
                            case "broadcastConnectedUser":
                                displayConnectedUserMessage(payload.username);
                                break;
                            case "broadcastDisconnectedUser":
                                displayDisconnectedUserMessage(payload.username);
                                break;
                            case "broadcastAvailableUsers":
                                cleanAvailableUsers();
                                for (var i = 0; i < payload.usernames.length; i++) {
                                    addAvailableUsers(payload.usernames[i]);
                                }
                                break;
                        }
                    }
                });
            }
        };

        socket.onclose = function (event) {
            clearTotalPeople();
            $("#contacts").empty();
            $("#messages").empty().hide();
            $("#chat-controls").hide();
            $("a.leave").hide();
            $("#message").val('');
            $("#signin").show();
            $("#username").focus();
        };

        socket.onerror = function (event) {
            console.error("WebSocket error observed:", event);
            displayErrorMessage('Could not connect to WebSocket server. Please refresh this page to try again!');
        };
    }

    function leaveRoom() {
        socket.close();
    }

    function sendMessage() {
        var text = $("#message").val().trim();
        $("#message").val('');

        if (text) {
            var chatMessage = {
                sendTextMessage: {
                    type: 'CHAT',
                    username: currentUser,
                    content: text
                }
            };
            socket.send(JSON.stringify(chatMessage));
            $("#message").val('').focus();
        }
    }

    function displayMessage(username, text) {
        var sentByCurrentUer = (currentUser === username);
        var message = $("<div/>").addClass(sentByCurrentUer === true ? "message sent" : "message received");
        message.data("sender", username);

        var sender = $("<span/>").addClass("sender");
        sender.text(sentByCurrentUer === true ? "You" : username);
        sender.appendTo(message);

        var content = $("<span/>").addClass("content").text(text);
        content.appendTo(message);

        var lastMessage = $("#messages .message").last();
        if (lastMessage.length && lastMessage.data("sender") === username) {
            message.addClass("same-sender-previous-message");
        }

        $("#messages").append(message);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }

    function displayConnectedUserMessage(username) {
        var sentByCurrentUer = currentUser === username;
        var text = (sentByCurrentUer === true ? "Welcome <strong>" + username : username + "</strong> joined the chat");
        displayEventMessage(text);
    }

    function displayDisconnectedUserMessage(username) {
        var text = "<strong>" + username + "</strong> left the chat";
        displayEventMessage(text);
    }

    function addAvailableUsers(username) {
        var contact = $("<div/>").addClass("contact");
        var status = $("<div/>").addClass("status");
        var name = $("<span/>").addClass("name").text(username);
        contact.append(status).append(name).appendTo($("#contacts"));
        updateTotalPeople();
    }

    function cleanAvailableUsers() {
        $("#contacts").empty();
        clearTotalPeople();
    }

    function updateTotalPeople() {
        $("#totalPeople").text("(" + $("#contacts .contact").length + ")");
    }

    function clearTotalPeople() {
        $("#totalPeople").text("");
    }

    function displayEventMessage(text) {
        var div = $("<div/>").addClass("message event");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#messages").append(div);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }

    function displayErrorMessage(text) {
        var div = $("<div/>").addClass("message event error");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#messages").append(div);
        $("#messages").animate({scrollTop: $("#messages").prop("scrollHeight")});
    }
</script>