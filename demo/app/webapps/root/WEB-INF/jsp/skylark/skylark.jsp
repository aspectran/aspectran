<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<div id="skylark-terminal"></div>
<script src="https://unpkg.com/jquery.terminal/js/jquery.terminal.min.js"></script>
<link href="https://unpkg.com/jquery.terminal/css/jquery.terminal.min.css" rel="stylesheet"/>
<script>
    const backend = "<aspectran:url value="/"/>";
    $(function () {
        $('#skylark-terminal').terminal(function(command, term) {
            if (command !== '') {
                term.pause();
                $.ajax({
                    url: backend + 'skylark/api/v0/tts',
                    data: {
                        text: command
                    },
                    method: 'GET',
                    dataType: 'text',
                    success: function(data) {
                        if (data) {
                            if (data.indexOf("data:audio/wav;base64,") === 0) {
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
                        if (xhr.status === '413') {
                            alert("The text provided was too long.");
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
            greetings: 'Online Text-To-Speech Web Application\n=====================================\nType what you want to say.',
            name: 'skylark',
            height: 500,
            width: "100%",
            prompt: '> '
        });
    });
</script>
