<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="grid-x grid-padding-x">
    <div class="cell small-12 large-5 t20">
        <h3>Session Statistics</h3>
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
    <div class="cell small-12 large-7 t20">
        <h3>Current Users</h3>
        <div class="panel users-wrap">
            <ul class="users">
            </ul>
        </div>
    </div>
</div>
<div class="grid-x grid-padding-x">
    <div class="cell t20">
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
<link rel="stylesheet" href="/assets/css/apm.css?20200111">
<script src="/assets/js/apm.js"></script>
<script>
    $(function() {
        let sessionStats = new SessionStats("/apm/stats", 5);
        try {
            sessionStats.openSocket();
        } catch (e) {
            console.error("Socket connection failed to [" + sessionStats.endpoint + "]");
        }
    });
</script>
<script>
    $(function() {
        let logTailer = new LogTailer("/apm/logtail", "app-log");
        $(".bite-tail").click(function() {
            let logtail = $(this).closest(".log-container").find(".log-tail");
            logTailer.switchTailBite(logtail, !logtail.data("bite"));
        });
        try {
            logTailer.openSocket();
        } catch (e) {
            logTailer.printErrorMessage("Socket connection failed");
        }
    });
</script>