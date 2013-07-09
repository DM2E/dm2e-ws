define([ 'log4javascript' ], function(log4javascript) {
	
	var consoleAppender = new log4javascript.BrowserConsoleAppender();
	var patternLayout = new log4javascript.PatternLayout("%-5p: [%c] %m");
	consoleAppender.setLayout(patternLayout);

	
	return { 
		getLogger: function(loggerName) {
			log = log4javascript.getLogger(loggerName);
			log.addAppender(consoleAppender);
			log.setLevel(log4javascript.Level.TRACE);
			return log;
		}
	}

});