//Filename: filename

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'BaseView',
    'singletons/UserSession',
    'views/file/FileListView',
    'views/config/ParameterAssignmentView',
    'collections/file/FilesCollection',
    'text!templates/config/ConfigEditorTemplate.html'
], function ($, _, logging, Vm, BaseView, session, FileListView, ParameterAssignmentView, FilesCollection, theTemplate) {

    var log = logging.getLogger("views.config.ConfigEditorPage");

    return BaseView.extend({

        template: theTemplate,

        fileCollections: {},

        initialize: function () {
//            this.listenTo(this.model, "change", this.render);
            _.each(session.get("user").get("fileServices"), function (fileColl) {
                // FIXME this might not be very efficient in the long run...
                this.fileCollections[fileColl] = new FilesCollection([], {
                    url: fileColl,
                });
                this.fileCollections[fileColl].fetch();
            }, this);
        },

        render: function () {
            this.renderModel();
            Vm.cleanupSubViews(this);
            this.renderResourceLists();
            this.renderParameterList();
            return this;
        },

        renderParameterList: function () {
            _.each(this.model.getQN("omnom:assignment").models, function (ass) {
                console.error(ass);
                var assView = Vm.createSubView(this, ParameterAssignmentView, { model: ass });
                this.appendHTML(assView, '.parameter-assignment-list');
            }, this);
        },

        renderResourceLists: function () {
            _.each(this.fileCollections, function (coll, collName) {
                this.$(".resource-list").append("<div data-omnom-fileservice='" + collName + "']> </div>");
                console.warn(coll);
                var subview = Vm.createSubView(this, FileListView, {collection: this.fileCollections[collName] });
                console.warn(subview);
                console.warn(this.$("div[data-omnom-fileservice='" + collName + "']"));
                this.assign(subview, "div[data-omnom-fileservice='" + collName + "']");
            }, this)
        },

    });
});
