define([
	'underscore',
	'backbone',
], function(_, Backbone) {

	var FileModel = Backbone.Model.extend({

		defaults : {
			originalName : "Untitled Document :)",
			fileType : "http://onto.dm2e.eu/omnom-types/Unknown",
		},

		parse : function(response, options) {
			// Reduce ID to the last path segment, so DELETE, PUT etc. in
			// collections work
			response.id = response.id.replace(/http.*\//, "");
			return response;
		},

		// All our IDs are dereferenceable URLs
		urlRoot : null,

	});

	return FileModel;

});
