package uni.sc.util.logger;
import java.util.logging.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static uni.sc.util.logger.MMHTMLFormatterForLog.levelName;

/**
 * Crea una formattazione personalizzata per il log su console e su file txt
 * Created by Matteo on 14/09/15.
 */
public class MMLogFormatter extends Formatter {

    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(df.format(new Date(record.getMillis()))).append(" - ");
        // builder.append("[").append(record.getSourceClassName()).append(".");
        // builder.append(record.getSourceMethodName()).append("] - ");
        builder.append("[").append(levelName(record)).append("] - ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}