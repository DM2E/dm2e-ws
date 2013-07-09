//Filename: app.js

define([
        'jquery', // lib/jquery/jquery
        'underscore', // lib/underscore/underscore
        'backbone', // lib/backbone/backbone
        'logging', // logging
        'bootstrap',
        'vm',
        'text!templates/layout.html',
        'models/user/UserModel'
], function($, _, Backbone, logging, bootstrap, Vm, layoutTemplate, UserModel) {

    var log = logging.getLogger("app.js");

    return Backbone.View.extend({

        el : '.app-container',

        // initialize : function() {
        // },

        render : function() {

            this.$el.html(layoutTemplate);

            require([
                'views/header/MenuView'
            ], function(HeaderMenuView) {
                var headerMenuView = Vm.createView(this, "HeaderMenuView", HeaderMenuView);
                $(".main-menu-container").html(headerMenuView.render().$el);
            });

        },

        showPage : function(view) {
            // console.log(view);
            view.render();
            $("#page").html(view.$el);
        }

    });
});
