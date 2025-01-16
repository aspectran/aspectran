function FrontBuilder() {

    const endpoints = [];

    this.build = function (basePath, token, currentEndpoint, joinGroups) {
        $.ajax({
            url: basePath + "backend/endpoints/" + token,
            type: 'get',
            dataType: "json",
            success: function (data) {
                if (data) {
                    endpoints.length = 0;
                    let index = 0;
                    for (let key in data.endpoints) {
                        let endpoint = data.endpoints[key];
                        endpoint['index'] = index++;
                        endpoint['basePath'] = basePath;
                        endpoint['token'] = data.token;
                        if (!currentEndpoint || currentEndpoint === endpoint.name) {
                            endpoints.push(endpoint);
                        }
                    }
                    if (endpoints.length) {
                        establishEndpoint(0, joinGroups);
                    }
                }
            }
        });
    };

    const establishEndpoint = function (endpointIndex, joinGroups) {
        console.log('endpointIndex', endpointIndex);
        function onEndpointJoined(endpoint, payload) {
            buildEndpointView(endpoint, payload);
            for (let key in payload.messages) {
                let msg = payload.messages[key];
                endpoint.viewer.processMessage(msg);
            }
        }
        function onEstablishCompleted(endpoint) {
            if (endpoint.index < endpoints.length - 1) {
                establishEndpoint(endpoint.index + 1, joinGroups);
            } else if (endpoint.index === endpoints.length - 1) {
                initEndpointViews();
                if (endpoints.length) {
                    changeEndpoint(0);
                    changeGroup();
                    if (location.hash) {
                        let groupName = location.hash.substring(1);
                        changeGroup(groupName);
                    }
                }
            }
        }
        function onErrorObserved(endpoint) {
            setTimeout(function () {
                if (endpoint.index === 0) {
                    clearScreen();
                }
                let client = new PollingClient(endpoint, onEndpointJoined, onEstablishCompleted);
                endpoint['client'] = client;
                client.start(joinGroups);
            }, (endpoint.index - 1) * 1000);
        }

        if (endpointIndex === 0) {
            clearScreen();
        }

        let endpoint = endpoints[endpointIndex];
        console.log('endpoint', endpoint);
        endpoint['viewer'] = new FrontViewer(endpoint);
        let client = new WebsocketClient(endpoint, onEndpointJoined, onEstablishCompleted, onErrorObserved);
        endpoint['client'] = client;
        client.start(joinGroups);
    };

    const clearScreen = function () {
        $(".endpoint.tabs .tabs-title.available").remove();
        $(".endpoint.tabs .tabs-title").show();
        $(".endpoint-box.available").remove();
        $(".endpoint-box").show();
    };

    const changeEndpoint = function (endpointIndex) {
        for (let key in endpoints) {
            endpoints[key].viewer.setVisible(false);
        }
        $(".endpoint-box.available").hide().eq(endpointIndex).show();
        endpoints[endpointIndex].viewer.setVisible(true);
        endpoints[endpointIndex].viewer.refreshConsole();
    };

    const changeGroup = function (groupName) {
        let exists = false;
        $(".endpoint-box.available").each(function () {
            let $endpointBox = $(this);
            $endpointBox.find(".tabs-title.available").each(function () {
                if (!groupName) {
                    groupName = $(this).data("name");
                }
                if ($(this).data("name") === groupName) {
                    if (!$(this).hasClass("is-active")) {
                        $(this).addClass("is-active");
                        changeEndpointGroup($endpointBox, groupName);
                    }
                    exists = true;
                } else {
                    $(this).removeClass("is-active");
                }
            });
        });
        if (!exists && groupName) {
            changeGroup();
        }
    }

    const changeEndpointGroup = function ($endpointBox, groupName) {
        let $groupBox = $endpointBox.find(".group-box[data-name=" + groupName + "]");
        if ($groupBox.length) {
            $endpointBox.find(".group-box").hide();
            $groupBox.show();
            $groupBox.find(".track-box .bullet").remove();
            $groupBox.find(".log-box.available .log-console").each(function () {
                let $console = $(this);
                if (!$console.data("pause")) {
                    let endpointIndex = $endpointBox.data("index");
                    endpoints[endpointIndex].viewer.refreshConsole($console);
                }
            });
        }
    };

    const initEndpointViews = function () {
        $(".endpoint.tabs .tabs-title.available").eq(0).addClass("is-active");
        $(".endpoint.tabs .tabs-title.available a").click(function() {
            $(".endpoint.tabs .tabs-title").removeClass("is-active");
            let $tab = $(this).closest(".tabs-title").addClass("is-active");
            let endpointIndex = $tab.data("index");
            changeEndpoint(endpointIndex);
        });
        $(".endpoint-box.available .group.tabs .tabs-title.available a").click(function() {
            let $groupTab = $(this).closest(".tabs-title");
            let groupName = $groupTab.data("name");
            changeGroup(groupName);
        });
        $(".log-box .tailing-switch").click(function() {
            let $console = $(this).closest(".log-box").find(".log-console");
            let endpointIndex = $console.data("endpoint-index");
            if ($console.data("tailing")) {
                $console.data("tailing", false);
                $(this).find(".tailing-status").removeClass("on");
            } else {
                $console.data("tailing", true);
                $(this).find(".tailing-status").addClass("on");
                let endpoint = endpoints[endpointIndex];
                endpoint.viewer.refreshConsole($console);
            }
        });
        $(".log-box .pause-switch").click(function() {
            let $console = $(this).closest(".log-box").find(".log-console");
            if ($console.data("pause")) {
                $console.data("pause", false);
                $(this).removeClass("on");
            } else {
                $console.data("pause", true);
                $(this).addClass("on");
            }
        });
        $(".log-box .clear-screen").click(function() {
            let $console = $(this).closest(".log-box").find(".log-console");
            let endpointIndex = $console.data("endpoint-index");
            let endpoint = endpoints[endpointIndex];
            endpoint.viewer.clearConsole($console);
        });
        $(".layout-options li a").click(function() {
            let $liStacked = $(".layout-options li.stacked");
            let $liTabbed = $(".layout-options li.tabbed");
            let $li = $(this).parent();
            if (!$li.hasClass("on")) {
                if ($li.hasClass("tabbed")) {
                    $liTabbed.addClass("on");
                    $liStacked.removeClass("on");
                    $(".endpoint-box").removeClass("stacked");
                } else if ($li.hasClass("stacked")) {
                    $liTabbed.removeClass("on");
                    $liStacked.addClass("on");
                    $(".endpoint-box").addClass("stacked");
                } else if ($li.hasClass("compact")) {
                    $li.addClass("on");
                    $(".endpoint-box").addClass("compact")
                        .find(".log-box.available")
                            .addClass("large-6");
                }
            } else {
                if ($li.hasClass("compact")) {
                    $li.removeClass("on");
                    $(".endpoint-box").removeClass("compact")
                        .find(".log-box.available")
                            .removeClass("large-6");
                }
            }
            let $endpointBox = $(this).closest(".endpoint-box");
            let endpointIndex = $endpointBox.data("index");
            $endpointBox.find(".log-box.available").each(function () {
                if ($(this).find(".tailing-status").hasClass("on")) {
                    endpoints[endpointIndex].viewer.refreshConsole();
                }
            });
        });
        $(".speed-options li").click(function() {
            let $endpointBox = $(this).closest(".endpoint-box");
            let endpointIndex = $endpointBox.data("index");
            let $liFast = $(".speed-options li.fast");
            if ($liFast.hasClass("on")) {
                $liFast.removeClass("on");
                endpoints[endpointIndex].client.speed(0);
            } else {
                $liFast.addClass("on");
                endpoints[endpointIndex].client.speed(1);
            }
        });
    };

    const buildEndpointView = function (endpoint, payload) {
        let $endpointBox = addEndpointBox(endpoint);
        let indicatorEndpoint = $(".endpoint.tabs .tabs-title.available .indicator").eq(endpoint.index);
        endpoint.viewer.putIndicator("endpoint", "event", endpoint.index, indicatorEndpoint);
        for (let key in payload.groups) {
            let groupInfo = payload.groups[key];
            addGroupBox($endpointBox, groupInfo);
            let $indicatorGroup = $endpointBox
                .find(".group.tabs .tabs-title[data-name=" + groupInfo.name + "], .group-box[data-name=" + groupInfo.name + "] .tabs-title")
                .find(".indicator");
            endpoint.viewer.putIndicator("group", "event", groupInfo.name, $indicatorGroup);
            for (let key in groupInfo.events) {
                let eventInfo = groupInfo.events[key];
                if (eventInfo.name === "request") {
                    let $trackBox = addTrackBox($endpointBox, eventInfo);
                    let $reqNum = $trackBox.find(".req-num");
                    endpoint.viewer.putDisplay(groupInfo.name, eventInfo.name, $trackBox);
                    endpoint.viewer.putIndicator(groupInfo.name, "event", eventInfo.name, $reqNum);
                } else {
                    let $displayBox = addDisplayBox($endpointBox, eventInfo);
                    endpoint.viewer.putDisplay(groupInfo.name, eventInfo.name, $displayBox);
                }
            }
            for (let key in groupInfo.logs) {
                let logInfo = groupInfo.logs[key];
                let $logBox = addLogBox($endpointBox, logInfo);
                let $console = $logBox.find(".log-console").data("tailing", true);
                $logBox.find(".tailing-status").addClass("on");
                endpoint.viewer.putConsole(groupInfo.name, logInfo.name, $console);
                let $indicatorLog = $logBox.find(".status-bar");
                endpoint.viewer.putIndicator(groupInfo.name, "log", logInfo.name, $indicatorLog);
            }
        }
        if (endpoint.mode === "polling") {
            $("ul.speed-options").show();
        }
    };

    const addEndpointBox = function (endpointInfo) {
        let $tabs = $(".endpoint.tabs");
        let $tab0 = $tabs.find(".tabs-title").eq(0);
        let $tab = $tab0.hide().clone()
            .addClass("available")
            .attr("data-index", endpointInfo.index)
            .attr("data-name", endpointInfo.name)
            .attr("data-title", endpointInfo.title)
            .attr("data-endpoint", endpointInfo.url);
        $tab.find("a .title").text(" " + endpointInfo.title + " ");
        $tab.show().appendTo($tabs);
        let $endpointBox = $(".endpoint-box");
        return $endpointBox.eq(0).hide().clone()
            .addClass("available")
            .attr("data-index", endpointInfo.index)
            .attr("data-name", endpointInfo.name)
            .attr("data-title", endpointInfo.title)
            .insertAfter($endpointBox.last()).show();
    };

    const addGroupBox = function ($endpointBox, groupInfo) {
        let endpointTitle = $endpointBox.data("title");
        let $tabs = $endpointBox.find(".group.tabs");
        let $tab0 = $tabs.find(".tabs-title").eq(0);
        let index = $tabs.find(".tabs-title").length - 1;
        let $tab = $tab0.hide().clone()
            .addClass("available")
            .attr("data-index", index)
            .attr("data-name", groupInfo.name)
            .attr("title", endpointTitle + " ›› " + groupInfo.title);
        $tab.find("a .title").text(" " + groupInfo.title + " ");
        $tab.show().appendTo($tabs);
        let $groupBox = $endpointBox.find(".group-box").eq(0).hide().clone();
        $groupBox.addClass("available")
            .attr("data-name", groupInfo.name)
            .attr("data-title", groupInfo.title)
            .appendTo($endpointBox);
        $groupBox.find(".tabs .tabs-title")
            .addClass("is-active")
            .find("a .title")
                .text(" " + groupInfo.title + " ");
        return $groupBox;
    };

    const addTrackBox = function ($endpointBox, eventInfo) {
        let $groupBox = $endpointBox.find(".group-box[data-name=" + eventInfo.group + "]");
        let $trackBox = $groupBox.find(".track-box").eq(0).hide().clone()
            .addClass("available")
            .attr("data-group", eventInfo.group)
            .attr("data-name", eventInfo.name);
        return $trackBox.appendTo($groupBox.find("> .grid-x")).show();
    };

    const addLogBox = function ($endpointBox, logInfo) {
        let endpointIndex = $endpointBox.data("index");
        let endpointTitle = $endpointBox.data("title");
        let $groupBox = $endpointBox.find(".group-box[data-name=" + logInfo.group + "]");
        let $logBox = $groupBox.find(".log-box").eq(0).hide().clone()
            .addClass("large-6 available")
            .attr("data-group", logInfo.group)
            .attr("data-name", logInfo.name);
        $logBox.find(".status-bar h4")
            .text(endpointTitle + " ›› " + logInfo.file);
        $logBox.find(".log-console")
            .attr("data-endpoint-index", endpointIndex)
            .attr("data-endpoint-name", endpointTitle)
            .attr("data-log-name", logInfo.name);
        return $logBox.appendTo($groupBox.find("> .grid-x")).show();
    };

    const addDisplayBox = function (endpointBox, eventInfo) {
        let $groupBox = endpointBox.find(".group-box[data-name=" + eventInfo.group + "]");
        let $displayBox = $groupBox.find(".display-box").eq(0).hide().clone()
            .addClass("available")
            .attr("data-group", eventInfo.group)
            .attr("data-name", eventInfo.name);
        return $displayBox.appendTo($groupBox.find("> .grid-x")).show();
    };
}
