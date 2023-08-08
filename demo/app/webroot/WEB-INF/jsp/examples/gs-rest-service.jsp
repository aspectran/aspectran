<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="grid-x grid-padding-x">
  <div class="cell contour">
    <div class="grid-x grid-padding-x">
      <div class="cell large-4 t20" style="position:relative;">
        <h2 style="margin:0;">Customer List</h2>
        <span id="total" class="float-right warning badge" style="font-size:2em;position:absolute;top:0;right:20px;">0</span>
        <div id="customer-list-board" style="clear:both;border-radius:3px;margin-bottom:.5em;">
          <select name="customerList" size="15" style="height:auto;background-image:none;margin:0;">
          </select>
        </div>
        <div class="float-left">
          <button id="refresh" type="button" class="button warning">Refresh</button>
        </div>
        <div class="float-right">
          <button id="add" type="button" class="button">Add</button>
          <button id="delete" type="button" class="button alert">Delete</button>
        </div>
      </div>
      <div class="cell large-8 t20" style="position:relative;">
        <h2 style="margin:0;">Customer Details</h2>
        <span id="cust-no" class="float-right success badge" style="font-size:2em;position:absolute;top:0;right:0;border-radius:50% 0 0 50%;">0</span>
        <div id="details" class="panel radius">
          <form>
            <label>No.
              <input type="text" name="id" disabled="disabled"/>
            </label>
            <label>Name
              <input type="text" name="name" maxlength="30"/>
            </label>
            <label>Age
              <input type="number" name="age" min="1" max="199" maxlength="3" oninput="if (this.value.length >= this.maxLength) this.value = this.value.slice(0, this.maxLength);"/>
            </label>
            <fieldset>
              <legend>Approval Status</legend>
              <input type="radio" name="approved" id="approved" value="Y" required><label for="approved">Approved</label>
              <input type="radio" name="approved" id="denied" value="N" required><label for="denied">Denied</label>
            </fieldset>
          </form>
        </div>
        <div class="float-right t10">
          <button id="save" type="button" class="success button">Save</button>
        </div>
      </div>
      <div class="cell">
        <div id="describe" class="input-group">
          <span class="input-group-label" style="white-space:nowrap;"></span>
          <input class="input-group-field" type="text" value="" readonly>
        </div>
      </div>
      <div class="cell" style="position:relative;">
        <span id="response-status-code" class="float-left badge" style="font-size:2em;position:absolute;top:0;right:0;border-radius:0 0 0 50%;">200</span>
        <pre id="response-text" style="min-height:100px;max-height:300px;"></pre>
      </div>
    </div>
  </div>
