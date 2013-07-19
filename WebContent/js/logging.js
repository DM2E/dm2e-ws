define([ 'log4javascript' ], function(log4javascript) {
	
	var consoleAppender = new log4javascript.BrowserConsoleAppender();
	// FIXME disable to spit out objects nicely
	var patternLayout = new log4javascript.PatternLayout("%-5p: [%c] %m");
	consoleAppender.setLayout(patternLayout);
	log4javascript.getRootLogger().addAppender(consoleAppender);

//	var dumpAppender = new log4javascript.BrowserConsoleAppender();
//	log4javascript.getRootLogger().addAppender(dumpAppender);
//
	log4javascript.getLogger("vm").setLevel(log4javascript.Level.DEBUG);
	
	return {
		getLogger: function(loggerName) {
			return log4javascript.getLogger(loggerName);
		},
//		getDumper: function(loggerName) {
//			log4javascript
//		}
//		
//			var log = log4javascript.getLogger(loggerName);
//			log.addAppender(consoleAppender);
//			log.setLevel(log4javascript.Level.ALL);
//			return log;
//		}
	};

});