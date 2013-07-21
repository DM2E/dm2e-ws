//Filename: filename

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'BaseView',
    'util/dialogs',
    'singletons/UserSession',
    'views/file/FileListView',
    'views/config/ParameterAssignmentView',
    'collections/file/FilesCollection',
    'text!templates/config/ConfigEditorTemplate.html'
], function ($, _, logging, Vm, BaseView, dialogs, session, FileListView, ParameterAssignmentView,
    FilesCollection, theTemplate) {

    var log = logging.getLogger("views.config.ConfigEditorPage");

    return BaseView.extend({

        template: theTemplate,

        fileCollections: {},

        events: {
            "click button#edit-workflow": function () {
                var that = this;
                window.location.hash = "workflow-edit/" + this.model.getQN("omnom:workflow").id;
            },
            "click button#save-config": function() { this.saveConfig(); },
            "click button#run-config": function () {
//                var button = this.$("button#run-config");
//                $("span", button).addClass("loading-indicator");
                this.setButtonLoading("button#run-config");
                var that = this;
                this.saveConfig();
                var config = this.model;
                $.ajax( {
                    url: config.getQN("omnom:workflow").id,
                    type: "PUT",
                    processData: false,
                    dataType: "json",
                    contentType: "text/plain",
                    data: config.id,
                    complete : function (jqXHR, textStatus, err) {
                        var jobLoc = jqXHR.getResponseHeader("Location");
                        if (jqXHR.status === 202) {
//                            dialogs.errorXHR(jqXHR);
                            dialogs.notify("Job started " + jobLoc);
                        } else {
                            dialogs.notify(jqXHR.status, 'error');
                        }
                        that.unsetButtonLoading("button#run-config");
                    },
                });
                    // FIXME TODO
//                ).complete(function (data, textStatus, jqXHR) {
//                    var jobLoc = jqXHR.getResponseHeader("Location");
//                    if (jqXHR.status === 202) {
//                        dialogs.errorXHR(jqXHR);
////                            dialogs.notify("Job started " + jobLoc);
//                    } else {
//                        dialogs.notify(textStatus, 'error');
//                    }
//                    dialogs.notify("Run executed.");
//                });
            }
        },

        saveConfig: function () {
            var that = this;
            this.model.save().then(function (data, textStatus, xhr) {
                if (xhr.status === 201)
                    dialogs.notify("Saved WorkflowConfig " + that.model.id);
                else
                    dialogs.notify(xhr.statusText, 'error');
            });

        },

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
