<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<div class="row">
    <div id="chat-title" class="col-12 p-3">
        <h2 class="m-0">Chat <span id="totalPeople"></span></h2>
        <a class="btn btn-outline-secondary leave" onclick="leaveRoom();"><i class="bi bi-box-arrow-left"></i> Leave</a>
    </div>
    <div id="chat-contacts" class="col-md-4 col-lg-3 d-none d-md-block p-3"></div>
    <div id="chat-room" class="col-12 col-md-8 col-lg-9 ps-3">
        <form id="chat-signin" onsubmit="return false;">
            <h3>Type your username</h3>
            <input type="text" id="chat-username" class="form-control" maxlength="50" placeholder="Username" autocomplete="off" autofocus/>
            <button class="btn btn-primary" onclick="signIn()">Start Chatting <i class="bi bi-box-arrow-right ms-2"></i></button>
        </form>
        <div id="chat-messages" class="pe-3 pb-3"></div>
        <form id="chat-controls-form">
            <div class="input-group pe-3">
                <input class="form-control" type="text" id="chat-message" placeholder="Type a message..."/>
                <button type="submit" class="btn btn-primary"><i class="bi bi-send-fill"></i> Send</button>
            </div>
        </form>
    </div>
</div>
<style>
    #chat-title {
        background-color: var(--bs-primary);
        color: var(--bs-light);
    }
    #chat-title h2 {
        float: left;
        font-size: 28px;
        color: var(--bs-light);
    }
    #chat-title a {
        float: right;
        display: none;
        color: var(--bs-light);
    }

    #chat-room {
        background-color: var(--bs-tertiary-bg);
        height: 580px;
    }
    #chat-signin {
        padding: 180px 0 0 0;
        background-color: var(--bs-tertiary-bg);
        text-align: center;
    }
    #chat-signin * {
        margin-left: auto;
        margin-right: auto;
        margin-bottom: 20px;
    }
    #chat-signin h3 {
        text-align: center;
        margin-bottom: 30px;
    }
    #chat-signin input {
        max-width: 300px;
    }

    #chat-contacts {
        float: left;
        background-color: var(--bs-secondary-bg);
        height: 580px;
        overflow: auto;
    }
    #chat-contacts .contact .status {
        background-color: var(--bs-success);
        width: 20px;
        height: 20px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 10px;
    }
    #chat-contacts .contact .name {
        line-height: 35px;
        vertical-align: super;
    }

    #chat-messages {
        height: 530px;
        overflow-y: auto;
        display: none;
    }
    #chat-messages .message {
        margin-bottom: 3px;
    }
    #chat-messages .message .content {
        border-radius: 20px;
        padding: 10px;
        display: table;
    }
    #chat-messages .message.event .content {
        background-color: var(--bs-info-bg-subtle);
        padding: 7px 20px;
        border-radius: 6px;
        width: 100%;
        text-align: center;
    }
    #chat-messages .message.event .content strong {
        font-weight: bold;
    }
    #chat-messages .message.event.error .content {
        background-color: var(--bs-danger);
        color: var(--bs-light);
    }
    #chat-messages .message.received {
        left: 0;
        text-align: left;
    }
    #chat-messages .message.received .content {
        background-color: var(--bs-primary);
        color: var(--bs-light);
        border-radius: 0 20px 20px;
    }
    #chat-messages .message.received .sender {
        font-weight: bold;
        color: var(--bs-secondary);
    }
    #chat-messages .message.sent {
        right: 0;
        text-align: right;
    }
    #chat-messages .message.sent .content {
        background-color: var(--bs-secondary-bg);
        margin-left: auto;
        margin-right: 0;
        text-align: right;
        border-radius: 20px 0 20px 20px;
    }
    #chat-messages .message.sent .sender {
        font-weight: bold;
    }

    #chat-controls-form {
        height: 40px;
        display: none;
    }

    #chat-messages .message.sent.same-sender-previous-message .sender,
    #chat-messages .message.received.same-sender-previous-message .sender {
        display: none;
    }
    #chat-messages .message:not(.same-sender-previous-message) {
        margin-top: 10px;
    }
