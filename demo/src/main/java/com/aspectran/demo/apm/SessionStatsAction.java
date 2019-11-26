package com.aspectran.demo.apm;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.undertow.server.TowServer;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.servlet.api.DeploymentManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SessionStatsAction {

    @Request("/apm/getSessionStats")
    @Transform(TransformType.JSON)
    public Map<String, Object> getSessionStats(Translet translet) {
        TowServer towServer = translet.getBean("tow.server");
        DeploymentManager deploymentManager = towServer.getTowServletContainer().getDeployment("root.war");
        SessionManager sessionManager = deploymentManager.getDeployment().getSessionManager();
        SessionManagerStatistics statistics = sessionManager.getStatistics();

        Map<String, Object> stats = new HashMap<>();

        // Session Statistics
        long activeSessionCount = statistics.getActiveSessionCount();
        long highestSessionCount = statistics.getHighestSessionCount();
        long createdSessionCount = statistics.getCreatedSessionCount();
        long expiredSessionCount = statistics.getExpiredSessionCount();
        long rejectedSessionCount = statistics.getRejectedSessions();
        stats.put("activeSessionCount", activeSessionCount);
        stats.put("highestSessionCount", highestSessionCount);
        stats.put("createdSessionCount", createdSessionCount);
        stats.put("expiredSessionCount", expiredSessionCount);
        stats.put("rejectedSessionCount", rejectedSessionCount);

        // Current Users
        List<String> currentUsers = new ArrayList<>();
        Set<String> sessionIds = sessionManager.getActiveSessions();
        for (String sessionId : sessionIds) {
            Session session = sessionManager.getSession(sessionId);
            if (session != null) {
                currentUsers.add("Session " + session.getId() + " created at " + formatTime(session.getCreationTime()));
            }
        }
        stats.put("currentUsers", currentUsers);

        return stats;
    }

    private String formatTime(long time) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        return date.toString();
    }

}