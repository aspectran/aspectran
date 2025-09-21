<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<div class="row g-3 pt-3">
    <div class="col-lg-4" style="position:relative;">
        <h2>Customer List</h2>
        <span id="customer-total" class="float-end badge bg-warning"
              style="font-size:2em;position:absolute;top:0;right:10px;border-radius:50%;">0</span>
        <div id="customer-list-board" class="card card-body p-1 pe-0" style="height:361px;overflow-y:auto;">
            <div id="customer-list-group" class="list-group w-100" role="button">
                <!-- Customer list items will be appended here -->
            </div>
        </div>
        <div class="d-flex justify-content-between align-items-center">
        <div class="mt-2">
            <button type="button" class="btn btn-warning refresh-customers">Refresh</button>
        </div>
        <div class="mt-2">
            <button type="button" class="btn btn-primary add-customer">Add</button>
            <button type="button" class="btn btn-danger delete-customer">Delete</button>
        </div>
        </div>
    </div>
    <div class="col-lg-8" style="position:relative;">
        <h2>Customer Details</h2>
        <span id="customer-number" class="float-end badge bg-success"
              style="font-size:2em;position:absolute;top:0;right:10px;border-radius:50%;">0</span>
        <form id="customer-form">
            <div id="customer-details" class="card card-body">
                <div class="mb-3">
                    <label class="form-label">No.</label>
                    <input type="text" name="id" class="form-control" disabled="disabled"/>
                </div>
                <div class="mb-3">
                    <label class="form-label">Name</label>
                    <input type="text" name="name" class="form-control" maxlength="30"/>
                </div>
                <div class="mb-3">
                    <label class="form-label">Age</label>
                    <input type="number" name="age" class="form-control" min="1" max="199" maxlength="3"
                           oninput="if (this.value.length >= this.maxLength) this.value = this.value.slice(0, this.maxLength);"/>
                </div>
                <fieldset>
                    <legend>Approval State</legend>
                    <div class="form-check form-check-inline">
                        <input type="radio" name="approved" id="approved-radio" value="Y" class="form-check-input"
                               required><label for="approved-radio" class="form-check-label">Approved</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" name="approved" id="denied-radio" value="N" class="form-check-input"
                               required><label for="denied-radio" class="form-check-label">Denied</label>
                    </div>
                </fieldset>
            </div>
            <div class="float-end mt-2">
                <button type="button" class="btn btn-success save-customer">Save</button>
            </div>
        </form>
    </div>
    <div class="col-12">
        <div id="request-describe" class="input-group">
            <span class="input-group-text" style="white-space:nowrap;"></span>
            <input class="form-control" type="text" value="" readonly>
        </div>
    </div>
    <div class="col-12" style="position:relative;">
        <span id="response-status" class="float-start badge bg-secondary"
              style="font-size:2em;position:absolute;top:-30px;right:10px;border-radius:50%;">200</span>
        <pre id="response-body" style="min-height:100px;max-height:300px;"></pre>
    </div>
