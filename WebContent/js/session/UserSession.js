//Filename: UserSession.js

define([ 'jquery', // lib/jquery/jquery
'underscore', // lib/underscore/underscore
'backbone', // lib/backbone/backbone
'logging', // logging
'models/user/UserModel'
], function($, _, Backbone, logging, UserModel) {

    var log = logging.getLogger("UserSession.js");

    var UserSession = Backbone.Model.extend({
    });
    
    var dummySession = new UserSession({
        user : new UserModel({userName : "John Doe"})
    });
    
    return dummySession;
});