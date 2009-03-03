package fr.inria.peerunit.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 
 * This objects of this class format the logging messages in conformity with
 * the values of properties file.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see PeerUnitLogger
 */
public class LogFormat extends Formatter{
	
	/**
	 * The delimiter used for the logging.
	 */
	private static String delimiter=TesterUtil.getDelimiter();
	
	/**
	 * The date format used for the logging.
	 */
	private static String dateformat=TesterUtil.getDateformat();

	/**
	 * The time format used for the logging.
	 */	
	private static String timeformat=TesterUtil.getTimeformat();

	/**
	 *  This method is overridden to apply our own formatting
	 *  
	 *  @param rec  the content to format
	 *  @return the string resulting from formatting
	 */
	@Override
	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		
		Timestamp time = new Timestamp(System.currentTimeMillis());
		
		SimpleDateFormat format = new SimpleDateFormat(dateformat+"' '"+timeformat);
				
		buf.append(format.format(time));
        buf.append(delimiter);
        buf.append(rec.getLevel());
        buf.append(delimiter);        
        buf.append(rec.getSourceMethodName());
        buf.append(delimiter);
        buf.append(formatMessage(rec));
        buf.append('\n');
        return buf.toString();		
	}
}
