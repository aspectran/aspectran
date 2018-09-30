<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="row">
  <div class="t10 large-4 columns" style="position:relative;">
    <h2 style="margin:0;">Customer List</h2>
    <span id="total" class="float-right warning badge" style="font-size:2em;position:absolute;top:0;right:0;">0</span>
    <div id="customer-list-board" style="clear:both;border-radius:3px;margin-bottom:.5em;">
      <select name="customerList" size="12" style="height:auto;background-image:none;margin:0;">
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
  <div class="t10 large-8 columns" style="position:relative;">
    <h2 style="margin:0;">Customer Details</h2>
    <span id="cust-no" class="float-right success badge" style="font-size:2em;position:absolute;top:0;right:0;">0</span>
    <div id="details" class="panel radius">
      <form>
        <label>
          No.
          <input type="text" name="id" disabled="disabled"/>
        </label>
        <label>
          Name
          <input type="text" name="name" maxlength="30"/>
        </label>
        <label>
          Age
          <input type="number" name="age" min="1" max="199" maxlength="3" oninput="if(this.value.length >= this.maxLength) this.value = this.value.slice(0, this.maxLength);"/>
        </label>
        <legend>Approval Status:
          <input type="radio" name="approvalStatus" id="approved" value="Y" required><label for="approved">Approved</label>
          <input type="radio" name="approvalStatus" id="denied" value="N" required><label for="denied">Denied</label>
        </legend>
      </form>
    </div>
    <div class="float-right t10">
      <button id="save" type="button" class="success button">Save</button>
    </div>
  </div>
</div>
<div class="row">
  <div class="large-12 columns">
    <div id="describe" class="input-group">
      <span class="input-group-label" style="white-space:nowrap;"></span>
      <input class="input-group-field" type="text" value="" readonly>
    </div>
  </div>
</div>
<div class="row" style="margin-top:10px;">
  <div class="large-12 columns" style="position:relative;">
    <span id="response-status-code" class="float-left badge" style="font-size:1.7em;position:absolute;top:0;left:0;margin-left:.32em;margin-top:-.36em;">200</span>
    <pre id="response-text" style="padding-left:4em;min-height:100px;max-height:300px;"></pre>
  </div>
</div>

<script>
var backend = "/examples";
$(function() {
  $("select[name=customerList]").change(function() {
    $("#details form, #cust-no").fadeOut(300);
    getCustomer($(this).val());
    $("#details form, #cust-no").fadeIn(200);
  });
  $("button#refresh").click(function() {
    $("#customer-list-board, #total").fadeOut(300);
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
  $("input[name=approvalStatus]").click(function() {
    updateApproval($(this).val() == "Y");
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
      describe("GET", this.url, xhr);
      var list = $("select[name=customerList]");
      list.empty();
      for(var i = 0; i < data.customers.length; i++) {
        var id = data.customers[i].id;
        var name = data.customers[i].name;
        list.append($("<option/>").attr("value", id).text("#" + id + ". " + name));
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
      $("input[name=approvalStatus][value=" + (data.customer.approved ? "Y" : "N") + "]").prop("checked", true);
      $("#cust-no").text(data.customer.id);
    },
    error: function(xhr, status, error) {
      alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
    }
  });
}

function saveCustomer() {
  if($("input[name=name]").val() == "") {
    $("input[name=name]").addClass("is-invalid-input");
  }
  if($("input[name=age]").val() == "") {
    $("input[name=age]").addClass("is-invalid-input");
  }
  if($("input.is-invalid-input").size() > 0) {
    return;
  }
  var type, url;
  var id = $("input[name=id]").val();
  if(id == "") {
    type = "POST";
    url = backend + "/gs-rest-service/customers";
  } else {
    type = "PUT";
    url = backend + "/gs-rest-service/customers/" + id;
  }
  $.ajax({
    type: type,
    url: url,
    data: {
      name: $("input[name=name]").val(),
      age: $("input[name=age]").val(),
      approved: $("input[name=approvalStatus]:checked").val()
    },
    dataType: "json",
    success: function(data, textStatus, xhr) {
      describe(this.type, this.url, xhr);
      if(id == "") {
        $("#total").fadeOut(300);
        $("select[name=customerList]").append($("<option/>").attr("value", data.id).text("#" + data.id + ". " + data.name));
        $("select[name=customerList]").val(data.id);
        $("input[name=id]").val(data.id);
        $("#cust-no").text(data.id);
        $("#total").text($("select[name=customerList] option").size());
        $("#total").fadeIn(200);
      } else {
        $("select[name=customerList] option[value=" + data.id + "]").text("#" + data.id + ". " + data.name);
      }
      $("select[name=customerList] option:selected").css("color", "blue");
    },
    error: function(xhr, status, error) {
      alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
    }
  });
}

function deleteCustomer() {
  var id = $("input[name=id]").val();
  if(!id) {
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
    error: function(xhr, status, error) {
      alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
    }
  });
}

function updateApproval(approved) {
  var id = $("input[name=id]").val();
  if(!id) {
    return;
  }
  $.ajax({
    type: "PUT",
    url: backend + "/gs-rest-service/customers/" + id + "/approve/" + (approved ? "true" : "false"),
    dataType: "json",
    success: function(data, textStatus, xhr) {
      describe(this.type, this.url, xhr);
    },
    error: function(xhr, status, error) {
      alert("code: " + xhr.status + "\nmessage: " + xhr.responseText + "\nerror: " + error);
    }
  });
}

function clearForm() {
  $("input[name=id]").val("");
  $("input[name=name]").val("");
  $("input[name=age]").val("");
  $("input[name=approvalStatus]").attr("checked", false);
  $("#total").text($("select[name=customerList] option").size());
  $("#cust-no").text("-");
  $("input.is-invalid-input").removeClass("is-invalid-input");
}

function describe(method, url, xhr) {
  $("#describe span").text(method);
  $("#describe input").val(url);
  $("#response-status-code").fadeOut(300);
  $("#response-status-code").text(xhr.status);
  $("#response-text").text(xhr.responseText);
  if(xhr.status == "201") {
    $("#response-text").prepend("<strong>Location: " + xhr.getResponseHeader('Location') + "</strong>\n");
  }
  $("#response-status-code").fadeIn(200);
}
</script>