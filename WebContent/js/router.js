// Filename: router.js
define([
    'jquery',
    'underscore',
    'backbone',
    'vm',
    'logging',
    'util/dialogs'
], function ($, _, Backbone, Vm, logging, dialogs) {

    var log = logging.getLogger("router");

    var AppRouter = Backbone.Router.extend({
        routes: {
            'file-upload': 'showFileUpload',
            'profile': 'showUserPage',
            'workflow-list': 'showWorkflowList',
            'job-list': 'showJobList',
            'workflow-run': 'showRunWorkflow',
            'help': 'showHelp',
            // Default route
            '*actions': 'defaultAction',
        }
    });

    var initialize = function (args) {

        var appRouter = new AppRouter();

        // Route to Workflow Editor
        appRouter.route(/workflow-edit\/?(.*)/, 'showWorkflowEditor');
        appRouter.route(/file-list\/?(.*)/, 'showFileManager');

        var appView = args.appView;

        appRouter.on('route:showFileManager', function (fileService) {

            require([ 'views/file/FileManagerPage'
            ], function (FileManagerPage) {
                var fileManagerPage = Vm.createView({},
                    'FileManagerPage',
                    FileManagerPage,
                    {
                        selectedFileService: fileService
                    });
                appView.showPage(fileManagerPage);
            });

        });

        appRouter.on('route:showUserPage', function () {

            require([ 'views/user/UserPage'
            ], function (UserPage) {
                appView.showPage(Vm.createView({}, 'UserPage', UserPage));
            });

        });

        appRouter.on('route:defaultAction', function (actions) {
            require([ 'views/home/HomePage'
            ], function (HomePage) {
                var page = Vm.reuseView({}, 'HomePage', HomePage);
                appView.showPage(page);
            });

        });

        appRouter.on('route:showFileUpload', function () {
            require([ 'views/file/FileUploadPage'
            ], function (FileUploadPage) {
                var page = Vm.reuseView({}, 'FileUploadPage', FileUploadPage);
                appView.showPage(page);
            });

        });

        appRouter.on('route:showWorkflowEditor', function (workflowURL) {

            require([
                'views/workflow/WorkflowEditorPage',
                'models/workflow/WorkflowModel',
                'collections/workflow/WorkflowCollection'
            ], function (WorkflowEditorPage, WorkflowModel, WorkflowCollection) {
                var model;
                if (workflowURL) {
                    console.error(workflowURL);
//                    model = WorkflowModel.findOrCreate({ id: workflowURL});
                    // smelly smelly
                    var TheModel = WorkflowModel.extend({
                        url : workflowURL,
                    });
                    model = new TheModel();
                    console.warn("Retrieving workflow %o", workflowURL);
                    console.log("I LIVE");
                    model.url = function () {
                        return workflowURL;
                    };
                    console.log("I LIVE");
                    model.fetch({
                        success: function () {
                            log.debug("I LIVE");
                            appView.showPage(Vm.createView({}, 'WorkflowEditorPage', WorkflowEditorPage, {model: model}));
                        },
                        error: function (model, xhr, options) {
                            dialogs.errorXHR(xhr);
                        }
                    });
                } else {
                    model = new WorkflowModel();
                    var urlRoot = 'api/workflow';
                    model.urlRoot = urlRoot;
                    model.save(null, {
                        success: function () {
                            console.warn("Resetting URL");
                            model.url = function () {
                                return model.get("id");
                            };
                            model.trigger("change");
                            var redirectRoute = "workflow-edit/" + urlRoot + model.url().split(urlRoot, 2)[1];
                            appRouter.navigate(redirectRoute);
                            appView.showPage(Vm.createView({}, 'WorkflowEditorPage', WorkflowEditorPage, {model: model}));
                        },
                        error: function (model, xhr, options) {
                            dialogs.errorXHR(xhr);
                        }
                    });
                }
            });

        });
        // TODO
        appRouter.on('route:showRunWorkflow', function () {
            dialogs.errorNotImplemented();
            //
            // require([
            // 'views/workflow/RunWorkflowPage'
            // ], function(RunWorkflowPage) {
            // appView.showPage(Vm.createView({}, 'RunWorkflowPage',
            // RunWorkflowPage, {}));
            // });

        });
        appRouter.on('route:showRunWorkflow', function () {
            dialogs.errorNotImplemented();
        });
        appRouter.on('route:showWorkflowList', function () {
            require([
                'views/workflow/WorkflowListPage',
                'collections/workflow/WorkflowCollection'
            ], function (WorkflowListPage, WorkflowCollection) {
                var collection = new WorkflowCollection();
//                collection.fetch();
                appView.showPage(Vm.createView({}, 'WorkflowListPage', WorkflowListPage, {collection: collection}));
            });
        });
        appRouter.on('route:showJobList', function () {
            dialogs.errorNotImplemented();
        });
        appRouter.on('route:showHelp', function () {
            dialogs.errorNotImplemented();
        });

        Backbone.history.start();

        return appRouter;
    };
    return {
        initialize: initialize
    };
});
