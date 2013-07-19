define([
	'jquery',
	'underscore',
	'backbone',
	'logging',
	'vm',
	'collections/file/FilesCollection',
	'views/file/FileManagerListItemView',
	'text!templates/file/fileManagerListTemplate.html'
], function($,
	_,
	Backbone,
	logging,
	Vm,
	FilesCollection,
	FileManagerListItemView,
	fileManagerListTemplate) {

	var log = logging.getLogger("FileManageListView");

	return Backbone.View.extend({
		
		className: "list-container",

		hideLoadingIndicator : function() {
			if (this.collection.listAvailable().length === 0) {
				this.$el.append("No Files found.");
			}
			this.$(".loading-indicator").hide();
		},

		initialize : function() {
			this.collection.on('remove', this.render, this);
			this.collection.on('remove', this.hideLoadingIndicator, this);
			this.collection.on('sync', this.hideLoadingIndicator, this);
		},

		render : function() {
			
			var that = this;

			this.$el.html(_.template(fileManagerListTemplate, {
				fileService : this.collection.url()
			}));

			_.each(this.collection.listAvailable(), function(fileModel) {
				that.renderFileModel(fileModel);
			});
//			
			return this;
		},

		renderFileModel : function(fileModel) {

			// var self = this;

			var viewName = 'view-' + fileModel.get("id");
			var itemView = Vm.createView(this, viewName, FileManagerListItemView, {
				model : fileModel
			});
			this.$el.append(itemView.render().$el);
		},

		clean : function() {
		}

	});

});