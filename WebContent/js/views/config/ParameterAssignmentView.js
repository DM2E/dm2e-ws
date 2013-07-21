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
        populateModelFromForm : function() {
            this.model.setQN("omnom:parameterValue", this.$("input#paramValue").val());
        },
        drop : function(event, ui) {
            var droppedModel = ui.draggable.data("model");
            console.log(droppedModel.absoluteURL());
            this.$("input#paramValue").val(droppedModel.absoluteURL());
            this.populateModelFromForm();
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
