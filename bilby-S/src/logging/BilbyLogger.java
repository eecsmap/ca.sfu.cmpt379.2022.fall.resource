package logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class is a Facade for java.util.logging.
 * It also retains the number of messages that have been logged.
 * Optionally, one may configure this logger to throw an exception
 * when a certain message count is reached.
 * <p>
 * Retains hard references to all loggers created.
 */ 

public class BilbyLogger {
	private static Map<String, BilbyLogger> loggers = new HashMap<String, BilbyLogger>();
	private static int numMessages = 0;
	private static int maxMessagesBeforeQuit = Integer.MAX_VALUE;
	
	////////////////////////////////////////////////////////////////
	// static interface
	public static BilbyLogger getLogger(String loggerName) {
		if(!loggers.containsKey(loggerName)) {
			loggers.put(loggerName, new BilbyLogger(loggerName)); 
		}
		return loggers.get(loggerName);
	}
	public static boolean hasErrors() {
		return numMessages != 0;
	}
	public static void setMaximumErrorMessages(int numMessages) {
		maxMessagesBeforeQuit = numMessages;
	}
	
	////////////////////////////////////////////////////////////////
	// per-instance code
	private Logger logger;
	private BilbyLogger(String loggerName) {
		logger = Logger.getLogger(loggerName);
	}
	
	public void log(Level level, String message) {
		logger.log(level, message);
		incrementNumMessages();
	}
	public void severe(String message) {
		log(Level.SEVERE, message);
	}
	private void incrementNumMessages() {
		numMessages++;
		if(numMessages >= maxMessagesBeforeQuit) {
			throw new BilbyLoggerException("Too many error messages.  Aborting.");
		}
	}
	
	////////////////////////////////////////////////////////////////
	// Exception to be thrown
	public class BilbyLoggerException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public BilbyLoggerException(String string) {
			super(string);
		}
	}
}
