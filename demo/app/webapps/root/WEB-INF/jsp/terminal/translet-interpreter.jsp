<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style>
    #term-demo {
        border-radius: 0 0 10px 10px;
    }
    @media print, screen and (min-width: 64em) {
        section > .row {
            background-color: #000;
            border-radius: 0 0 10px 10px;
        }
    }
    .terminal-wrapper textarea {
        box-shadow: none;
        min-height: initial;
        min-width: initial;
    }
</style>
<div id="term-demo"></div>
<script src="https://cdn.rawgit.com/jcubic/jquery.terminal/master/js/jquery.terminal.min.js"></script>
<link href="https://cdn.rawgit.com/jcubic/jquery.terminal/master/css/jquery.terminal.min.css" rel="stylesheet"/>
<script>
    $(function() {
        $('#term-demo').terminal(function(command, term) {
            if (command !== '') {
                term.pause();
                $.ajax({
                    url: '/terminal/query/' + command,
                    method: 'GET',
                    dataType: 'json',
                    success: function(data) {
                        var prompts = [];
                        var translet = data.translet;
                        var request = data.request;
                        var response = data.response;
                        if (!translet) {
                            return;
                        }
                        if (request) {
                            var prev = null;
                            var params = request.parameters;
                            if (params && params.tokens) {
                                for (var i = 0; i < params.tokens.length; i++) {
                                    var token = params.tokens[i];
                                    var item = {
                                        command: command,
                                        contentType: response.contentType,
                                        group: 'parameters',
                                        prev: prev,
                                        next: null,
                                        token: token,
                                        items: params.items
                                    };
                                    prompts.push(item);
                                    if (prev) {
                                        prev.next = item;
                                    }
                                    prev = item;
                                }
                            }
                            var attrs = request.attributes;
                            if (attrs && attrs.tokens) {
                                for (var i = 0; i < attrs.tokens.length; i++) {
                                    var token = attrs.tokens[i];
                                    var item = {
                                        command: command,
                                        contentType: response.contentType,
                                        group: 'attributes',
                                        prev: prev,
                                        next: null,
                                        token: token,
                                        items: attrs.items
                                    };
                                    prompts.push(item);
                                    if (prev) {
                                        prev.next = item;
                                    }
                                    prev = item;
                                }
                            }
                        }
                        if (prompts.length > 0) {
                            prompts[prompts.length - 1].terminator = true;
                            enterEachToken(term, prompts[0]);
                        } else {
                            var prompt = {
                                command: command,
                                contentType: response.contentType
                            };
                            execCommand(term, prompt);
                        }
                    },
                    complete: function () {
                        term.resume();
                    }
                });
            } else {
                term.echo('');
            }
        }, {
            greetings: 'Translet Interpreter\nType "hello"',
            name: 'transletInterpreter',
            height: 500,
            width: "100%",
            prompt: 'Aspectran> '
        });
    });
    function enterEachToken(term, prompt, retried) {
        if (!prompt.prev || prompt.group !== prompt.prev.group) {
            if (prompt.prev) {
                var root = checkMandatory(term, prompt.prev, retried);
                if (root) {
                    if (!retried) {
                        enterEachToken(term, root, true);
                    }
                    return;
                }
            }
            if (!retried) {
                term.echo("Required " + prompt.group + ":");
                var items = prompt.items;
                for (var i = 0; i < items.length; i++) {
                    term.echo("   " + items[i].name + ": " + items[i].tokenString);
                }
            }
            term.echo("Enter a value for each token:");
        }
        var curr = prompt.prev;
        while (curr) {
            if (prompt.token.name === curr.token.name && curr.done) {
                prompt.value = curr.value;
                prompt.done = true;
                break;
            }
            curr = curr.prev;
        }
        if (prompt.done) {
            term.echo("   " + prompt.token.string + ": " + prompt.value);
            if (prompt.terminator) {
                var root = checkMandatory(term, prompt, retried);
                if (root) {
                    if (!retried) {
                        enterEachToken(term, root, true);
                    }
                    return;
                }
                execCommand(term, prompt);
            } else {
                enterEachToken(term, prompt.next, retried);
            }
            return;
        }
        term.push(function (value, term) {
            var token = prompt.token;
            var mandatory = token.mandatory;
            if (mandatory && value === '') {
                prompt.done = false;
            } else {
                prompt.value = value;
                prompt.done = true;
            }
            term.pop();
            if (prompt.terminator) {
                var root = checkMandatory(term, prompt, retried);
                if (root) {
                    if (!retried) {
                        enterEachToken(term, root, true);
                    }
                    return;
                }
                execCommand(term, prompt);
            } else {
                enterEachToken(term, prompt.next, retried);
            }
        }, {
            prompt: "   " + prompt.token.string + ": "
        });
    }
    function checkMandatory(term, prompt, retried) {
        var root = prompt;
        while (root.prev) {
            if (!root.prev || prompt.group !== root.prev.group) {
                break;
            }
            root = root.prev;
        }
        var arr = [];
        var curr = root;
        while (curr) {
            if (!curr.done && prompt.group === curr.group) {
                arr.push(curr);
            }
            curr = curr.next;
        }
        if (arr.length > 0) {
            if (retried) {
                term.echo("Missing required " + root.group + ":");
                var items = root.items;
                var itemNames = [];
                for (var i = 0; i < arr.length; i++) {
                    var name = arr[i].token.name;
                    for (var j = 0; j < items.length; j++) {
                        for (var k = 0; k < items[j].tokenNames.length; k++) {
                            var name2 = items[j].tokenNames[k];
                            if (name === name2) {
                                var name3 = items[j].name;
                                var exists = false;
                                for (var l = 0; l < itemNames.length; l++) {
                                    if (name3 === itemNames[l]) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    itemNames.push(name3);
                                }
                            }
                        }
                    }
                }
                for (var l = 0; l < itemNames.length; l++) {
                    term.echo("   " + itemNames[l]);
                }
            } else {
                term.echo("Required " + root.group + " are missing.");
            }
            return root;
        } else {
            return null;
        }
    }

    function execCommand(term, prompt) {
        var root = prompt;
        while (root.prev) {
            root = root.prev;
        }
        var params = {};
        var curr = root;
        while (curr && curr.token) {
            params[curr.token.name] = curr.value;
            curr = curr.next;
        }
        term.pause();
        $.ajax({
            url: '/terminal/exec/' + prompt.command,
            data: params,
            method: 'POST',
            dataType: 'text',
            success: function (data) {
                if (data) {
                    if (prompt.contentType.indexOf("audio/") === 0) {
                        var html = "<audio controls autoplay>" +
                            "<source src=\"" + data + "\" type='audio/wav'>" +
                            "Your browser does not support the audio element.</audio>";
                        term.echo(html, {raw: true});
                    } else {
                        term.echo(data);
                    }
                }
            },
            error: function (xhr) {
                if (xhr.status == '413') {
                    alert("The text provided was too long.");
                }
            },
            complete: function () {
                term.resume();
            }
        });
    }
</script>