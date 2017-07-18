package uni.sc.util.logger;

import uni.sc.util.MMConfig;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Crea una formattazione personalizzata per il log su file HTML
 * Created by Matteo on 15/04/15.
 */
class MMHTMLFormatterForLog extends Formatter {

    public String prj_name;
    public String description;
    public String title;

    public String getPrj_name() {
        return prj_name;
    }

    public void setPrj_name(String prj_name) {
        this.prj_name = prj_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String format(LogRecord rec) {
        StringBuffer buf = new StringBuffer(1000);

        // colorize any levels >= WARNING in red
        if (rec.getLevel().intValue() > Level.WARNING.intValue()) {
            buf.append("\t<tr class=\"info\">\n");
        } else if (rec.getLevel().intValue() > Level.INFO.intValue()) {
            buf.append("\t<tr class=\"success\">\n");
        } else if (rec.getLevel().intValue() > Level.CONFIG.intValue()) {
            buf.append("\t<tr class=\"danger\">\n");
        } else if (rec.getLevel().intValue() > Level.FINE.intValue()) {
            buf.append("\t<tr class=\"warning\">\n");
        } else if (rec.getLevel().intValue() >= Level.FINER.intValue()) {
            buf.append("\t<tr class=\"active\">\n");
        } else {
            buf.append("\t<tr>\n");
        }

        // colorize any levels >= WARNING in red
        if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
            buf.append("\t<td style=\"font-size: 14px;\">");
            buf.append("<b>");
            buf.append(levelName(rec));
            buf.append("</b>");
        } else {
            buf.append("\t<td>");
            buf.append(levelName(rec));
        }

        buf.append("</td>\n");
        buf.append("\t<td>");
        buf.append(calcDate(rec.getMillis()));
        buf.append("</td>\n");
        buf.append("\t<td>");
        buf.append(formatMessage(rec));
        buf.append("</td>\n");
        buf.append("</tr>\n");

        return buf.toString();
    }

    public static String levelName(LogRecord rec) {

        if(rec.getLevel().intValue() == Level.SEVERE.intValue()) {
            return "Fase        ";
        } else if(rec.getLevel().intValue() == Level.WARNING.intValue()) {
            return "Task        ";
        } else if(rec.getLevel().intValue() == Level.INFO.intValue()) {
            return "Azione      ";
        } else if(rec.getLevel().intValue() == Level.CONFIG.intValue()) {
            return "Metodo      ";
        } else if(rec.getLevel().intValue() == Level.FINE.intValue()) {
            return "Informazione";
        } else if(rec.getLevel().intValue() == Level.FINER.intValue()) {
            return "Debug       ";
        } else {
            return "Core level  ";
        }

    }

    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    public String getHead(Handler h) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        Date today = new Date();

        MMConfig config = new MMConfig();

        String prj_name = this.prj_name;
        String description = this.description;
        String title = this.title;

        return "<!DOCTYPE html>\r\n<html lang=\"en\">\r\n  <head>\r\n    <meta charset=\"utf-8\">\r\n    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->\r\n    <title>"+prj_name+"</title>\r\n\r\n    <!-- Bootstrap -->\r\n    <link href=\"../lib/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n\r\n    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->\r\n    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->\r\n    <!--[if lt IE 9]>\r\n      <script src=\"https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js\"></script>\r\n      <script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\r\n    <![endif]-->\r\n  </head>\r\n  <body>\r\n  <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->\r\n    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>\r\n    <!-- Include all compiled plugins (below), or include individual files as needed -->\r\n    <script src=\"../lib/js/bootstrap.min.js\"></script>"
                + "<body class=\" ext-webkit ext-chrome ext-mac\">\r\n\r\n    <div class=\"container\">\r\n <div class=\"jumbotron\">\r\n        <h3>"+title+"</h3>\r\n        <p class=\"lead\">"+description+"</p>\r\n      </div>\r\n\r\n      <div class=\"row marketing\">\r\n"

                + "<h1>Giorno: " + (date_format.format(today)) + "</h1>\n"
                + "<table class=\"table\">\r\n      <thead>\r\n        <tr>\r\n          <th style=\"width: 15%;\">Livello</th>\r\n          <th style=\"width: 20%;\">Giorno e ora</th>\r\n          <th style=\"width: 65%;\">Messaggio</th>\r\n     </tr>\r\n      </thead>\r\n      <tbody>";
    }

    public String getTail(Handler h) {
        return "</table>"
                +"</div>\r\n\r\n      <footer class=\"footer\">\r\n        <p>\u00A9 Matteo Madeddu - Dipartimento di Informatica 2015</p>\r\n      </footer>\r\n\r\n    </div> <!-- /container -->\r\n\r\n\r\n    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->\r\n    <script src=\"../../assets/js/ie10-viewport-bug-workaround.js\"></script>\r\n  \r\n\r\n<iframe id=\"rdbIndicator\" width=\"100%\" height=\"270\" border=\"0\" src=\"chrome-extension://oknpjjbmpnndlpmnhmekjpocelpnlfdi/indicator.html\" style=\"display: none; border: 0; position: fixed; left: 0; top: 0; z-index: 2147483647\"></iframe>"
                +"\n</body>\n</html>";
    }

}
