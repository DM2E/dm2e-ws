define([
	'jquery',
	'underscore',
	'backbone',
	'logging',
	'text!templates/home/homeTemplate.html'
], function($,
	_,
	Backbone,
	logging,
	homeTemplate) {

	var log = logging.getLogger("HomeView");

	var HomeView = Backbone.View.extend({

		initialize : function() {
			log.trace("HomeView initialized.");
		},

		render : function() {

			this.$el.html(homeTemplate);

			log.trace("HomeView rendered.");

			return this;
		}

	});

	return HomeView;

});
