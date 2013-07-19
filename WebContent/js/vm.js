// vm.js
// View manager
//
define([
    'underscore',
    'logging'
], function(_, logging) {

    var log = logging.getLogger("vm");
    
    var GLOBAL_MODAL_CONTEXT = {};
    var GLOBAL_MODAL_VIEW;
    
    var cleanupSubViews = function(context) {
    	log.debug("Cleaning up subviews.");
    	var i = 0;
        _.each(context.subViews || [], function(subview) {
        	this.cleanupView(subview);
        	i++;
        }, this);
        context.subViews = [];
    	log.debug("DONE Cleaning up " + i + " subviews.");
    }

    var createSubView = function(context, View, options) {
    	log.debug("Creating item view.")
        var view = new View(options);
        if (typeof context.subViews === 'undefined') {
            context.subViews = [];
        }
        context.subViews.push(view);
    	log.debug("DONE Creating item view.")
        return view;
    };
    
//    // TODO
    var createModalView = function(template, options) {
//    	this.cleanupView(GLOBAL_MODAL_VIEW);
//    	GLOBAL_MODAL_VIEW = Vm.createView({}, 'ModalView', BaseView, _.extend({
//    		template : formTemplate,
//    	}, options));
//    	var modal = new Backbone.BootstrapModal({ content : this.formView });
//
////    	GLOBAL_MODAL_VIEW;
    };

    var createView = function(context, name, View, options) {

        this.closeView(context, name);

        log.debug("Creating view " + name);
        var view = new View(options);

        this.views[name] = view;

        if (typeof context.subViews === 'undefined') {
            context.subViews = [];
        }
        context.subViews.push(view);

        log.debug("DONE Creating view "  + name);
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
    
    var navigateTo = function(path) {
    	if (! this.router) {
    		log.warn("No router was set for ViewManager.");
    		return;
    	}
    	this.router.navigate(path, true);
    };
    
    var setRouter = function(theRouter) {
    	this.router = theRouter;
    };

    return {
        views : {},
        createView : createView,
        createSubView : createSubView,
        createModalView : createModalView,
        closeView : closeView,
        reuseView : reuseView,
        cleanupView : cleanupView,
        cleanupSubViews : cleanupSubViews,
        navigateTo : navigateTo,
        setRouter : setRouter,
    };

});