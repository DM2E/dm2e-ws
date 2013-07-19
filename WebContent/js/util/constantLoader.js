define([
	'jquery',
	'logging'
], function($, logging) {
	var loadConstants = function(url) {
		var log = logging.getLogger("constantLoader");

		log.debug("Retrieving constants from " + url);

		var constants = {};
		$.ajax({
			async : false,
			url : url,
			success : function(data) {
				log.debug("Success retrieving constants.");
				constants = data;
			},
			error : function() {
				log.error("Couldn't retrieve constants. :(");
			},
			dataType : 'json',
		});
		return constants;
	};
	return {
		from : loadConstants
	};
});