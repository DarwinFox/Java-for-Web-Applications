
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import java.util.Vector;

@WebListener
public class SessionListener implements HttpSessionListener, HttpSessionIdListener
{
    private SimpleDateFormat formatter =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    @Override
    public void sessionCreated(HttpSessionEvent e)
    {
        System.out.println(this.date() + ": Session " + e.getSession().getId() +
                " created.");
        SessionRegistry.addSession(e.getSession());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent e)
    {
        System.out.println(this.date() + ": Session " + e.getSession().getId() + "" +
                " destroyed.");

        Vector<PageVisit> visits =
                (Vector<PageVisit>)e.getSession().getAttribute("activity");
        SimpleDateFormat f = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(e.getSession().getId() + ".txt"), "utf-8"))) {
                if (!e.getSession().isNew()) {
                    for (PageVisit visit : visits) {
                        try {
                            writer.write("Session " + e.getSession().getId() +
                                    " activity for Username: " + "" + e.getSession().getAttribute("username") + " at URL: ");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            writer.write(visit.getRequest());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        if (visit.getIpAddress() != null)
                            writer.write(" from IP " + visit.getIpAddress().getHostAddress());
                        writer.write(" (" + f.format(new Date(visit.getEnteredTimestamp())));
                        if (visit.getLeftTimestamp() != null) {
                            writer.write(", stayed for " + toString(
                                    visit.getLeftTimestamp() - visit.getEnteredTimestamp()
                            ) + "\n\n");
                        }
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        SessionRegistry.removeSession(e.getSession());
    }

    @Override
    public void sessionIdChanged(HttpSessionEvent e, String oldSessionId)
    {
        System.out.println(this.date() + ": Session ID " + oldSessionId +
                " changed to " + e.getSession().getId());
        SessionRegistry.updateSessionId(e.getSession(), oldSessionId);
    }

    private String date()
    {
        return this.formatter.format(new Date());
    }
    private static String toString(long timeInterval)
    {
        if(timeInterval < 1_000)
            return "less than one second";
        if(timeInterval < 60_000)
            return (timeInterval / 1_000) + " seconds";
        return "about " + (timeInterval / 60_000) + " minutes";
    }
}
