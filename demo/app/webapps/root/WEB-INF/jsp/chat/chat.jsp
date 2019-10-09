<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="chat-wrap">
    <div id="title">Chat</div>

    <form id="authentication" onsubmit="return false;">
        <h3>Type your username</h3>
        <input type="text" id="username" maxlength="50" placeholder="Username" autocomplete="off" autofocus/>
        <button class="button" onclick="signIn()">Start Chatting</button>
        <span class="error" id="authentication-error"></span>
    </form>

    <div id="contacts"></div>

    <div id="chat">
        <div id="messages"></div>
        <form id="chat-controls" onsubmit="return false;">
            <input type="text" id="message" placeholder="Enter a message"/>
            <button class="button" onclick="sendMessage()">Send</button>
        </form>
    </div>
</div>
<style>
    /* Common */
    @media print, screen and (min-width: 64em) {
        section > .row {
            background-color: #f5f5f5;
            border-radius: 0 0 10px 10px;
        }
    }
    @media print, screen and (min-width: 64em) {
        section > .row > .columns {
            padding: 0;
        }
    }
    #chat-wrap {
        background-color: #f5f5f5;
        overflow: auto;
        border-radius: 0 0 10px 10px;

    }
    .error {
        color: #F04823;
        font-size: 12px;
    }

    /* Authentication */
    #authentication {
        margin: 100px 0;
    }
    #authentication * {
        display: block;
        margin-left: auto;
        margin-right: auto;
        margin-bottom: 10px;
    }
    #authentication .error {
        text-align: center;
    }
    #authentication h3 {
        text-align: center;
        margin-bottom: 30px;
    }
    #authentication input {
        max-width: 300px;
        margin-bottom: 20px;
    }

    /* Title Bar */
    #title {
        background-color: #35505B;
        font-size: 20px;
        padding: 10px 20px 10px 20px;
        color: #fff;
    }

    /* Contact list */
    #contacts {
        float: left;
        width: 220px;
        padding: 15px;
        background-color: #bbedfe;
        height: 580px;
        border-radius: 0 0 0 10px;
        overflow: auto;
        display: none;
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

    /* Chat */
    #chat {
        float: right;
        width: calc(100% - 220px);
        display: none;
    }
    #messages {
        height: 500px;
        padding: 20px 20px 0 20px;
        overflow-y: auto;
    }
    #messages .message {
        margin-bottom: 3px;
    }
    #messages .message .content {
        min-width: 20px;
        max-width: 250px;
        border-radius: 20px;
        padding: 10px;
        display: table;
    }
    #messages .message.event .content {
        background-color: #FF7E29;
        color: #fff;
        margin-left: auto;
        margin-right: auto;
        padding: 7px 20px;
    }
    #messages .message.received {
        left: 0;
        text-align: left;
    }
    #messages .message.received .content {
        background-color: gainsboro;
        border-radius: 0 20px 20px;
    }
    #messages .message.received .sender {
        color: dimgrey;
    }
    #messages .message.sent {
        right: 0;
        text-align: right;
    }
    #messages .message.sent .content {
        background-color: #0084FF;
        margin-left: auto;
        margin-right: 0;
        color: #fff;
        text-align: right;
        border-radius: 20px 0 20px 20px;
    }
    #messages .message.sent .sender {
        color: #0084FF;
    }
    #chat-controls {
        height: 40px;
        padding: 20px 20px 0 20px;
    }
    #chat-controls button {
        float: right;
    }
    #chat-controls input[type="text"] {
        width: calc(100% - 5em);
        float: left;
        overflow: auto;
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

    function signIn() {
        currentUser = $("#username").val().trim();
        if (currentUser) {
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
            $("#authentication").hide();
            $("#contacts").show();
            $("#chat").show();
            $("#message").focus();

            var chatMessage = {
                sendTextMessage: {
                    type: 'JOIN',
                    username: currentUser
                }
            };
            socket.send(JSON.stringify(chatMessage));
        };

        socket.onmessage = function (event) {
            //displayMessage('test', event.data);
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
        }
    }

    function displayMessage(username, text) {
        var sentByCurrentUer = currentUser === username;

        var message = document.createElement("div");
        message.setAttribute("class", sentByCurrentUer === true ? "message sent" : "message received");
        message.dataset.sender = username;

        var sender = document.createElement("span");
        sender.setAttribute("class", "sender");
        sender.appendChild(document.createTextNode(sentByCurrentUer === true ? "You" : username));
        message.appendChild(sender);

        var content = document.createElement("span");
        content.setAttribute("class", "content");
        content.appendChild(document.createTextNode(text));
        message.appendChild(content);

        var messages = document.getElementById("messages");
        var lastMessage = messages.lastChild;
        if (lastMessage && lastMessage.dataset.sender && lastMessage.dataset.sender === username) {
            message.className += " same-sender-previous-message";
        }

        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }

    function displayConnectedUserMessage(username) {
        var sentByCurrentUer = (currentUser === username);

        var message = document.createElement("div");
        message.setAttribute("class", "message event");

        var text = (sentByCurrentUer === true ? "Welcome " + username : username + " joined the chat");
        var content = document.createElement("span");
        content.setAttribute("class", "content");
        content.appendChild(document.createTextNode(text));
        message.appendChild(content);

        var messages = document.getElementById("messages");
        messages.appendChild(message);
    }

    function displayDisconnectedUserMessage(username) {
        var message = document.createElement("div");
        message.setAttribute("class", "message event");

        var text = username + " left the chat";
        var content = document.createElement("span");
        content.setAttribute("class", "content");
        content.appendChild(document.createTextNode(text));
        message.appendChild(content);

        var messages = document.getElementById("messages");
        messages.appendChild(message);
    }

    function addAvailableUsers(username) {
        var contact = document.createElement("div");
        contact.setAttribute("class", "contact");

        var status = document.createElement("div");
        status.setAttribute("class", "status");
        contact.appendChild(status);

        var content = document.createElement("span");
        content.setAttribute("class", "name");
        content.appendChild(document.createTextNode(username));
        contact.appendChild(content);

        var contacts = document.getElementById("contacts");
        contacts.appendChild(contact);
    }

    function cleanAvailableUsers() {
        var contacts = document.getElementById("contacts");
        while (contacts.hasChildNodes()) {
            contacts.removeChild(contacts.lastChild);
        }
    }
</script>