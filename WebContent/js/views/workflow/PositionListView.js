//Filename: PositionListView.js

define([
	'jquery',
	'underscore',
	'logging',
	'BaseView',
	'vm',
	'constants/RDFNS',
	'views/workflow/PositionView',
	'models/workflow/PositionModel',
	'text!templates/workflow/positionListTemplate.html'
], function($,
	_,
	logging,
	BaseView,
	Vm,
	NS,
	PositionView,
	PositionModel,
	positionListTemplate) {

	var log = logging.getLogger("PositionListView.js");

	return BaseView.extend({

		// events : {
		// "click button.add-item" : addItem,
		// },

		template : positionListTemplate,

		initialize : function() {
			log.debug("Initialized PositionListView.js");
			this.listenTo(this.collection, "add", this.render);
			this.listenTo(this.collection, "remove", this.render);
		},
		
//		addItem : function() {
//			that.collection.add(new PositionModel());
//		},

		render : function() {
			this.$el.html(this.createHTML(positionListTemplate));
			var that = this;

			Vm.cleanupSubViews(this);
			_.each(this.collection.models, function(positionModel) {
				var subview = Vm.createSubView(this, PositionView, {
					model : positionModel
				});
				this.appendHTML(subview, "div.list-container");
			}, this);

			this.$el.droppable({
				accept: ".webservice",
				activeClass: "drop-active",
				hoverClass: "drop-hover",
				drop : function(event, ui) {
					console.log(ui.draggable.data("model").get("id"));
					$(this).append(ui.draggable.data("model"));
//					var newPositionModel = new PositionModel();
//					newPositionModel.setQN("omnom:webservice", ui.draggable.data("model"));
//					that.collection.add(newPositionModel);
					var newPos = {};
					log.debug("foo");
					newPos[NS.getQN("omnom:webservice")] = ui.draggable.data("model");
					that.collection.add(newPos);
					log.debug("bar");
				}
			});
		}

	});
});