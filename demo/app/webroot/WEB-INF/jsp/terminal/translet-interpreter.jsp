<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<div class="grid-x grid-padding-x">
    <div class="cell contour">
        <div id="term-demo"></div>
    </div>
</div>
<style>
    body.plate .cell.contour {
        background-color: #000;
    }
    #term-demo {
        padding: 15px 0 0 0;
    }
    .terminal-wrapper textarea {
        box-shadow: none;
        min-height: initial;
        min-width: initial;
    }
</style>
<script src="https://unpkg.com/jquery.terminal/js/jquery.terminal.min.js"></script>
<link href="https://unpkg.com/jquery.terminal/css/jquery.terminal.min.css" rel="stylesheet"/>
<script>
    const backend = "<aspectran:url value="/"/>";
    $(function() {
        $('#term-demo').terminal(function(command, term) {
            if (command !== '') {
                term.pause();
                $.ajax({
                    url: backend + 'terminal/query/' + command,
                    method: 'GET',
                    dataType: 'json',
                    success: function(data) {
                        let prompts = [];
                        let translet = data.translet;
                        let request = data.request;
                        let response = data.response;
                        if (!translet) {
                            return;
                        }
                        if (request) {
                            let prev = null;
                            let params = request.parameters;
                            if (params && params.tokens) {
                                for (let i = 0; i < params.tokens.length; i++) {
                                    let token = params.tokens[i];
                                    let item = {
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
                        }
                        if (prompts.length > 0) {
                            prompts[prompts.length - 1].terminator = true;
                            enterEachToken(term, prompts[0]);
                        } else {
                            let prompt = {
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
            greetings: 'Translet Interpreter\n====================\nType "hello" or "login"',
            name: 'transletInterpreter',
            height: 500,
            width: "100%",
            prompt: 'Aspectran> '
        });
    });
    function enterEachToken(term, prompt, retried) {
        if (!prompt.prev || prompt.group !== prompt.prev.group) {
            if (prompt.prev) {
                let root = checkMandatory(term, prompt.prev, retried);
                if (root) {
                    if (!retried) {
                        enterEachToken(term, root, true);
                    }
                    return;
                }
            }
            if (!retried) {
                term.echo("Required " + prompt.group + ":");
                let items = prompt.items;
                for (let i = 0; i < items.length; i++) {
                    term.echo("   " + items[i].name + ": " + items[i].tokenString);
                }
            }
            term.echo("Enter a value for each token:");
        }
        let curr = prompt.prev;
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
                let root = checkMandatory(term, prompt, retried);
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
            let token = prompt.token;
            if (!value && token.defaultValue) {
                value = token.defaultValue;
            }
            let mandatory = token.mandatory;
            if (mandatory && value === '') {
                prompt.done = false;
            } else {
                prompt.value = value;
                prompt.done = true;
            }
            term.pop();
            if (prompt.terminator) {
                let root = checkMandatory(term, prompt, retried);
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
        let root = prompt;
        while (root.prev) {
            if (!root.prev || prompt.group !== root.prev.group) {
                break;
            }
            root = root.prev;
        }
        let arr = [];
        let curr = root;
        while (curr) {
            if (!curr.done && prompt.group === curr.group) {
                arr.push(curr);
            }
            curr = curr.next;
        }
        if (arr.length > 0) {
            if (retried) {
                term.echo("Missing required " + root.group + ":");
                let items = root.items;
                let itemNames = [];
                for (let i = 0; i < arr.length; i++) {
                    let name = arr[i].token.name;
                    for (let j = 0; j < items.length; j++) {
                        for (let k = 0; k < items[j].tokenNames.length; k++) {
                            let name2 = items[j].tokenNames[k];
                            if (name === name2) {
                                let name3 = items[j].name;
                                let exists = false;
                                for (let l = 0; l < itemNames.length; l++) {
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
                for (let l = 0; l < itemNames.length; l++) {
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
        let root = prompt;
        while (root.prev) {
            root = root.prev;
        }
        let params = {};
        let curr = root;
        while (curr && curr.token) {
            params[curr.token.name] = curr.value;
            curr = curr.next;
        }
        term.pause();
        $.ajax({
            url: backend + 'terminal/exec/' + prompt.command,
            data: params,
            method: 'POST',
            dataType: 'text',
            success: function (data) {
                if (data) {
                    if (prompt.contentType.indexOf("audio/") === 0) {
                        let html = "<audio controls autoplay>" +
                            "<source src=\"" + data + "\" type='audio/wav'>" +
                            "Your browser does not support the audio element.</audio>";
                        term.echo(html, {raw: true});
                    } else {
                        term.echo(data);
                    }
                }
            },
            error: function (xhr) {
                if (xhr.status === '413') {
                    alert("The text provided was too long.");
                }
            },
            complete: function () {
                term.resume();
            }
        });
    }
</script>
