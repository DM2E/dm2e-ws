// Filename: main.js

// Configure Require.JS
require.config({
	// http://requirejs.org/docs/api.html#config-shim
	shim : {
		'backbone' : {
			// These script dependencies should be loaded before loading
			// backbone.js
			deps : [ 'underscore', 'jquery' ],
			// Once loaded, use the global 'Backbone' as the
			// module value.
			exports : 'Backbone'
		},
		'underscore' : {
			exports : '_',
		},
		'log4javascript': {
			exports: 'log4javascript'
		},
		'bootstrap' : {
		    deps: ["jquery"]
		}
		
	},
	paths : {
		jquery : 'lib/jquery/jquery',
		underscore : 'lib/underscore/underscore',
		backbone : 'lib/backbone/backbone',
		bootstrap : 'lib/bootstrap/bootstrap',
		jasny_bootstrap : "lib/jasny-bootstrap/jasny-bootstrap",
		text : 'lib/text',
		log4javascript : 'lib/log4javascript/log4javascript',
		NS : 'util/NS',
	}

});

// Actually start the whole thing
require([
     'router',
     'views/app',
     'vm',
], function(Router, AppView, Vm){
    
    var appView = Vm.createView({}, 'AppView', AppView);
    appView.render();

    Router.initialize({appView: appView});  // The router now has a copy of all main appview
});