</style>
<script>
    let socket;
    let currentUser;

    $(function () {
        $("form#chat-controls-form").submit(function() {
            sendMessage();
            return false;
        });
    });

    function signIn() {
        currentUser = $("#chat-username").val().trim();
        $("#chat-username").val("");
        if (currentUser) {
            $("#chat-signin").hide();
            $("#chat-messages").show();
            $("#chat-controls-form").show();
            $("a.leave").show();
            $("#chat-message").focus();
            openSocket();
        }
    }

    function openSocket() {
        if (socket) {
            socket.close();
        }
        let url = new URL('<aspectran:url value="/chat"/>', location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        socket = new WebSocket(url.href);

        socket.onopen = function(event) {
            let chatMessage = {
                sendTextMessage: {
                    type: 'JOIN',
                    username: currentUser
                }
            };
            socket.send(JSON.stringify(chatMessage));
        };

        socket.onmessage = function (event) {
            if (typeof event.data === "string") {
                let chatMessage = JSON.parse(event.data);
                Object.getOwnPropertyNames(chatMessage).forEach(function(val, idx, array) {
                    let payload = chatMessage[val];
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
                                for (let i = 0; i < payload.usernames.length; i++) {
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
            $("#chat-contacts").empty();
            $("#chat-messages").empty().hide();
            $("#chat-controls-form").hide();
            $("a.leave").hide();
            $("#chat-message").val('');
            $("#chat-signin").show();
            $("#chat-username").focus();
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
        let text = $("#chat-message").val().trim();
        $("#chat-message").val('');

        if (text) {
            let chatMessage = {
                sendTextMessage: {
                    type: 'CHAT',
                    username: currentUser,
                    content: text
                }
            };
            socket.send(JSON.stringify(chatMessage));
            displayMessage(currentUser, text);
            $("#chat-message").val('').focus();
        }
    }

    function displayMessage(username, text) {
        let sentByCurrentUer = (currentUser === username);
        let message = $("<div/>").addClass(sentByCurrentUer === true ? "message sent" : "message received");
        message.data("sender", username);

        let sender = $("<span/>").addClass("sender");
        sender.text(sentByCurrentUer === true ? "You" : username);
        sender.appendTo(message);

        let content = $("<span/>").addClass("content").text(text);
        content.appendTo(message);

        let lastMessage = $("#chat-messages .message").last();
        if (lastMessage.length && lastMessage.data("sender") === username) {
            message.addClass("same-sender-previous-message");
        }

        $("#chat-messages").append(message);
        $("#chat-messages").animate({scrollTop: $("#chat-messages").prop("scrollHeight")});
    }

    function displayConnectedUserMessage(username) {
        let sentByCurrentUer = currentUser === username;
        let text = (sentByCurrentUer === true ? "Welcome <strong>" + username + "</strong>" : "<strong>" + username + "</strong> joined the chat");
        displayEventMessage(text);
    }

    function displayDisconnectedUserMessage(username) {
        let text = "<strong>" + username + "</strong> left the chat";
        displayEventMessage(text);
    }

    function addAvailableUsers(username) {
        let contact = $("<div/>").addClass("contact");
        let status = $("<div/>").addClass("status");
        let name = $("<span/>").addClass("name").text(username);
        contact.append(status).append(name).appendTo($("#chat-contacts"));
        updateTotalPeople();
    }

    function cleanAvailableUsers() {
        $("#chat-contacts").empty();
        clearTotalPeople();
    }

    function updateTotalPeople() {
        $("#totalPeople").text("(" + $("#chat-contacts .contact").length + ")");
    }

    function clearTotalPeople() {
        $("#totalPeople").text("");
    }

    function displayEventMessage(text) {
        let div = $("<div/>").addClass("message event");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#chat-messages").append(div);
        $("#chat-messages").animate({scrollTop: $("#chat-messages").prop("scrollHeight")});
    }

    function displayErrorMessage(text) {
        let div = $("<div/>").addClass("message event error");
        $("<p/>").addClass("content").html(text).appendTo(div);
        $("#chat-messages").append(div);
        $("#chat-messages").animate({scrollTop: $("#chat-messages").prop("scrollHeight")});
    }
</script>
