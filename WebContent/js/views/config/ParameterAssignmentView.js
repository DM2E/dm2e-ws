//Filename: ParameterAssignmentView.js

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'BaseView',
    'text!templates/config/parameterAssignmentTemplate.html'
], function($,
            _,
            logging,
            Vm,
            BaseView,
            theTemplate
    ) {

    var log = logging.getLogger("ParameterAssignmentView");

    return BaseView.extend({

        template : theTemplate,
        drop : function(event, ui) {
            var droppedModel = ui.draggable.data("model");
            this.$("input#paramValue").val(droppedModel.url());
        },
        accept: function(draggable) {
            return true;
        },
        render: function() {

            this.renderModel();

            var that = this;
            this.$el.droppable({
                drop : function(event, ui) { return that.drop(event, ui) },
                accept : function(draggable) { return that.accept(draggable) },
                activeClass: "drop-active",
                hoverClass: "drop-hover",
            });

            return this;
        },

//        initialize : function() {
//            this.listenTo(this.model, "change", this.render);
//        },

    });
});
