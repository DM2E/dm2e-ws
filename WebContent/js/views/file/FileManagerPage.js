define([
	'jquery',
	'underscore',
	'backbone',
	'logging',
	'vm',
	'session/UserSession',
	'collections/file/FilesCollection',
	'text!templates/file/fileManagerTemplate.html'
], function(
	$,
	logging,
	Backbone,
	logging,
	Vm,
	userSession,
	FilesCollection,
	fileManagerTemplate) {

	var log = logging.getLogger("FileManagerView");

	return Backbone.View.extend({

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

				that.collection = new FilesCollection([]);
				console.log(this.selectedFileService);
				this.selectedFileService = options.selectedFileService;
				that.collection.url = function() {
					return options.selectedFileService;
				};
				that.collection.fetch({
					success : onDataHandler,
					error : onErrorHandler,
					dataType : "json",
				});
			}

		},

		render : function() {

			var compiledTemplate = _.template(fileManagerTemplate, {
				user : userSession.get("user"),
			});
			this.$el.html(compiledTemplate);

			if (typeof this.collection !== 'undefined')
				this.renderFileList();

			return this;
		},

		renderFileList : function() {

			that = this;

			require([
				'views/file/FileManagerListView',
			], function(FileManagerListView) {
				var fileListView = Vm.createView(
					that,
					'FileManagerListView',
					FileManagerListView,
					{
						collection : that.collection,
						selectedFileServie : this.selectedFileService
					});
				fileListView.render();
				$(".file-list", that.$el).append(fileListView.$el);
			});

			log.debug("FileList rendered.");
		},

		clean : function() {
			log.debug("Removing collection object.");
			if (typeof this.collection !== 'undefined')
				this.collection.reset();
		}

	});
});