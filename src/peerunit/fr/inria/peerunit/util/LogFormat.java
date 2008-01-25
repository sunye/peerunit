package fr.inria.peerunit.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormat extends Formatter{
	
	private static String delimiter=TesterUtil.getDelimiter();
	
	private static String dateformat=TesterUtil.getDateformat();
	
	private static String timeformat=TesterUtil.getTimeformat();

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
