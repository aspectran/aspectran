<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<style>
    .left-panel, .right-panel {
        height: calc(100vh - 250px); /* Adjust height based on your template's header/footer */
        min-height: 500px;
    }
    .scrollable-pane {
        height: 100%;
        overflow-y: auto;
    }
</style>
<div class="row g-3 py-3">
    <div class="col-md-4 left-panel">
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
    <div class="col-md-8 right-panel">
        <div class="card h-100">
            <div class="card-header">
                <h5 class="mb-0">Rule Details (APON)</h5>
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

        $detailsContent.text("Select a rule from the left panel to see details.");

        $.getJSON('<%= request.getContextPath() %>/anatomy/data', function(data) {
            const anatomyData = data.anatomyData;
            if (!anatomyData) {
                $detailsContent.text('Failed to load anatomy data.');
                return;
            }

            $componentTree.empty();

            $.each(anatomyData, function(componentKey, rules) {
                console.log('Processing componentKey:', componentKey);
                const componentId = 'component-' + componentKey.replace(/\s+/g, '-');
                const $componentHeader = $('<a/>', {
                    'href': '#' + componentId,
                    'class': 'list-group-item list-group-item-action fw-bold',
                    'data-bs-toggle': 'collapse'
                });
                const $badge = $('<span/>', {
                    'class': 'badge bg-secondary float-end',
                    'text': rules.length
                });
                $componentHeader.append(componentKey + ' ');
                $componentHeader.append($badge);

                const $ruleList = $('<div/>', {
                    'id': componentId,
                    'class': 'list-group-flush border-bottom collapse'
                });

                $componentTree.append($componentHeader).append($ruleList);

                if (rules.length > 0) {
                    $.each(rules, function(index, rule) {
                        const ruleName = rule.name || rule.id || rule.className || 'Unnamed Rule';
                        const $ruleItem = $('<a/>', {
                            'href': '#',
                            'class': 'list-group-item list-group-item-secondary list-group-item-action rule-item',
                            'text': ruleName
                        });

                        $ruleItem.on('click', function(e) {
                            e.preventDefault();
                            $componentTree.find('.list-group-item.active').removeClass('active');
                            $(this).addClass('active');
                            $detailsContent.text(rule.apon);
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
