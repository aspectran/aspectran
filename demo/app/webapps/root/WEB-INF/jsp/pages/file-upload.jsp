<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<style>
  .files {
    padding: 20px;
    background-color: #f5f5f5;
  }
  .files ul {
    list-style-type: none;
    margin: 0;
    overflow: auto;
  }
  .files li {
    float: left;
    width: 30%;
    min-width: 300px;
    height: 102px;
    margin-right: 10px;
    margin-bottom: 10px;
    border: 1px solid #ccc;
    position: relative;
    background-color: #fff;
  }
  .files li:hover {
    border: 1px solid #aaa;
    box-shadow: 0 3px 4px 0 rgba(0,0,0,0.2),0 3px 3px -2px rgba(0,0,0,0.14),0 1px 8px 0 rgba(0,0,0,0.12);
  }
  .files li canvas, .files li img {
    position: absolute;
    width: 100px;
    height: 100px;
    border-radius: 0;
  }
  .files li canvas.link:hover, .files li img.link:hover {
    cursor: pointer;
  }
  .files li img.blank {
    background: #ccc url("http://www.aspectran.com/assets/img/aspectran-site-logo.png") no-repeat;
    background-size: cover;
  }
  .files li div.info {
    padding-left: 110px;
    height: 100px;
    overflow: auto;
  }
  .files li div.info p {
    margin: 0;
  }
  .files li button.delete {
    border: 0;
    background-color: #ccc;
    color: #fff;
    padding: 6px 8px;
    position: absolute;
    top: 0;
    right: 0;
    border-radius: 0 0 0 4px;
  }
  .files li button.delete:hover {
    background: indianred;
  }
  .files li div.progress {
    position: absolute;
    left: 0;
    bottom: 0;
    height: 8px;
    width: 100px;
    margin: 0;
  }
</style>
<div class="t50 b20">
  <form id="fileupload" action="https://jquery-file-upload.appspot.com/" method="POST" enctype="multipart/form-data">
    <label for="fileAdds" class="button fileinput-button">Add files...</label>
    <input type="file" name="file" id="fileAdds" class="show-for-sr" multiple>
    <div id="files" class="files">Drop files here to upload</div>
  </form>
  <br>
  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">Demo Notes</h3>
    </div>
    <div class="panel-body">
      <ul>
        <li>The maximum file size for uploads in this demo is <strong>3MB</strong>.</li>
        <li>Only image files (<strong>JPG, GIF, PNG</strong>) are allowed in this demo.</li>
        <li>Up to 30 files will be stored in the memory, and older files will be deleted.</li>
        <li>You can <strong>drag &amp; drop</strong> files from your desktop on this webpage (see <a href="https://github.com/blueimp/jQuery-File-Upload/wiki/Browser-support">Browser support</a>).</li>
        <li>Please refer to the <a href="https://github.com/blueimp/jQuery-File-Upload">project website</a> and <a href="https://github.com/blueimp/jQuery-File-Upload/wiki">documentation</a> for more information about jQuery File Upload Plugin.</li>
        <li>Built with the <a href="https://foundation.zurb.com/">Foundation</a> CSS framework.</li>
      </ul>
    </div>
  </div>
</div>
<h2>Recently Uploaded Files</h2>
<div class="files">
  <ul>
  <c:forEach items="${files}" var="file">
    <li>
      <a href="${file.url}" target="_blank"><img src="${file.url}"/></a>
      <div class="info">
        <p><a href="${file.url}" download="${file.fileName}" target="_blank">${file.fileName}</a></p>
        <p>${file.humanFileSize}</p>
      </div>
    </li>
  </c:forEach>
  </ul>
