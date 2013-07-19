//Filename: WorkflowView.js

define([
	'jquery', // lib/jquery/jquery
	'underscore', // lib/underscore/underscore
	'BaseView', // lib/backbone/backbone
	'logging', // logging
	'vm',
	'singletons/UserSession',
	'constants/RDFNS',
//	'collections/workflow/WorkflowCollection',
//	'models/workflow/WorkflowModel',
	'text!templates/workflow/workflowListPage.html',
    'views/workflow/WorkflowTableRow'
], function($,
	_,
	BaseView,
	logging,
	Vm,
	session,
	NS,
//	WorkflowCollection,
//	WorkflowModel,
	workflowListTemplate,
    WorkflowTableRow
	) {

	var log = logging.getLogger("views.workflow.WorkflowListPage");

	return BaseView.extend({

        template : workflowListTemplate,

		events : {
		},

		initialize : function() {
			
//			this.listenTo(this.collection, "add", this.render);
            this.listenTo(this.collection, "sync", this.render);
            this.collection.fetch();

//			/*
//			 * Load Webservice List
//			 */
//			this.webserviceCollection = session.get("user").getQN("omnom:webservice");
//			this.webserviceListView = Vm.createView(this, 'WebserviceListView', WebserviceListView, {
//				collection : this.webserviceCollection
//			});

		},

		render : function () {
            this.doRender();
            console.log(this.collection);
            this.renderCollection(WorkflowTableRow, {}, 'tbody');
            return this;
		},
		
//		saveWorkflow: function() {
//			log.debug(JSON.stringify(this.model.toJSON(), undefined, 2));
//			log.debug(this.model.toJSON());
//			this.model.save();
//		},

	});
});