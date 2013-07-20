//Filename: WorkflowView.js

define([
	'jquery', // lib/jquery/jquery
	'underscore', // lib/underscore/underscore
	'BaseView', // lib/backbone/backbone
	'logging', // logging
	'vm',
	'singletons/UserSession',
	'constants/RDFNS',
	'collections/workflow/WebserviceCollection',
	'models/workflow/WorkflowModel',
	'models/workflow/WebserviceModel',
	'views/workflow/WebserviceListView',
	'views/workflow/ParameterListView',
	'views/workflow/PositionListView',
	'views/workflow/ConnectorListView',
	'text!templates/workflow/workflowEditorTemplate.html'
], function($,
	_,
	BaseView,
	logging,
	Vm,
	session,
	NS,
	WebserviceCollection,
	WorkflowModel,
	WebserviceModel,
	WebserviceListView,
	ParameterListView,
	PositionListView,
	ConnectorListView,
	workflowEditorTemplate
	) {

	var log = logging.getLogger("views.workflow.WorkflowView");

	return BaseView.extend({

        template : workflowEditorTemplate,

		events : {
			"click button#save-workflow" : "saveWorkflow",
            "click button#create-config" : "createConfig",
			"click button#render" : "render"
		},

		initialize : function() {
			
			this.listenTo(this.model, "change", this.render);
            this.on("sync", this.render);

            // NOTE cannot reference $el yet because app.js assigns it to the #page at render time => render
//            this.$el.attr("data-backbone-modelid", this.model.id);

			/*
			 * Load Webservice List
			 */
			this.webserviceCollection = session.get("user").getQN("omnom:webservice");
			this.webserviceListView = Vm.createView(this, 'WebserviceListView', WebserviceListView, {
				collection : this.webserviceCollection
			});
//			console.log(session.get("user").getQN("omnom:webservice").models);

			/*
			 * Parameter Views
			 */
			this.inputParameterListView = Vm.createView(this,
				'InputParameterListView',
				ParameterListView,
				{
					parentType : NS.getQN("omnom:Workflow"),
					parentModel : this.model,
                    inputOrOutput : 'input',
					itemClass : "input-param",
					collection : this.model.getQN("omnom:inputParam")
				});
			this.outputParameterListView = Vm.createView(this,
				'OutputParameterListView',
				ParameterListView,
				{
					parentType : NS.getQN("omnom:Workflow"),
					parentModel : this.model,
                    inputOrOutput : 'output',
					itemClass : "output-param",
					collection : this.model.getQN("omnom:outputParam")
				});

			/*
			 * Positions View
			 */
			this.positionListView = Vm.createView(this, 'PositionListView', PositionListView, { collection : this.model.getQN("omnom:workflowPosition") });

			/*
			 * Connectors View
			 */
			this.connectorListView = Vm.createView(this, 'ConnectorListView', ConnectorListView, { collection : this.model.getQN("omnom:parameterConnector") });

		},
		
		renderInputParameterListView : function() {
			this.assign(this.inputParameterListView, "#workflow-input-parameter-list");
            this.$("#workflow-input-parameter-list").attr("data-backbone-modelid", this.model.id);
		},
		renderOutputParameterListView : function() {
			this.assign(this.outputParameterListView, "#workflow-output-parameter-list");
            this.$("#workflow-output-parameter-list").attr("data-backbone-modelid", this.model.id);
		},

		renderPositionListView : function() {
			this.assign(this.positionListView, "#workflow-position-list");
		},

		renderConnectorListView : function() {
			this.assign(this.connectorListView, "#workflow-connector-list");
		},
		
		renderWebserviceList : function() {
			log.info("renderWebserviceList()");
			this.assign(this.webserviceListView, '#workflow-webservice-list');
		},

		render : function() {
            this.renderModel();

            this.$el.data("model", this.model);

			this.renderWebserviceList();
			this.renderInputParameterListView();
			this.renderOutputParameterListView();
			this.renderPositionListView();
			this.renderConnectorListView();
            return this;
		},
		
		saveWorkflow: function() {
			log.debug(JSON.stringify(this.model.toJSON(), undefined, 2));
			log.debug(this.model.toJSON());
			this.model.save();
		},

        createConfig: function() {
            this.saveWorkflow();
            // Navigate to the config editor
            window.location.hash = "#config-edit-from/" + this.model.id;
        }

	});
});