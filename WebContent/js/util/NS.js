define([
        'jquery',
        'logging'
], function($, logging) {

    var log = logging.getLogger("NS");
    
    log.trace("Retrieving RDF URLs.");

    var NS = {};

    $.ajax({
        async : false,
        url : 'api/ns',
        success : function(data) {
            log.trace("Success retrieving RDF URLs.")
            NS = data;
        },
        error : function() {
            log.error("Couldn't retrieve RDF URLs. :(");
        },
        dataType : 'json',
    });

    return NS;
});