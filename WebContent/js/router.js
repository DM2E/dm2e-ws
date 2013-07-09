// Filename: router.js
define([
        'jquery',
        'underscore',
        'backbone',
        'vm'
], function($, _, Backbone, Vm) {

    var AppRouter = Backbone.Router.extend({
        routes : {
            'file-upload' : 'showFileUpload',
            'profile' : 'showUserPage',
            // Default route
            '*actions' : 'defaultAction',
        }
    });

    var initialize = function(args) {

        var appRouter = new AppRouter;

        // Route to Workflow Editor
        appRouter.route(/workflow\/(.*)/, 'showWorkflowEditor');
        appRouter.route(/file-manager\/?(.*)/, 'showFileManager');

        var appView = args.appView;

        appRouter.on('route:showFileManager', function(fileService) {

            require([
                'views/file/FileManagerPage'
            ], function(FileManagerPage) {
                var fileManagerPage = Vm.createView({}, 'FileManagerPage', FileManagerPage, {selectedFileService : fileService});
                appView.showPage(fileManagerPage);
            });

        });

        appRouter.on('route:showUserPage', function() {

            require([
                'views/user/UserPage'
            ], function(UserPage) {
                appView.showPage(Vm.createView({}, 'UserPage', UserPage));
            });

        });

        appRouter.on('route:defaultAction', function(actions) {
            require([
                'views/home/HomePage'
            ], function(HomePage) {
                var page = Vm.reuseView({}, 'HomePage', HomePage);
                appView.showPage(page);
            });

        });
        
        appRouter.on('route:showFileUpload', function() {
            require([
                'views/file/FileUploadPage'
            ], function(FileUploadPage) {
                var page = Vm.reuseView({}, 'FileUploadPage', FileUploadPage);
                appView.showPage(page);
            });

        });

        // TODO
        appRouter.on('route:showWorkflowEditor', function(arg1, arg2) {

            console.log(arg1, arg2);

            require([
                'views/home/WorkflowEditorPage'
            ], function(WorkflowEditorPage) {
                appView.showPage(Vm.createView({}, 'WorkflowEditorPage', WorkflowEditorPage, {}));
            });

        });

        Backbone.history.start();
    };
    return {
        initialize : initialize
    };
});
