//Filename: ParameterView.js

define([
	'jquery',
	'underscore',
	'logging',
	'backbone',
	'BaseView',
	'vm',
	'constants/RDFNS',
], function($,
	_,
	logging,
	Backbone,
	BaseView,
	Vm,
	NS
) {

	var log = logging.getLogger("ParamterListItemView.js");

	return BaseView.extend({

		className : "fat-border parameter",

		initialize : function(options) {

	    	var that = this;

	    	this.$el.data("model", this.model);

			this.parentModel = options.parentModel;
            this.model.inputOrOutput = options.inputOrOutput;
	    	this.$el.data("parentModel", this.parentModel);

			this.$el.addClass(options.itemClass);

            this.$el.attr("data-backbone-modelid", this.model.cid);

	    	this.$el.draggable({
				revert: "invalid",
//				helper: "clone",
                helper: function () {
                    return $(this)
                        .clone()
                        .css("list-style", "none")
                        .addClass("highlight");
                },
				delay: 0
	    	});

	    	this.$el.droppable({
	    		accept: function(draggable) {
                    return that.isAcceptable(draggable);
                },
	    		drop : function(event, ui) {
	    			that.addConnectionTo(ui.draggable);
	    		},
	    		activeClass: "drop-active",
	    		hoverClass: "drop-hover",
	    	});
		},

        isAcceptable : function(draggable) {
            if (! draggable.hasClass("parameter"))
                return false;
            var source = draggable.data("model");
            var sourceParent = draggable.data("parentModel");
            var target = this.model;
            var targetParent = this.parentModel;

            // No self-references
            if(targetParent === sourceParent)
                return false;

            // From Workflow Input only to Position Input
            if (source.paramType === 'workflow'
                &&
                target.inputOrOutput === 'output')
                return false;

            // From Position Input nowhere else
            if (source.paramType === 'webservice'
                &&
                source.inputOrOutput === 'input')
                return false;

            // Nothing to workflow input
            if (target.paramType === 'workflow'
                &&
                target.inputOrOutput === 'input')
                return false;

            // From position output never to position output
            if (source.paramType === 'webservice'
                &&
                target.paramType === 'webservice'
                &&
                target.inputOrOutput === 'output')
                return false;

            console.log(target.inputOrOutput);
//            TODO

            return true;
        },

		addConnectionTo : function(draggable) {
			var source = draggable.data("model");
			var sourceParent = draggable.data("parentModel");
			var target = this.model;
			var targetParent = this.parentModel;
			var conn = {};

            // fromParam
			conn[NS.getQN("omnom:fromParam")] = source;

            // toParam
			conn[NS.getQN("omnom:toParam")] = target;
//            conn[NS.getQN("omnom:from")]

            // fromWorkflow/fromPosition
            conn[source.paramType === 'workflow'
                ? NS.getQN("omnom:fromWorkflow")
                : NS.getQN("omnom:fromPosition")] = sourceParent;

            // toWorkflow/toPosition
            conn[target.paramType === 'workflow'
                ? NS.getQN("omnom:toWorkflow")
                : NS.getQN("omnom:toPosition")] = targetParent;

//            console.error(targetParent.cid);

//			console.log("New connection: %o", conn);

            // finding the workflow
            var workflow = $("#page").data("model");
            var x = workflow.getQN("omnom:parameterConnector").add(conn);
            console.log("New connection: %o", x.toJSON());


//			if (source.paramType == 'workflow')
//				console.log("fromWorkflow");
//			else
//				console.log("fromWebservice");
		}

	});
});