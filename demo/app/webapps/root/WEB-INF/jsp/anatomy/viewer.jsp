<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/default.min.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<style>
    .content-row, .content-row > div {
        height: calc(100vh - 200px); /* Adjust height based on your template's header/footer */
        min-height: 500px;
    }
    .scrollable-pane {
        height: 100%;
        overflow-y: auto;
    }
    #component-tree .list-group-item {
        cursor: pointer;
        border-radius: 0;
    }
    #component-tree .rule-item {
        padding-left: 2.5rem;
    }
    #details-content {
        height: 100%;
    }
</style>
<div class="row content-row g-3 pt-3">
    <div class="col-md-4 h-100">
        <div class="card h-100">
            <div class="card-header">
                <h5 class="mb-0">Components</h5>
            </div>
            <div class="p-2 border-bottom">
                <input type="text" id="rule-filter" class="form-control" placeholder="Filter rules...">
            </div>
            <div class="scrollable-pane p-0">
                <div id="component-tree" class="list-group list-group-flush"></div>
            </div>
        </div>
    </div>
    <div class="col-md-8 h-100">
        <div class="card h-100">
            <div class="card-header">
                <h5>Rule Details (APON)</h5>
            </div>
            <div class="card-body scrollable-pane">
                <pre id="details-content">Select a rule from the left panel to see details.</pre>
            </div>
        </div>
    </div>
</div>

<script>
    $(function() {
        const $componentTree = $('#component-tree');
        const $detailsContent = $('#details-content');

        $.getJSON('<%= request.getContextPath() %>/anatomy/data', function(data) {
            const anatomyData = data.anatomyData;
            if (!anatomyData) {
                $detailsContent.text('Failed to load anatomy data.');
                return;
            }

            $componentTree.empty();

            $.each(anatomyData, function(componentKey, rules) {
                console.log('Processing componentKey:', componentKey);
                const componentId = 'component-' + componentKey;
                const $componentHeader = $('<a/>', {
                    'href': '#' + componentId,
                    'class': 'list-group-item list-group-item-action fw-bold',
                    'data-bs-toggle': 'collapse'
                });
                const $badge = $('<span/>', {
                    'class': 'badge bg-secondary float-end',
                    'text': rules.length
                });
                $componentHeader.append(document.createTextNode(componentKey + ' '));
                $componentHeader.append($badge);

                const $ruleList = $('<div/>', {
                    'id': componentId,
                    'class': 'collapse'
                });

                $componentTree.append($componentHeader).append($ruleList);

                if (rules.length > 0) {
                    $.each(rules, function(index, rule) {
                        const ruleName = rule.name || rule.id || rule.className || 'Unnamed Rule';
                        const $ruleItem = $('<a/>', {
                            'href': '#',
                            'class': 'list-group-item list-group-item-action rule-item',
                            'text': ruleName
                        });

                        $ruleItem.on('click', function(e) {
                            e.preventDefault();
                            $componentTree.find('.list-group-item.active').removeClass('active');
                            $(this).addClass('active');
                            $detailsContent.text(rule.apon);
                            hljs.highlightElement($detailsContent[0]);
                        });

                        $ruleList.append($ruleItem);
                    });
                } else {
                    $ruleList.append('<div class="list-group-item text-muted rule-item">No rules defined.</div>');
                }
            });
        }).fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Error fetching anatomy data:', textStatus, errorThrown);
            $detailsContent.text('Error fetching anatomy data: ' + textStatus);
        });

        // Register a simple APON language for highlight.js
        hljs.registerLanguage('apon', function(hljs) {
            return {
                case_insensitive: false,
                keywords: {
                    keyword: 'translet bean aspect schedule template parameters attributes arguments properties item entry settings advice joinpoint pointcut echo action invoke include headers choose when otherwise transform dispatch forward redirect exception thrown description',
                    literal: 'true false null'
                },
                contains: [
                    hljs.COMMENT('#', ''),
                    {
                        className: 'string',
                        begin: /\s*:\s*/, end: /$/,
                        excludeBegin: true,
                        relevance: 0
                    },
                    {
                        className: 'number',
                        begin: '\\b\\d+(\\.\\d+)?',
                        relevance: 0
                    }
                ]
            };
        });

        // Initial highlighting
        $detailsContent.text("Select a rule from the left panel to see details.");

        $('#rule-filter').on('keyup', function() {
            const filterText = $(this).val().toLowerCase();

            // Iterate over each component group header
            $('#component-tree > .list-group-item-action').each(function() {
                const $header = $(this);
                const $ruleList = $header.next('.collapse');
                let hasVisibleRules = false;

                // Check children of this specific group
                $ruleList.children('.rule-item').each(function() {
                    const $ruleItem = $(this);
                    const ruleName = $ruleItem.text().toLowerCase();
                    if (ruleName.includes(filterText)) {
                        $ruleItem.show();
                        hasVisibleRules = true;
                    } else {
                        $ruleItem.hide();
                    }
                });

                // Show/hide the header based on whether it has any matching rules
                if (hasVisibleRules) {
                    $header.show();
                } else {
                    $header.hide();
                }
            });
        });
    });
</script>