//Filename: UserPage.js

define([ 'jquery', // lib/jquery/jquery
'underscore', // lib/underscore/underscore
'backbone', // lib/backbone/backbone
'logging', // logging
'session/UserSession',
'text!templates/user/UserPageTemplate.html'
], function($, _, Backbone, logging, userSession, userPageTemplate) {

    var log = logging.getLogger("UserPage.js");

    return Backbone.View.extend({
        
        render: function() {
            
            compiledTemplate = _.template(userPageTemplate, { user: userSession.get("user") });
            
            this.$el.html(compiledTemplate);
            
            log.debug('UserPage rendered');
        }

    });
});