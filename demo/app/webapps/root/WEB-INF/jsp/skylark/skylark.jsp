<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="grid-x grid-padding-x">
    <div class="cell contour">
        <div id="skylark-term"></div>
    </div>
</div>
<style>
    body.plate .cell.contour {
        background-color: #000;
    }
    #skylark-term {
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
    $(function () {
        $('#skylark-term').terminal(function(command, term) {
            if (command !== '') {
                term.pause();
                $.ajax({
                    url: '/skylark/api/v0/tts',
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