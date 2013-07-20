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
            '*foo': 'notFound',
        }
    });

    var initialize = function (args) {

        var appRouter = new AppRouter();

        var appView = args.appView;

        /**
         * The fallback/default route
         */
        appRouter.on('route:notFound', function() {
            dialogs.errorNotFound();
        });

        /**
         * The home/landing page
         */
        appRouter.route(/(^$|^home$)/, 'showHomePage');
        appRouter.on('route:showHomePage', function () {
            require([ 'views/home/HomePage'
            ], function (HomePage) {
                var page = Vm.reuseView({}, 'HomePage', HomePage);
                appView.showPage(page);
            });

        });

        /**
         * Shows a list of files sorted by file service
         */
        appRouter.route(/file-list\/?(.*)/, 'showFileManager');
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

        /**
         * Shows the user page / profile
         */
        appRouter.route(/^profile$/, 'showUserPage');
        appRouter.on('route:showUserPage', function () {
            require([ 'views/user/UserPage'
            ], function (UserPage) {
                appView.showPage(Vm.createView({}, 'UserPage', UserPage));
            });

        });

        /**
         * Shows the file upload page
         */
        appRouter.route(/^file-upload$/, 'showFileUploadPage');
        appRouter.on('route:showFileUploadPage', function () {
            require([ 'views/file/FileUploadPage'
            ], function (FileUploadPage) {
                var page = Vm.reuseView({}, 'FileUploadPage', FileUploadPage);
                appView.showPage(page);
            });

        });

        /**
         * Show the workflow editor. If no workflow is given as URL argumnt, create a new one
         */
        appRouter.route(/workflow-edit\/?(.*)/, 'showWorkflowEditor');
        appRouter.on('route:showWorkflowEditor', function (workflowURL) {

            require([
                'views/workflow/WorkflowEditorPage',
                'models/workflow/WorkflowModel',
            ], function (WorkflowEditorPage, WorkflowModel) {
                var model;
                if (workflowURL) {
                    console.error(workflowURL);
//                    model = WorkflowModel.findOrCreate({ id: workflowURL});
                    // smelly smelly HACK
                    var TheModel = WorkflowModel.extend({
                        url: workflowURL,
                    });
                    model = new TheModel();
                    console.warn("Retrieving workflow %o", workflowURL);
                    model.url = function () {
                        return workflowURL;
                    };
                    model.fetch({
                        success: function () {
                            appView.showPage(Vm.createView({}, 'WorkflowEditorPage', WorkflowEditorPage, {model: model}));
                        },
                        error: function (model, xhr) {
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
                        error: function (model, xhr) {
                            dialogs.errorXHR(xhr);
                        }
                    });
                }
            });

        });

        /**
         * Create a config from a workflow, persist it and show the config editor for it.
         */
        appRouter.route(/config-edit-from\/(.*)/, 'showConfigEditorFrom');
        appRouter.on('route:showConfigEditorFrom', function (fromWorkflowUri) {
            require([
//                'views/workflow/RunWorkflowPage',
                'models/config/WorkflowConfigModel'
            ], function (WorkflowConfigModel) {
                if (! fromWorkflowUri) {
                    dialogs.errorNotFound();
                    return;
                }

                var onError = function(xhr, status, err) {
                    dialogs.errorXHR(xhr);
                };

                // Persist the workflow config
                var onGenerateSuccess = function(blankConfigData, status, xhr) {
                    $.ajax({
                        async: false,
                        url : '/api/config',
                        type : "POST",
                        processData : false,
                        dataType: "json",
                        contentType : "application/json",
                        data : JSON.stringify(blankConfigData),
                        success : function(namedConfigData, status, xhr) {
                            var respLocation = xhr.getResponseHeader("Location");
                            log.debug("Posted config to " + respLocation);
                            // Load the workflow config editor
                            appRouter.navigate( "config-edit/" + respLocation, true );
                        },
                        error : onError,
                    })
                }

                // Let the server create an empty config for this workflow
                $.ajax({
                    url : fromWorkflowUri + "/blankConfig",
                    type : "GET",
                    dataType: "json",
                    async: false,       // wait for result
                    success: onGenerateSuccess,
                    error : onError,
                })
            });
        });

        /**
         * Show the config editor
         */
        appRouter.route(/config-edit\/(.*)/, 'showConfigEditor');
        appRouter.on('route:showConfigEditor', function (workflowConfigURI) {
            log.debug("Hit route 'config-edit/" + workflowConfigURI);
            require([
                'views/config/ConfigEditorPage',
                'models/config/WorkflowConfigModel'
            ], function (ConfigEditorPage,  WorkflowConfigModel) {
                if (!workflowConfigURI) {
                    dialogs.errorNotFound();
                } else {
                    var TheModel = WorkflowConfigModel.extend({
                        url: workflowConfigURI
                    });
                    var configModel = new TheModel();
                    configModel.fetch({async: false});
//                    console.warn(configModel);
                    var pageView = Vm.createView({}, 'ConfigEditorPage', ConfigEditorPage, { model: configModel });
                    appView.showPage(pageView);
                }
            });

        });

        /**
         *  Show the list of workflows
         */
        appRouter.route(/workflow-list/, 'showWorkflowList');
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

        /**
         * TODO
         */
        appRouter.route(/job-list/, 'showJobList');
        appRouter.on('route:showJobList', function () {
            dialogs.errorNotImplemented();
        });

        /**
         * Shows the help page
         */
        appRouter.route(/help/, 'showHelp');
        appRouter.on('route:showHelp', function () {
            require(['views/help/HelpView'], function(HelpView) {
                var helpView = Vm.reuseView(this, 'HelpView', HelpView, {});
                appView.showPage(helpView);
            });
        });

        Backbone.history.start();

        return appRouter;
    };
    return {
        initialize: initialize
    };
});
