//Filename: ConnectorView.js

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'BaseView',
    'text!templates/workflow/connectorTemplate.html'
], function($,
            _,
            logging,
            Vm,
            BaseView,
            theTemplate
    ) {

    var log = logging.getLogger("views.workflow.ConnectorView");

    return BaseView.extend({

        events : {
            "click button.remove-connector" : function() {
                this.model.collection.remove(this.model);
            }
        },

        template : theTemplate,

        className : "boxed-item",

        initialize : function() {
            log.debug("Initializing ConnectorView.");
//            this.listenTo(this.model, "change", this.render);
        },

        render : function() {

            var from_cid, from_id, to_cid, to_id;
            var is_from_workflow = false;
            var is_to_workflow = false;

            if (this.model.getQN("omnom:fromWorkflow")) {
                from_cid = this.model.getQN('omnom:fromWorkflow').id;
                from_id = this.model.getQN('omnom:fromWorkflow').id;
                is_from_workflow = true;
            } else {
                from_cid = this.model.getQN('omnom:fromPosition').id
                    ? this.model.getQN('omnom:fromPosition').id
                    : this.model.getQN('omnom:fromPosition').attributes.cid;
                from_id = this.model.getQN('omnom:fromPosition').id;
            }
            if (this.model.getQN("omnom:toWorkflow")) {
                is_to_workflow = true;
                to_cid = this.model.getQN('omnom:toWorkflow').id;
                to_id = this.model.getQN('omnom:toWorkflow').id;
            } else {
                to_cid = this.model.getQN('omnom:toPosition').id
                    ? this.model.getQN('omnom:toPosition').id
                    : this.model.getQN('omnom:toPosition').attributes.cid;
                to_id = this.model.getQN('omnom:toPosition').id;
            }
            var render_opts = {
                from_cid : from_cid,
                from_id : from_id,
                to_cid : to_cid,
                to_id : to_id,
                is_to_workflow : is_to_workflow,
                is_from_workflow : is_from_workflow,
            };
            console.error(render_opts);
            console.error(this.model);

            this.renderModel(render_opts);

            this.$("*[data-backbone-modelid-ref]").hover(function(ev) {
//                console.error(this);
//                console.error($(this).attr("data-omnom-uuid-ref"));
//                console.error($(this).attr("data-backbone-modelid-ref"));
                $("*[data-backbone-modelid='"
                    + $(this).attr("data-backbone-modelid-ref")
                    + "']").toggleClass("highlight");
                // FIND VIEW
                // highlight it
//                alert("FNOIP");
            });

            return this;
        },

    });
});
