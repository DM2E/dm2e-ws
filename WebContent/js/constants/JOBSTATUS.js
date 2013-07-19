define([
	'jquery',
	'logging',
	'util/constantLoader'
], function($, logging, loadConstants) {

	return loadConstants.from('api/constants/jobStatus');
});