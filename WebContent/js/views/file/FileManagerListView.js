define([ 'jquery',
        'underscore',
        'backbone',
        'logging',
        'collections/file/FilesCollection',
        'text!templates/file/fileManagerListItemTemplate.html'
], function(
        $,
        _,
        Backbone,
        logging,
        FilesCollection,
        fileManagerListItemTemplate) {
    
    var log = logging.getLogger("FileManageListView");
    
    return Backbone.View.extend({
        
//        initialize : function() {
//            this.setElement($("#file-manager .file-list"));
//        },

        render : function() {
            
            var that = this;
            
            that.$el.append($("<h2>List of files on " + that.collection.url() + "</h2>"));
            
			_.each(this.collection.models, function(fileModel) {
			    var compiledTemplate = _.template( fileManagerListItemTemplate, fileModel.attributes );
//			    log.info(compiledTemplate);
			    that.$el.append(compiledTemplate);
			});
			
			log.debug("FileManagerListView rendered.");
        },
        
        clean : function() {
        }

    });

});