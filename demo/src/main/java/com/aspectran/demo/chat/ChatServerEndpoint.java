/*
 * Copyright (c) 2008-2019 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.demo.chat;

import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.demo.examples.hello.HelloAction;
import com.aspectran.web.socket.jsr356.ActivityContextAwareEndpoint;
import com.aspectran.web.socket.jsr356.AspectranConfigurator;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * <p>Created: 29/09/2019</p>
 */
@Component
@ServerEndpoint(value = "/chat", configurator = AspectranConfigurator.class)
public class ChatServerEndpoint extends ActivityContextAwareEndpoint {

    private static final Log log = LogFactory.getLog(ChatServerEndpoint.class);

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        log.debug(session.getId() + ": Connected");

        HelloAction helloAction = getBeanRegistry().getBean("helloAction");

        log.debug(helloAction.helloWorld());

        session.getBasicRemote().sendText(session.getId() + ": Connected");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        log.debug(session.getId() + ": " + message);

        for (Session s : session.getOpenSessions()) {
            s.getAsyncRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.debug(session.getId() + ": Disconnected: " + reason.getCloseCode().getCode());
    }

}
