define([
	'jquery',
	'underscore',
	'BaseView',
	'logging',
	'vm',
	'singletons/UserSession',
	'collections/file/FilesCollection',
	'views/file/FileManagerListView',
	'text!templates/file/fileManagerTemplate.html'
], function(
	$,
	_,
	BaseView,
	logging,
	Vm,
	userSession,
	FilesCollection,
	FileManagerListView,
	fileManagerTemplate
) {

	var log = logging.getLogger("FileManagerView");

	return BaseView.extend({

		className : "file-manager-page",

		initialize : function(options_arg) {

			var options = options_arg || {};
			var that = this;

			if (options.selectedFileService) {
				var onDataHandler = function(collection) {
					log.debug("FilesCollection retrieved, size: " + collection.models.length);
					that.render();
				};
				var onErrorHandler = function(collection, resp) {
					log.warn("Error retrieving collection");
					console.log(resp);
				};
				this.collection = new FilesCollection([], {
					url: options.selectedFileService
				});
				that.collection.fetch({
					success : onDataHandler,
					error : onErrorHandler,
					dataType : "json",
				});
			}
		},

		render : function() {

			this.$el.html(_.template(fileManagerTemplate, {
				user : userSession.get("user"),
			}));

			if (this.collection) {
				this.renderFileList();
			}

			return this;
		},

		renderFileList : function() {
			this.fileListView = Vm.createView(this, 'FileManagerListView', FileManagerListView, {
				collection : this.collection,
			});
			this.assign(this.fileListView, 'div.file-list');
//			Vm.cleanupSubViews(this);
//			_.each(this.collection.models, function(fileModel) {
//				var subview = Vm.createSubView(this, FileManagerListView, {
//					model : fileModel
//				});
//				this.appendHTML(subview, "div.list-container");
//			}, this);

		},

		clean : function() {
			log.debug("Removing collection object.");
			if (typeof this.collection !== 'undefined') {
				this.collection.reset();
			}
		}

	});
});