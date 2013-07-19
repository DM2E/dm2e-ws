//Filename: FileManagerListItemView

define([
	'jquery', 
	'underscore', 
	'BaseView', 
	'logging', 
	'util/dialogs',
	'text!templates/file/fileManagerListItemTemplate.html',
], function($, _, BaseView, logging, dialogs, itemTemplate) {

	var log = logging.getLogger('FileManagerListItemView.js');

	return BaseView.extend({
		
		events : {
			"click .btn-file-delete" : function(e) {
				console.log("Destroying model " + this.model.url());
				this.model.destroy({
					wait : true,
					error : function(model, xhr) {
						log.error("Could not destroy this file model.");
						dialogs.errorXHR(xhr);
					},
					success : function(model, xhr) {
						log.info("Successfully destroyed this file model.");
					}
				});
			},
		},

		render : function() {

			this.$el.html(this.createHTML(itemTemplate, {model: this.model.toJSON()}));
			if (this.model.get("fileEditURI")) {
				this.$(".btn-file-edit").removeClass("disabled");
			}
//			if (this.model.get("fileRetrievalURI")) {
//				this.$(".btn-file-delete").removeClass("disabled");
//			}
			
			return this;
		}

	});
});