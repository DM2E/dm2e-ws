//Filename: FileUploadPage.js

define([
	'jquery', // lib/jquery/jquery
	'underscore', // lib/underscore/underscore
	'backbone', // lib/backbone/backbone
	'logging', // logging
	'vm',
	'constants/RDFNS',
	'text!templates/file/fileUploadTemplate.html',
], function($,
	_,
	Backbone,
	logging,
	Vm,
	RDFNS,
	fileUploadTemplate) {

	var log = logging.getLogger("FileUploadPage.js");

	return Backbone.View.extend({

		events : {

			"click #submit-upload" : function(e) {
				e.preventDefault();

				var fileInput = this.$("input:file");
				var fileInputFile = fileInput.get(0).files[0];
				var fd = new FormData();
				fd.append("file", fileInputFile);
				console.log(fd);
				$.ajax({
					url : 'api/file',
					data : fd,

					cache : false,
					processData : false,

					type : 'POST',

					// This will override the content type header,
					// regardless of whether content is actually sent.
					// Defaults to 'application/x-www-form-urlencoded'
					contentType : false,

					// Before 1.5.1 you had to do this:
					beforeSend : function(x) {
						if (x && x.overrideMimeType) {
							x.overrideMimeType("multipart/form-data");
						}
					},

					// Now you should be able to do this:
					mimeType : 'multipart/form-data', // Property added in
														// 1.5.1

					success : function(data) {
						log.info("success");
						console.log(data);
						Vm.navigateTo("file-manager/api/file");
					},
					error : function(xhr) {
						log.info("errror :(");
						console.log(xhr);
						// console.log($.httpData(xhr));
						console.log(xhr.status);
						console.log(xhr.responseText);
					},

				});
				// var filename=$("input:file").val().replace("C:\\fakepath\\",
				// "");
				// var jqXHR = $("#upload-control").fileupload('send', {
				// formData: function (form) {
				// var arr = form.serializeArray();
				// arr.push({
				// name: "filename",
				// value: filename
				// });
				// return arr;
				// },
				// fileInput: $("input:file")
				// });
			}
		},

		render : function() {

			var self = this;

			this.$el.html(fileUploadTemplate);

			// intercept submit click
			// $("#submit-upload").click(handleUpload);

			this.renderFileTypeSelection();

			log.info("Rendered FileUploadPage");

			return this;
		},

		renderFileTypeSelection : function() {

			var self = this;
			

			_.each(RDFNS.OMNOM_TYPES(), function(prop, url) {
				this.$("select#inputFiletype").append($("<option>")
					.attr("value", prop).append(url));
			}, this);
		}

	});
});
