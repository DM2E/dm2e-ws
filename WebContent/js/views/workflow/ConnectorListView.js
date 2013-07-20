//Filename: ConnectorListView.js

define([ 'jquery', 'underscore', 'logging', 'BaseView', 'vm',
    'views/workflow/ConnectorView',
    'text!templates/workflow/connectorListTemplate.html'
], function ($, _, logging, BaseView, Vm, ConnectorView, connectorListTemplate) {

    var log = logging.getLogger("views.workflow.ConnectorListView");

    return BaseView.extend({

        template: connectorListTemplate,

        itemView : ConnectorView,

        initialize: function () {
            log.debug("Initialized ConnectorListView");
            this.listenTo(this.collection, "add", this.render);
            this.listenTo(this.collection, "remove", this.render);
            console.log(this.collection);
        },

        render: function () {
            this.$el.html(this.createHTML(this.template));
            this.renderCollection();
        }

    });
});