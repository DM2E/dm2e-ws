define([
        'underscore',
        'backbone',
        'logging',
        'models/file/FileModel',
], function(_, Backbone, logging, FileModel) {

    var log = logging.getLogger("FilesCollection");

    var FilesCollection = Backbone.Collection.extend({

        model : FileModel,

        initialize : function(models, options) {
            log.debug("Created FilesCollection of " + models.length + " files.");
        },

//        url : function() {
//            return 'api/file/list';
//        },

        parse : function(data) {
            log.debug("Parsing list of files.");
            console.log(data);
            return data;
        },

    });

    return FilesCollection;

});