// vm.js
// View manager
//
define([
    'underscore',
    'logging'
], function(_, logging) {

    var log = logging.getLogger("vm");

    var createView = function(context, name, View, options) {

        this.closeView(context, name);

        log.debug("Creating view " + name);
        var view = new View(options);

        this.views[name] = view;

        if (typeof context.subViews === 'undefined') {
            context.subViews = [];
        }
        context.subViews.push(view);

        return view;
    };

    var cleanupView = function(view) {
        
        var that = this;
        
         _.each(view.subViews || [], function(subview) {
             console.log(subview);
             log.debug("Closing subview " + subview);
             that.cleanupView(subview);
         });
         view.subViews = null;

        // Cleanup view
        // Remove all of the view's delegated events
        view.undelegateEvents();
        
        // Call View's 'clean' function
        if (typeof view.clean === 'function') {
            log.info("This view defines a clean() function.");
            view.clean();
        }
        
        // Remove view from the DOM
        view.$el.removeData().unbind();
        view.remove();
        // Removes all callbacks on view
        view.off();
//        delete view;

    };

    // Close existing view
    var closeView = function(context, name) {
        if (typeof this.views[name] !== 'undefined') {
            log.debug("Closing view " + name);
            this.cleanupView(this.views[name]);
            log.debug("Done Closing view " + name);
        }
    };

    var reuseView = function(context, name, View, options) {
        if (typeof this.views[name] !== 'undefined') {
            log.debug("Re-Using view '" + name + "'.");
            return this.views[name];
        }
        return this.createView(context, name, View, options);
    };

    return {
        views : {},
        createView : createView,
        closeView : closeView,
        reuseView : reuseView,
        cleanupView : cleanupView,
    };

});