//Filename: UserModel.js

define([
        'jquery',
        'underscore',
        'backbone',
        'logging',
], function($, _, Backbone, logging) {

    var log = logging.getLogger("UserModel");

    return Backbone.Model.extend({
        
        defaults : {
            fileServices : [
                    'api/file',
                    'api/mint-file'
            ],
            configService : 'api/config',
            workflowService : 'api/workflow',
            services : [
                    'api/service/xslt',
                    'api/service/xslt-zip',
                    'api/service/publish',
                    'api/service/demo'
            ]
        }
    });
});