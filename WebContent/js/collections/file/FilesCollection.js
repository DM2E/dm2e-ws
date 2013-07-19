define([
	'underscore',
	'backbone',
	'logging',
	'constants/FILESTATUS',
	'models/file/FileModel',
], function(_, Backbone, logging, FILESTATUS, FileModel) {

	var log = logging.getLogger("FilesCollection");

	var FilesCollection = Backbone.Collection.extend({

		model : FileModel,

		initialize : function(models, options) {
			options = options || {};
			// log.debug("Created FilesCollection of " + models.length + "
			// files.");
			if (options.url) {
				this.url = function() {
					return options.url;
				};
			}
		},

		// url : function() {
		// return 'api/file/list';
		// },

		parse : function(data) {
			log.debug("Parsing list of files.");
			// console.log(data);
			return data;
		},

		listAvailable : function() {
//			return this.where({fileStatus : FILESTATUS.AVAILABLE});
			return this.models;
		}

	});

	return FilesCollection;

});