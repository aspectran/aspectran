<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://aspectran.com/tags" prefix="aspectran" %>
<style>
    #file-upload-list ul {
        list-style-type: none;
        margin: 0;
        padding: 0;
    }
    #file-upload-list .card img, #file-upload-list .card canvas {
        border-radius: 0;
        width: 100px;
        height: 100px;
    }
    #file-upload-list .card canvas.link:hover, #file-upload-list .card img.link:hover {
        cursor: pointer;
    }
    #file-upload-list .card img.blank {
        background: #ccc url("https://assets.aspectran.com/img/aspectran-site-logo.png") no-repeat;
        background-size: cover;
    }
    #file-upload-list .card-body {
        padding: 0.5rem;
    }
    #file-upload-list .file-info p {
        margin: 0;
        font-size: 0.9em;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    #file-upload-list .delete-btn {
        position: absolute;
        top: 5px;
        right: 5px;
    }
    #file-upload-list .progress {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
    }
</style>
<div class="row gx-3 py-4">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Demo Notes</h3>
            </div>
            <div class="card-body">
                <ul>
                    <li>The maximum file size for uploads in this demo is <strong>500KB</strong>.</li>
                    <li>Only image files (<strong>JPG, GIF, PNG</strong>) are allowed in this demo.</li>
                    <li>Up to 30 files will be stored in the memory, and older files will be deleted.</li>
                    <li>You can <strong>drag &amp; drop</strong> files from your desktop on this webpage (see <a
                            href="https://github.com/blueimp/jQuery-File-Upload/wiki/Browser-support">Browser
                        support</a>).
                    </li>
                    <li>Please refer to the <a href="https://github.com/blueimp/jQuery-File-Upload">project website</a>
                        and <a href="https://github.com/blueimp/jQuery-File-Upload/wiki">documentation</a> for more
                        information about jQuery File Upload Plugin.
                    </li>
                    <li>Built with the <a href="https://getbootstrap.com/">Bootstrap</a> CSS framework.</li>
                </ul>
            </div>
        </div>
    </div>
    <div class="col-12 mt-3 text-end">
        <form id="file-upload-form" action="<aspectran:url value="/examples/file-upload/files"/>" method="POST"
              enctype="multipart/form-data">
            <label for="file-adds-input" class="btn btn-success">Add files...</label>
            <input type="file" name="file" id="file-adds-input" class="visually-hidden" multiple>
            <div class="panel mt-1 text-center"><i class="bi bi-box-arrow-in-down display-1"></i>
                <p>Drop files here to upload</p>
            </div>
        </form>
    </div>
    <div class="col-12 mt-3">
        <h2 >Recently Uploaded Files</h2>
        <div id="file-upload-list">
            <ul class="row g-2">
                <c:forEach items="${files}" var="file">
                    <li class="col-md-4">
                        <div class="card">
                            <div class="row g-0">
                                <div class="col-auto">
                                    <a href="<aspectran:url value="files/${file.key}"/>" target="_blank">
                                        <img src="<aspectran:url value="files/${file.key}"/>" alt="${file.fileName}">
                                    </a>
                                </div>
                                <div class="col">
                                    <div class="card-body file-info">
                                        <p><a href="<aspectran:url value="files/${file.key}"/>"
                                              download="${file.fileName}" target="_blank">${file.fileName}</a></p>
                                        <p class="card-text"><small class="text-muted">${file.humanFileSize}</small></p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>
