//Filename: FileListItemView

define([
	'jquery', 
	'underscore', 
	'BaseView', 
	'logging', 
	'util/dialogs',
	'text!templates/file/fileListItemTemplate.html',
], function($, _, BaseView, logging, dialogs, itemTemplate) {

	var log = logging.getLogger('FileListItemView.js');

	return BaseView.extend({

        template : itemTemplate,

        initialize : function() {
            this.$el.data("model", this.model);
        },
		
		render : function() {
            this.renderModel();

            // TODO draggable
            this.$el.draggable({
                helper: 'clone',

            });

			return this;
		}

	});
});