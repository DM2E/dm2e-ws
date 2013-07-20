define([
    'jquery',
    'underscore',
    'BaseView',
    'logging',
    'vm',
    'collections/file/FilesCollection',
    'views/file/FileListItemView',
    'text!templates/file/fileListTemplate.html'
], function ($, _, BaseView, logging, Vm, FilesCollection, FileListItemView, fileListTemplate) {

    var log = logging.getLogger("FileListView");

    return BaseView.extend({

//        className: "list-container",

        template: fileListTemplate,

        itemView: FileListItemView,

        hideLoadingIndicator: function () {
            if (this.collection.listAvailable().length === 0) {
                this.$el.append("No Files found.");
            }
            this.$(".loading-indicator").hide();
        },

        initialize: function () {
            this.collection.on('sync', this.render, this);
            this.collection.on('sync', this.hideLoadingIndicator, this);
        },

        render: function () {
            this.renderModel({ fileService: this.collection.url() });
            console.warn(this.el);
            this.renderCollection({}, '.file-list');
            return this;
        },

    });

});