<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
<script src="<aspectran:url value="/assets/js/vendor/jquery.ui.widget.js"/>"></script>
<!-- The Canvas to Blob plugin is included for image resizing functionality -->
<script src="<aspectran:url value="/assets/js/vendor/canvas-to-blob.min.js"/>"></script>
<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
<script src="<aspectran:url value="/assets/js/vendor/load-image.all.min.js"/>"></script>
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="<aspectran:url value="/assets/js/jquery.iframe-transport.js"/>"></script>
<!-- The basic File Upload plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload.js"/>"></script>
<!-- The File Upload processing plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload-process.js"/>"></script>
<!-- The File Upload image preview & resize plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload-image.js"/>"></script>
<!-- The File Upload audio preview plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload-audio.js"/>"></script>
<!-- The File Upload video preview plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload-video.js"/>"></script>
<!-- The File Upload validation plugin -->
<script src="<aspectran:url value="/assets/js/jquery.fileupload-validate.js"/>"></script>
<script>
    $(function () {
        'use strict';
        // Change this to the location of your server-side upload handler:
        let url = "<aspectran:url value="/examples/file-upload/files"/>";
        let deleteButton = $('<button/>')
            .attr("type", "button")
            .addClass('btn btn-danger btn-sm delete-btn')
            .append($('<i/>').addClass('bi bi-x-lg'));
        let progressBar = $('<div/>')
            .addClass('progress')
            .append($('<div/>')
                .addClass('progress-bar progress-bar-striped progress-bar-animated'));

        $('#file-upload-form').fileupload({
            url: url,
            dataType: 'json',
            autoUpload: true,
            acceptFileTypes: /([.\/])(gif|jpe?g|png)$/i,
            maxFileSize: 1000000,
            // Enable image resizing, except for Android and Opera,
            // which actually support image resizing, but fail to
            // send Blob objects via XHR requests:
            disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
            previewMaxWidth: 100,
            previewMaxHeight: 100,
            previewCrop: true
        }).on('fileuploadadd', function (e, data) {
            let ul = $('#file-upload-list ul');
            if (ul.length === 0) {
                ul = $('<ul/>').addClass('row');
                $('#file-upload-list').html(ul);
            }
            data.context = $('<li/>').addClass('col-md-4');
            let card = $('<div/>').addClass('card');
            let row = $('<div/>').addClass('row g-0');
            let col4 = $('<div/>').addClass('col-auto');
            let col8 = $('<div/>').addClass('col');
            let cardBody = $('<div/>').addClass('card-body file-info');
            row.append(col4).append(col8.append(cardBody));
            card.append(row);
            data.context.append(card);
            if (data.files) {
                $.each(data.files, function (index, file) {
                    let node = cardBody;
                    node.append($('<p/>').append($('<a/>').addClass('filename').text(file.name)));
                    node.append($('<p/>').addClass('card-text').append($('<small/>').addClass('text-muted').text(humanFileSize(file.size, true))));
                    if (!index) {
                        card.append(deleteButton.clone(true).data(data));
                        card.append(progressBar.clone(true));
                    }
                });
            }
            if (ul.find('li').length >= ${page.maxFiles}) {
                ul.find('li:eq(0)').fadeOut(400);
                setTimeout(function () {
                    ul.find('li:eq(0)').remove();
                    data.context.appendTo(ul);
                }, 400);
            } else {
                data.context.appendTo(ul);
            }
        }).on('fileuploadprocessalways', function (e, data) {
            let index = data.index,
                file = data.files[index],
                node = $(data.context);
            if (file.preview) {
                node.find('.col-auto').append(file.preview);
            } else {
                node.find('.col').append($('<img/>').addClass('blank'));
            }
            if (file.error) {
                node.find(".file-info").append($('<div class="alert alert-danger p-1 m-0 mt-1"/>').text(file.error));
                node.find("button.delete-btn").prop('disabled', false).fadeOut(1000);
                node.find('.progress').fadeOut();
            }
        }).on('fileuploadprogress', function (e, data) {
            let node = $(data.context);
            let progress = Math.floor(data.loaded / data.total * 100);
            node.find('.progress-bar').css(
                'width',
                progress + '%'
            ).attr('aria-valuenow', progress).text(progress + '%');
        }).on('fileuploaddone', function (e, data) {
            $.each(data.result.files, function (index, file) {
                let node = $(data.context);
                if (file.fileName) {
                    let link = $('<a>')
                        .attr('href', "files/" + file.key)
                        .attr('target', '_blank');
                    node.find(".col-auto canvas")
                        .addClass("link")
                        .click(function() {
                            window.open(file.url);
                        }).wrap(link);
                    let fileLink = $('<a>')
                        .attr('href', "files/" + file.key)
                        .attr('target', '_blank')
                        .attr('download', file.fileName)
                        .text(file.fileName);
                    node.find("p a.filename").replaceWith(fileLink);
                    node.find("button.delete-btn")
                        .data("file-key", file.key)
                        .prop("disabled", false)
                        .on('click', function () {
                            let that = $(this);
                            let fileKey = that.data("file-key");
                            console.log("fileKey: ", fileKey);
                            if (fileKey) {
                                $.ajax({
                                    url: url + "/" + fileKey,
                                    type: 'delete',
                                    success: function () {
                                        that.closest('li').fadeOut();
                                        setTimeout(function () {
                                            that.closest('li').remove();
                                        }, 500)
                                    }
                                });
                            } else {
                                that.closest('li').remove();
                            }
                        });
                } else if (file.error) {
                    let error = $('<div class="alert alert-danger p-1 m-0 mt-1"/>').text(file.error);
                    node.find('.file-info').append(error);
                }
                setTimeout(function () {
                    node.find(".progress").fadeOut();
                }, 500);
            });
        }).on('fileuploadfail', function (e, data) {
            $.each(data.files, function () {
                let node = $(data.context);
                let error = $('<div class="alert alert-danger p-1 m-0 mt-1"/>').text('File upload failed.');
                node.find('.file-info').append(error);
                node.find('.progress-bar').removeClass('bg-success').addClass('bg-danger');
            });
        }).prop('disabled', !$.support.fileInput)
            .parent().addClass($.support.fileInput ? undefined : 'disabled');
    });

    function humanFileSize(bytes, si) {
        let thresh = si ? 1000 : 1024;
        if (Math.abs(bytes) < thresh) {
            return bytes + ' B';
        }
        let units = si
            ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
            : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
        let u = -1;
        do {
            bytes /= thresh;
            ++u;
        } while (Math.abs(bytes) >= thresh && u < units.length - 1);
        return bytes.toFixed() + ' ' + units[u];
    }
</script>
