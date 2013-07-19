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
    var dummyUserModel = new UserModel();
    dummyUserModel.set("id", userURI);
	dummyUserModel.setQN("foaf:name", "John Doe");
    dummyUserModel.setQN("omnom:preferredTheme", "dark");
    dummyUserModel.url = userURI;
    dummyUserModel.save();

	var UserSession = Backbone.Model.extend({});

	var session = new UserSession({
		user :  dummyUserModel,
	});
    themeSwitcher.setTheme(session.get("user").getQN("omnom:preferredTheme"));
	return session;
});