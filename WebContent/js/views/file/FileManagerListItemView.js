//Filename: FileManagerListItemView

define([
        'jquery', // lib/jquery/jquery
        'underscore', // lib/underscore/underscore
        'backbone', // lib/backbone/backbone
        'logging', // logging
        'templates/file/fileManagerListItemTemplate'
], function($, _, Backbone, logging, itemTemplate) {

    var log = logging.getLogger('FileManagerListItemView');

    return Backbone.View.extend({

        render : function() {

            var compiledTemplate = _.template(itemTemplate, this.model);
            return this;

        }

    });
});