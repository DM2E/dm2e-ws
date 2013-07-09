//Filename: FileUploadPage.js

define([
		'jquery', // lib/jquery/jquery
		'underscore', // lib/underscore/underscore
		'backbone', // lib/backbone/backbone
		'logging', // logging
		'jasny_bootstrap',
		'NS',
		'text!templates/file/fileUploadTemplate.html'
], function($, _, Backbone, logging, jBootstrap, NS, fileUploadTemplate) {

	var log = logging.getLogger("FileUploadPage.js");

	return Backbone.View.extend({

		handleUpload : function(e) {
			e.preventDefault();
			log.error("Intercepting upload");
			console.log(e);
		},

		render : function() {

			var self = this;

			this.$el.html(fileUploadTemplate);

			// intercept submit click
			$("#submit-upload", self.$el).click(self.handleUpload);

			this.renderFileTypeSelection();

			log.info("Rendered FileUploadPage");

			return this;
		},

		renderFileTypeSelection : function() {

			var self = this;

			_.each(NS.OMNOM_TYPES, function(prop, url) {
				$("select#inputFiletype", self.$el).append(
						$("<option>").attr("value", prop).append(url));
			});
		}

	});
});