</div>
<script>
    <aspectran:profile expression="prod">
    const BASE_PATH = "/examples";
    </aspectran:profile>
    <aspectran:profile expression="!prod">
    const BASE_PATH = "${pageContext.request.contextPath}/examples";
    </aspectran:profile>

    $(function () {
        let customerForm = $("#customer-form");

        // Change event for the new dropdown items
        $("#customer-list-group").on("click", "a", function (e) {
            e.preventDefault();
            const id = $(this).data("id");

            // Add active class and scroll into view
            $("#customer-list-group a").removeClass("active"); // Remove active from others
            $(this).addClass("active"); // Add active to current

            scrollIntoView(this); // Scroll the clicked item into view

            $("#customer-details, #customer-number").stop(true).fadeOut(300);
            getCustomer(id);
            $("#customer-details, #customer-number").fadeIn(200);
        });

        $("button.refresh-customers").click(function () {
            $("#customer-list-board, #customer-total").stop(true).fadeOut(300);
            getCustomerList();
            $("#customer-list-board, #customer-total").fadeIn(200);
        });
        $("button.add-customer").click(function () {
            clearForm();
            customerForm.find("input[name=name]").focus();
        });
        $("button.save-customer").click(function () {
            saveCustomer();
        });
        $("button.delete-customer").click(function () {
            deleteCustomer();
        });
        customerForm.find("input[name=approved]").click(function () {
            updateApproval($(this).val() === "Y");
        });
        customerForm.find("input").focus(function () {
            $(this).removeClass("is-invalid");
        });

        getCustomerList();
    });

    // Function to scroll an element into view within its scrollable parent
    function scrollIntoView(element) {
        const container = $("#customer-list-board"); // Changed to customer-list-board as it's the scrollable parent
        const item = $(element);

        if (item.length === 0 || container.length === 0) {
            return; // Element or container not found
        }

        const containerTop = container.scrollTop();
        const containerBottom = containerTop + container.height();

        const itemTop = item.position().top + containerTop; // Absolute position relative to container's scroll top
        const itemBottom = itemTop + item.outerHeight();

        if (itemTop < containerTop) {
            // Item is above the current view, scroll up
            container.scrollTop(itemTop);
        } else if (itemBottom > containerBottom) {
            // Item is below the current view, scroll down
            container.scrollTop(itemBottom - container.height());
        }
    }

    function getCustomerList() {
        $.ajax({
            type: "GET",
            url: BASE_PATH + "/gs-rest-service/customers",
            dataType: "json",
            success: function (data, textStatus, xhr) {
                console.log(data);
                describe("GET", this.url, xhr);
                let listMenu = $("#customer-list-group");
                listMenu.empty();
                for (let i = 0; i < data.customers.length; i++) {
                    let id = data.customers[i].id;
                    let name = data.customers[i].name;
                    listMenu.append($("<a class='list-group-item list-group-item-action' data-id='" + id + "'>" + id + ". " + name + "</a>"));
                }
                clearForm();
                $("#customer-total").text(data.customers.length); // Update total count
            },
            error: function (xhr, status, error) {
                alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
            }
        });
    }

    function getCustomer(id) {
        let customerForm = $("#customer-form");
        customerForm.find("input.is-invalid").removeClass("is-invalid");
        $.ajax({
            type: "GET",
            url: BASE_PATH + "/gs-rest-service/customers/" + id,
            dataType: "json",
            success: function (data, textStatus, xhr) {
                describe(this.type, this.url, xhr);
                customerForm.find("input[name=id]").val(data.customer.id);
                customerForm.find("input[name=name]").val(data.customer.name);
                customerForm.find("input[name=age]").val(data.customer.age);
                customerForm.find("input[name=approved][value=" + (data.customer.approved ? "Y" : "N") + "]").prop("checked", true);
                $("#customer-number").text(data.customer.id);
            },
            error: function (xhr, status, error) {
                alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
            }
        });
    }

    function saveCustomer() {
        let customerForm = $("#customer-form");
        if (customerForm.find("input[name=name]").val() === "") {
            customerForm.find("input[name=name]").addClass("is-invalid");
        }
        if (customerForm.find("input[name=age]").val() === "") {
            customerForm.find("input[name=age]").addClass("is-invalid");
        }
        if (customerForm.find("input.is-invalid").length > 0) {
            return;
        }
        let type, url;
        let id = customerForm.find("input[name=id]").val();
        if (id) {
            type = "PUT";
            url = BASE_PATH + "/gs-rest-service/customers/" + id;
        } else {
            type = "POST";
            url = BASE_PATH + "/gs-rest-service/customers";
        }
        $.ajax({
            type: type,
            url: url,
            data: {
                name: customerForm.find("input[name=name]").val(),
                age: customerForm.find("input[name=age]").val(),
                approved: customerForm.find("input[name=approved][value=Y]").prop("checked")
            },
            dataType: "json",
            success: function (data, textStatus, xhr) {
                describe(this.type, this.url, xhr);
                if (id) {
                    // Update existing item in list-group
                    $("#customer-list-group a[data-id='" + data.customer.id + "']").text(data.customer.id + ". " + data.customer.name);
                } else {
                    customerForm.find("input[name=id]").val(data.id);
                    // Add new item to list-group
                    $("#customer-list-group").append($("<a class='list-group-item list-group-item-action' data-id='" + data.id + "'>" + data.id + ". " + data.name + "</a>"));
                    selectCustomer(data.id);
                }
                $("#customer-number").text(data.id);
                $("#customer-total").text($("#customer-list-group a").length); // Update total count
            },
            error: function (xhr, status, error) {
                if (xhr.status === 403) {
                    alert("Maximum number of customers exceeded");
                } else {
                    alert("An error has occurred making the request: " + error);
                }
            }
        });
    }

    function deleteCustomer() {
        let customerForm = $("#customer-form");
        let id = customerForm.find("input[name=id]").val();
        if (!id) {
            alert("Please select a customer to remove.");
            return;
        }
        $.ajax({
            type: "DELETE",
            url: BASE_PATH + "/gs-rest-service/customers/" + id,
            dataType: "json",
            success: function (data, textStatus, xhr) {
                describe(this.type, this.url, xhr);
                $("#customer-list-group a[data-id='" + id + "']").remove(); // Remove item from list-group
                clearForm();
                $("#customer-total").text($("#customer-list-group a").length); // Update total count
            },
            error: function (xhr, status, error) {
                alert("An error has occurred making the request: " + error);
            }
        });
    }

    function updateApproval(approved) {
        let customerForm = $("#customer-form");
        let id = customerForm.find("input[name=id]").val();
        if (!id) {
            return;
        }
        $.ajax({
            type: "PUT",
            url: BASE_PATH + "/gs-rest-service/customers/" + id + "/attributes",
            data: {
                approved: approved
            },
            dataType: "json",
            success: function (data, textStatus, xhr) {
                describe(this.type, this.url, xhr);
            },
            error: function (xhr, status, error) {
                alert("An error has occurred making the request: " + error);
            }
        });
    }

    function selectCustomer(id) {
        $("#customer-list-group").find("a").removeClass("active");
        $("#customer-list-group").find("a[data-id=" + id + "]").addClass("active").focus();
        $("#customer-list-board").scrollTop($("#customer-list-board").prop("scrollHeight"));
    }

    function clearForm() {
        let customerForm = $("#customer-form");
        customerForm.find("input[name=id]").val("");
        customerForm.find("input[name=name]").val("");
        customerForm.find("input[name=age]").val("");
        customerForm.find("input[name=approved][value=Y]").prop("checked", true);
        $("#customer-total").text($("#customer-list-group a").length);
        $("#customer-number").text("-");
        customerForm.find("input.is-invalid").removeClass("is-invalid");
        $("#customer-list-group a").removeClass("active"); // Remove active class from all items
    }

    function describe(method, url, xhr) {
        $("#request-describe span").text(method);
        $("#request-describe input").val(url);
        $("#response-status").stop(true).fadeOut(300);
        $("#response-status").text(xhr.status);
        $("#response-body").text(xhr.responseText);
        if (xhr.status == "201") {
            $("#response-body").prepend("<strong>Location: " + xhr.getResponseHeader('Location') + "</strong>\n");
        }
        $("#response-status").fadeIn(200);
    }
</script>