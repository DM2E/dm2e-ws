define([ 'jquery', 'underscore', 'backbone',

//'backboneBootstrapModal', // exports to Backbone.BootstrapModal!
], function($,
	_,
	Backbone) {

	return {

        errorNotFound : function() {
            new Backbone.BootstrapModal({
                content : "404 : Not found.",
            }).open();
            window.location.hash = "home";
        },

		errorNotImplemented : function() {
			new Backbone.BootstrapModal({
				content : "This view is not yet implemented.",
			}).open();
			window.location.hash = "home";
		},

		errorXHR : function(xhr) {
			new Backbone.BootstrapModal({
				title : "Error communicating with server.",
				content : "Response was " + xhr.status + " " + xhr.statusText,
			}).open();
		}

	};

});