define([
        'jquery',
        'underscore',
        'backbone',
        'bootstrap',
        'logging',
        'session/UserSession',
        'text!templates/header/menuTemplate.html'
], function($, _, Backbone, bootstrap, logging, userSession, menuTemplate) {

    var log = logging.getLogger("MenuView");

    var HeaderMenuView = Backbone.View.extend({
        initialize : function() {
            log.trace("MenuView initialized.");
        },
        render : function() {
            var compiledTemplate = _.template(menuTemplate, {
                user : userSession.get("user")
            });
            $(this.el).html(compiledTemplate);
            $('a[href="' + window.location.hash + '"]').parent().addClass('active');
            return this;
        },
        events : {
            'click a' : 'highlightMenuItem'
        },
        highlightMenuItem : function(ev) {
            $('.active').removeClass('active');
            $(ev.currentTarget).parent().addClass('active');
        }
    });

    return HeaderMenuView;
});
