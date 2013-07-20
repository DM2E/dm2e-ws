//Filename: filename

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'BaseView',
    'text!templates/help/helpTemplate.html'
], function($,
            _,
            logging,
            Vm,
            BaseView,
            theTemplate
    ) {

    // var log = logging.getLogger("filename");

    return BaseView.extend({

        template : theTemplate,

//        initialize : function() {
//            this.listenTo(this.model, "change", this.render);
//        },

    });
});
