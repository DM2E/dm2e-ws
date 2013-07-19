//Filename: UserSession.js

define([
	'jquery',
	'underscore',
	'backbone',
	'logging',
	'constants/RDFNS',
    'util/themeSwitcher',
	'models/user/UserModel'
], function($,
	_,
	Backbone,
	logging,
	NS,
    themeSwitcher,
	UserModel) {

	var log = logging.getLogger("sessions.UserSession");

    var userURI = '/api/user/john-doe';

    var TheUserModel = UserModel.extend({
        url : userURI,
    });
    var userModel = new TheUserModel();
    userModel.fetch({
        async: false,
        success : function() {
            log.debug("Successfully fetched user from server.");
        },
        error : function() {
            log.warn("Creating user");
            userModel.set("id", userURI);
            userModel.setQN("foaf:name", "John Doe");
            userModel.setQN("omnom:preferredTheme", "dark");
            userModel.save();
        }
    });

	var UserSession = Backbone.Model.extend({

    });

	var session = new UserSession({
		user :  userModel,
	});
    themeSwitcher.setTheme(session.get("user").getQN("omnom:preferredTheme"));
	return session;
});