define([
	'jquery',
	'underscore',
	'BaseView',
	'logging',
    'util/themeSwitcher',
	'singletons/UserSession',
	'text!templates/header/menuTemplate.html'
], function($,
	_,
	BaseView,
	logging,
    themeSwitcher,
	userSession,
	menuTemplate) {

	var log = logging.getLogger("MenuView");

	var HeaderMenuView = BaseView.extend({
		
		template: menuTemplate,

        events: {
            "click #switch-theme" : function() {
                themeSwitcher.toggle();
                userSession.get("user").setQN("omnom:preferredTheme", themeSwitcher.getTheme());
                userSession.get("user").save();
                this.render();
            }
        },
		initialize : function() {
			log.trace("MenuView initialized.");
			this.model = userSession.get("user");
		},
		render : function() {
            this.renderModel();
            this.$("#switch-theme").html(userSession.get("user").getQN("omnom:preferredTheme"));
        },
//			this.$el.html(_.template(menuTemplate, {
//				user : userSession.get("user")
//			}));
//			$('a[href="' + window.location.hash + '"]').parent().addClass('active');
//			return this;
//		},
//		events : {
//			'click a' : 'highlightMenuItem'
//		},
//		highlightMenuItem : function(ev) {
//			$('.active').removeClass('active');
//			$(ev.currentTarget).parent().addClass('active');
//		}
	});

	return HeaderMenuView;
});
