<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <style>

        /* Common */

        * {
            margin: 0;
            padding: 0;
            font-family: sans-serif;
            font-size: 13px;
            outline: 0;
        }

        .error {
            color: #F04823;
            font-size: 12px;
        }

        input[type="text"],
        input[type="password"] {
            border-radius: 20px;
            padding: 10px;
            border: 2px solid gainsboro;
        }

        button {
            border-radius: 20px;
            padding: 10px;
            background-color: #0084FF;
            color: #fff;
            width: 6em;
            border: 2px solid #0084FF;
        }

        button:hover,
        button:focus {
            background-color: #0966ff;
            border-color: #0966ff;
            cursor: pointer;
        }

        /* Authentication */

        #authentication {
            margin-top: 100px;
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

        /* Title Bar */

        #title {
            background-color: #0084FF;
            font-size: 20px;
            padding: 10px 20px 10px 20px;
            color: #fff;
        }

        /* Contact list */

        #contacts {
            float: left;
            width: 120px;
            padding: 20px;
            background-color: aliceblue;
            min-height: calc(100vh - 85px);
            max-height: calc(100vh - 85px);
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
            line-height: 40px;
            vertical-align: super;
        }

        /* Chat */

        #chat {
            float: right;
            width: calc(100% - 160px);
            display: none;
        }

        #messages {
            min-height: calc(100vh - 130px);
            max-height: calc(100vh - 130px);
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
            font-size: 10px;
            padding: 7px;
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
            font-size: 10px;
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
            font-size: 10px;
        }

        #chat-controls {
            height: 40px;
            padding: 20px 20px 0 20px;
        }

        #chat-controls button {
            float: right;
        }

        #chat-controls input[type="text"] {
            width: calc(100% - 9em);
        }

        #messages .message.sent.same-sender-previous-message .sender,
        #messages .message.received.same-sender-previous-message .sender {
            display: none;
        }

        #messages .message:not(.same-sender-previous-message) {
            margin-top: 10px;
        }

    </style>

</head>
<body>

<div id="title">Chat</div>

<form id="authentication" onsubmit="return false;">
    <input type="text" id="username" placeholder="Username" autofocus/>
    <input type="password" id="password" placeholder="Password"/>
    <button class="button" onclick="signIn()">Sign In</button>
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

<script>

    var socket;
    var currentUser;

    function signIn() {

        var credentials = {
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        };

        currentUser = credentials.username;
        openSocket('');

/*
        var request = new XMLHttpRequest();
        request.open("POST", "http://localhost:8080/auth");
        request.setRequestHeader("Content-Type", "application/json");

        request.onreadystatechange = function () {

            if (request.readyState === XMLHttpRequest.DONE) {

                switch (request.status) {

                    case 200:
                        currentUser = credentials.username;
                        var webSocketAccessToken = JSON.parse(request.responseText);
                        openSocket(webSocketAccessToken.token);
                        break;

                    case 403:
                        currentUser = null;
                        document.getElementById("authentication-error").innerHTML = "Oops... These credentials are invalid.";
                        break;

                    default:
                        document.getElementById("authentication-error").innerHTML = "Oops... Looks like something is broken.";
                }
            }
        };

        request.send(JSON.stringify(credentials));

 */
    }

    function openSocket(accessToken) {

        if (socket) {
            socket.close();
        }

        socket = new WebSocket("ws://localhost:8080/chat?access-token=" + accessToken);

        socket.onopen = function (event) {
            document.getElementById("authentication").style.display = "none";
            document.getElementById("contacts").style.display = "block";
            document.getElementById("chat").style.display = "block";
            document.getElementById("message").focus();
        };

        socket.onmessage = function (event) {
            displayMessage('test', event.data);


            if (typeof event.data === "string") {

                var webSocketMessage = JSON.parse(event.data);
                switch (webSocketMessage.type) {

                    case "welcomeUser":
                        displayConnectedUserMessage(webSocketMessage.payload.username);
                        break;

                    case "broadcastTextMessage":
                        displayMessage(webSocketMessage.payload.username, webSocketMessage.payload.content);
                        break;

                    case "broadcastConnectedUser":
                        displayConnectedUserMessage(webSocketMessage.payload.username);
                        break;

                    case "broadcastDisconnectedUser":
                        displayDisconnectedUserMessage(webSocketMessage.payload.username);
                        break;

                    case "broadcastAvailableUsers":
                        cleanAvailableUsers();
                        for (var i = 0; i < webSocketMessage.payload.usernames.length; i++) {
                            addAvailableUsers(webSocketMessage.payload.usernames[i]);
                        }
                        break;
                }
            }
        };
    }

    function sendMessage() {

        var text = document.getElementById("message").value;
        document.getElementById("message").value = "";

        var payload = {
            content: text
        };

        var webSocketMessage = {
            type: "sendTextMessage"
        };

        webSocketMessage.payload = payload;

        socket.send(JSON.stringify(webSocketMessage));
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

        var sentByCurrentUer = currentUser === username;

        var message = document.createElement("div");
        message.setAttribute("class", "message event");

        var text = sentByCurrentUer === true ? "Welcome " + username : username + " joined the chat";
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