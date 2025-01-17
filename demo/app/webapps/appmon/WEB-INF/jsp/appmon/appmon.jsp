<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<link rel="stylesheet" href="<aspectran:url value="/assets/appmon/css/appmon.css?20250114"/>">
<script src="https://aspectran.github.io/assets/countries/countries.js"></script>
<script src="<aspectran:url value="/assets/appmon/js/front-builder.js?20250114"/>"></script>
<script src="<aspectran:url value="/assets/appmon/js/front-viewer.js?20250114"/>"></script>
<script src="<aspectran:url value="/assets/appmon/js/websocket-client.js?20250114"/>"></script>
<script src="<aspectran:url value="/assets/appmon/js/polling-client.js?20250114"/>"></script>
<div class="grid-x endpoint-box compact horizontal">
    <div class="cell options t10 b5">
        <ul class="speed-options">
            <li class="fi-fast-forward fast" title="Set to poll every second. Turn this option on only when absolutely necessary."></li>
        </ul>
        <ul class="layout-options">
            <li class="fi-layout tabbed on" data-columns="0"><a> Tabbed</a></li>
            <li class="fi-layout stacked" data-columns="0"><a> Stacked</a></li>
            <li class="fi-layout compact on horizontal hide-for-small-only"><a> Compact</a></li>
        </ul>
    </div>
    <dl class="cell group tabs b0">
        <dd class="tabs-title"><a><span class="bullet fi-list-bullet"></span>
            <span class="title"> </span> <span class="indicator fi-loop"></span></a>
        </dd>
    </dl>
    <div class="cell group-box">
        <div class="grid-x">
            <dl class="cell tabs b0">
                <dd class="tabs-title"><a><span class="bullet fi-list-bullet"></span>
                    <span class="title"> </span> <span class="indicator fi-loop"></span></a>
                </dd>
            </dl>
            <div class="cell track-box">
                <div class="track-stack"><div class="req-num"></div></div>
            </div>
            <div class="cell display-box">
                <div class="grid-x">
                    <div class="cell small-12 large-4">
                        <div class="panel">
                            <dl class="session-statistics">
                                <dt title="The number of active sessions">Current Active Sessions</dt>
                                <dd><span class="number numberOfActives">0</span></dd>
                                <dt title="The highest number of sessions that have been active at a single time">Highest Active Sessions</dt>
                                <dd><span class="number highestNumberOfActives">0</span></dd>
                                <dt title="The number of sessions created since system bootup">Created Sessions</dt>
                                <dd><span class="number numberOfCreated">0</span></dd>
                                <dt title="The number of expired sessions">Expired Sessions</dt>
                                <dd><span class="number numberOfExpired">0</span></dd>
                                <dt title="This number of sessions includes sessions that are inactive or have been transferred to a session manager on another clustered server">Unmanaged Sessions</dt>
                                <dd><span class="number numberOfUnmanaged">0</span></dd>
                                <dt title="The number of rejected sessions">Rejected Sessions</dt>
                                <dd><span class="number numberOfRejected">0</span></dd>
                            </dl>
                            <p class="text-right"><i>Elapsed <span class="elapsed"></span></i></p>
                        </div>
                    </div>
                    <div class="cell small-12 large-8">
                        <div class="panel sessions-box">
                            <ul class="sessions">
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            <div class="cell log-box">
                <div class="status-bar">
                    <h4 class="ellipses"></h4>
                    <a href="#" class="tailing-switch" title="Scroll to End of Log">
                        <span class="tailing-status"></span>
                    </a>
                    <a href="#" class="clear-screen" title="Clear screen">
                        <span class="icon fi-x"></span>
                    </a>
                    <a href="#" class="pause-switch" title="Pause log output">
                        <span class="icon fi-pause"></span>
                    </a>
                </div>
                <pre class="log-console"></pre>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {
        const token = "${page.token}";
        const endpoint = "${page.endpoint}";
        new FrontBuilder().build("<aspectran:url value="/"/>", token, endpoint);
    });
</script>