</div>
<script>
  let backend = "/examples";
  $(function() {
    $("select[name=customerList]").change(function() {
      $("#details form, #cust-no").stop(true).fadeOut(300);
      getCustomer($(this).val());
      $("#details form, #cust-no").fadeIn(200);
    });
    $("button#refresh").click(function() {
      $("#customer-list-board, #total").stop(true).fadeOut(300);
      getCustomerList();
      $("#customer-list-board, #total").fadeIn(200);
    });
    $("button#add").click(function() {
      $("select[name=customerList] option:selected").removeAttr("selected");
      clearForm();
      $("input[name=name]").focus();
    });
    $("button#save").click(function() {
      saveCustomer();
    });
    $("button#delete").click(function() {
      deleteCustomer();
    });
    $("input[name=approved]").click(function() {
      updateApproval($(this).val() === "Y");
    });
    $("input").focus(function() {
      $(this).removeClass("is-invalid-input");
    });

    getCustomerList();
  });

  function getCustomerList() {
    $.ajax({
      type: "GET",
      url: backend + "/gs-rest-service/customers",
      dataType: "json",
      success: function(data, textStatus, xhr) {
        console.log(data);
        describe("GET", this.url, xhr);
        let list = $("select[name=customerList]");
        list.empty();
        for(let i = 0; i < data.customers.length; i++) {
          let id = data.customers[i].id;
          let name = data.customers[i].name;
          list.append($("<option/>").attr("value", id).text(id + ". " + name));
        }
        clearForm();
      },
      error: function(xhr, status, error) {
        alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
      }
    });
  }

  function getCustomer(id) {
    $("input.is-invalid-input").removeClass("is-invalid-input");
    $.ajax({
      type: "GET",
      url: backend + "/gs-rest-service/customers/" + id,
      dataType: "json",
      success: function(data, textStatus, xhr) {
        describe(this.type, this.url, xhr);
        $("input[name=id]").val(data.customer.id);
        $("input[name=name]").val(data.customer.name);
        $("input[name=age]").val(data.customer.age);
        $("input[name=approved][value=" + (data.customer.approved ? "Y" : "N") + "]").prop("checked", true);
        $("#cust-no").text(data.customer.id);
      },
      error: function(xhr, status, error) {
        alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
      }
    });
  }

  function saveCustomer() {
    if ($("input[name=name]").val() === "") {
      $("input[name=name]").addClass("is-invalid-input");
    }
    if ($("input[name=age]").val() === "") {
      $("input[name=age]").addClass("is-invalid-input");
    }
    if ($("input.is-invalid-input").length > 0) {
      return;
    }
    let type, url;
    let id = $("input[name=id]").val();
    if (id) {
      type = "PUT";
      url = backend + "/gs-rest-service/customers/" + id;
    } else {
      type = "POST";
      url = backend + "/gs-rest-service/customers";
    }
    $.ajax({
      type: type,
      url: url,
      data: {
        name: $("input[name=name]").val(),
        age: $("input[name=age]").val(),
        approved: $("input[name=approved][value=Y]").prop("checked")
      },
      dataType: "json",
      success: function(data, textStatus, xhr) {
        describe(this.type, this.url, xhr);
        if (id) {
          $("select[name=customerList] option[value=" + data.id + "]").text(data.id + ". " + data.name);
        } else {
          $("#total").fadeOut(300);
          $("select[name=customerList]").append($("<option/>").attr("value", data.id).text(data.id + ". " + data.name));
          $("select[name=customerList]").val(data.id);
          $("input[name=id]").val(data.id);
          $("#cust-no").text(data.id);
          $("#total").text($("select[name=customerList] option").length);
          $("#total").fadeIn(200);
        }
        $("select[name=customerList] option:selected").css("color", "blue");
      },
      error: function(request, status, error) {
        alert("An error has occurred making the request: " + error);
      }
    });
  }

  function deleteCustomer() {
    let id = $("input[name=id]").val();
    if (!id) {
      alert("Please select a customer to remove.");
      return;
    }
    $.ajax({
      type: "DELETE",
      url: backend + "/gs-rest-service/customers/" + id,
      dataType: "json",
      success: function(data, textStatus, xhr) {
        describe(this.type, this.url, xhr);
        $("select[name=customerList] option[value=" + id + "]").remove();
        clearForm();
      },
      error: function(request, status, error) {
        alert("An error has occurred making the request: " + error);
      }
    });
  }

  function updateApproval(approved) {
    let id = $("input[name=id]").val();
    if (!id) {
      return;
    }
    $.ajax({
      type: "PUT",
      url: backend + "/gs-rest-service/customers/" + id + "/attributes",
      data: {
        approved: approved
      },
      dataType: "json",
      success: function(data, textStatus, xhr) {
        describe(this.type, this.url, xhr);
      },
      error: function(request, status, error) {
        alert("An error has occurred making the request: " + error);
      }
    });
  }

  function clearForm() {
    $("input[name=id]").val("");
    $("input[name=name]").val("");
    $("input[name=age]").val("");
    $("input[name=approved][value=Y]").prop("checked", true);
    $("#total").text($("select[name=customerList] option").length);
    $("#cust-no").text("-");
    $("input.is-invalid-input").removeClass("is-invalid-input");
  }

  function describe(method, url, xhr) {
    $("#describe span").text(method);
    $("#describe input").val(url);
    $("#response-status-code").stop(true).fadeOut(300);
    $("#response-status-code").text(xhr.status);
    $("#response-text").text(xhr.responseText);
    if (xhr.status == "201") {
      $("#response-text").prepend("<strong>Location: " + xhr.getResponseHeader('Location') + "</strong>\n");
    }
    $("#response-status-code").fadeIn(200);
  }
</script>