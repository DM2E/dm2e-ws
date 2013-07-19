//Filename: UserPage.js

define([
	'jquery',
	'underscore',
	'BaseView',
	'logging',
	'singletons/UserSession',
	'text!templates/user/userPageTemplate.html'
], function($,
	_,
	BaseView,
	logging,
	userSession,
	userPageTemplate) {

	var log = logging.getLogger("UserPage.js");

	return BaseView.extend({

		template : userPageTemplate,

	 initialize : function() {
		 console.log("I LIVE");
		 this.model = userSession.get("user");
		 console.log("I LIVE");
	 }

	// render : function() {
	// this.$el.html(_.template(userPageTemplate, {
	// user : userSession.get("user")
	// }));
	//
	// log.debug('UserPage rendered');
	// }

	});
});