</div>
<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
<script src="/assets/js/vendor/jquery.ui.widget.js"></script>
<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
<script src="https://blueimp.github.io/JavaScript-Load-Image/js/load-image.all.min.js"></script>
<!-- The Canvas to Blob plugin is included for image resizing functionality -->
<script src="https://blueimp.github.io/JavaScript-Canvas-to-Blob/js/canvas-to-blob.min.js"></script>
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="/assets/js/jquery.iframe-transport.js"></script>
<!-- The basic File Upload plugin -->
<script src="/assets/js/jquery.fileupload.js"></script>
<!-- The File Upload processing plugin -->
<script src="/assets/js/jquery.fileupload-process.js"></script>
<!-- The File Upload image preview & resize plugin -->
<script src="/assets/js/jquery.fileupload-image.js"></script>
<!-- The File Upload audio preview plugin -->
<script src="/assets/js/jquery.fileupload-audio.js"></script>
<!-- The File Upload video preview plugin -->
<script src="/assets/js/jquery.fileupload-video.js"></script>
<!-- The File Upload validation plugin -->
<script src="/assets/js/jquery.fileupload-validate.js"></script>
<script>
  $(function () {
    'use strict';
    // Change this to the location of your server-side upload handler:
    var url = '/examples/file-upload/files';
    var uploadButton = $('<button/>')
        .attr("type", "button")
        .addClass('upload button success')
        .prop('disabled', true)
        .text('Upload')
        .on('click', function () {
            var $this = $(this),
                data = $this.data();
            $this.off('click')
                .text('Abort')
                .on('click', function () {
                    $this.remove();
                    data.abort();
                });
            data.submit().always(function () {
                $this.remove();
            });
            $("#progress").removeClass("alert").addClass("success");
        });
    var deleteButton = $('<button/>')
        .attr("type", "button")
        .addClass('button delete')
        .prop('disabled', true)
        .text('X')
        .on('click', function() {
            var $this = $(this);
            var fileKey = $this.data("file-key");
            if(fileKey) {
                $.ajax({
                    url: url + "/" + fileKey,
                    type: 'delete',
                    success: function() {
                        $this.parent().fadeOut();
                        setTimeout(function () {
                            $this.parent().remove();
                        }, 500)
                    }
                });
            } else {
                $this.parent().remove();
            }
        });
    var progressBar = $('<div/>')
            .addClass('success progress')
            .append($('<div/>')
                .addClass('progress-meter'));
    $('#fileupload').fileupload({
      url: url,
      dataType: 'json',
      autoUpload: true,
      acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
      maxFileSize: 3000000,
      // Enable image resizing, except for Android and Opera,
      // which actually support image resizing, but fail to
      // send Blob objects via XHR requests:
      disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
      previewMaxWidth: 100,
      previewMaxHeight: 100,
      previewCrop: true
    }).on('fileuploadadd', function(e, data) {
      var ul = $('#files ul');
      if (ul.size() == 0) {
        ul = $('<ul/>');
        $('#files').html(ul);
      }
      data.context = $('<li/>');
      $.each(data.files, function(index, file) {
          var node = $('<div/>').addClass('info').hide()
              .append($('<p/>').addClass('filename').text(file.name))
              .append($('<p/>').text(humanFileSize(file.size, true)));
          if (!index) {
              node.append(uploadButton.clone(true).data(data));
              data.context.append(deleteButton.clone(true).data(data));
              data.context.append(progressBar.clone(true));
          }
          node.appendTo(data.context).fadeIn();
      });
      if (ul.find('li').size() >= ${page.maxFiles}) {
          ul.find('li:eq(0)').fadeOut(400);
          setTimeout(function() {
              ul.find('li:eq(0)').remove();
              data.context.appendTo(ul);
          }, 400);
      } else {
          data.context.appendTo(ul);
      }
    }).on('fileuploadprocessalways', function (e, data) {
      var index = data.index,
          file = data.files[index],
          node = $(data.context[index]);
      if (file.preview) {
          node.prepend(file.preview);
      } else {
          node.prepend('<img class="blank"/>');
      }
      if (file.error) {
          node.find("div.info").append($('<span class="label alert"/>').text(file.error));
          node.find("button.upload").hide();
          node.find("button.delete").prop('disabled', false);
          node.find('.progress').fadeOut();
          return;
      }
      if (index + 1 === data.files.length) {
          $(data.context[index]).find('button.upload')
              .prop('disabled', !!data.files.error);
      }
    }).on('fileuploadprogress', function (e, data) {
        var node = $(data.context);
        var progress = parseInt(data.loaded / data.total * 100, 10);
        node.find('.progress-meter').css(
            'width',
            progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
      $.each(data.result.files, function (index, file) {
          if (file.url) {
              $(data.context[index]).find("canvas")
                  .addClass("link")
                  .click(function() {
                    window.open(file.url);
                  });
              var link = $('<a>')
                  .attr('href', file.url)
                  .attr('target', '_blank')
                  .attr('download', '');
              $(data.context[index]).find("p.filename").wrap(link);
              $(data.context[index]).find("button.upload").remove();
              $(data.context[index]).find("button.delete")
                  .data("file-key", file.key)
                  .prop("disabled", false);
          } else if (file.error) {
              var error = $('<span class="label alert"/>').text(file.error);
              $(data.context[index]).find('div.info').append(error);
          }
          setTimeout(function () {
              $(data.context[index]).find(".progress").fadeOut();
          }, 500);
      });
    }).on('fileuploadfail', function (e, data) {
      $.each(data.files, function (index) {
          var error = $('<span class="label alert"/>').text('File upload failed.');
          $(data.context[index]).find('div.info')
              .append(error);
      });
        $(data.context[index]).find('.progress').removeClass('success').addClass('alert');
        $(data.context[index]).find('.progress-meter').css("width", "100%");
    }).prop('disabled', !$.support.fileInput)
        .parent().addClass($.support.fileInput ? undefined : 'disabled');
  });

  function humanFileSize(bytes, si) {
      var thresh = si ? 1000 : 1024;
      if(Math.abs(bytes) < thresh) {
          return bytes + ' B';
      }
      var units = si
          ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
          : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
      var u = -1;
      do {
          bytes /= thresh;
          ++u;
      } while(Math.abs(bytes) >= thresh && u < units.length - 1);
      return bytes.toFixed() + ' ' + units[u];
  }
</script>