define([
        'jquery',
        'underscore',
        'backbone',
        'text!templates/home/homeTemplate.html',
        'NS'
], function($, _, Backbone, homeTemplate, NS) {

    var HomeView = Backbone.View.extend({

        initialize : function() {
            // this.setElement($("#page"));
            log.trace("HomeView initialized.");
        },

        render : function() {

            this.$el.html(homeTemplate);

            log.trace("HomeView rendered.");
        }

    });

    return HomeView;

});
