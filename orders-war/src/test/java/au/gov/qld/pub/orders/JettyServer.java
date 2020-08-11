package au.gov.qld.pub.orders;


import java.util.Properties;

import org.eclipse.jetty.server.Server;

import com.dumbster.smtp.SimpleSmtpServer;

public class JettyServer {
    private static Server server;
    private static SimpleSmtpServer mailServer;

    public static void main(String[] args) throws Exception {
        start();
        server.join();
        stop();
    }

    public static void start() throws Exception {
        //server = new Server(8091);
        Properties applicationProps = new Properties();
        applicationProps.load(JettyServer.class.getClassLoader().getResourceAsStream("application.properties"));

       // WebAppContext context = new WebAppContext();
       // context.setDescriptor("/WEB-INF/web.xml");
       // context.setResourceBase("src/main/webapp");
       // context.setContextPath(applicationProps.getProperty("web.context"));
        //context.setParentLoaderPriority(true);

        mailServer = SimpleSmtpServer.start(Integer.valueOf(applicationProps.getProperty("mail.port")));
       // server.setHandler(context);

        //server.start();
        System.out.println("Started");
    }

    public static void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
        if (mailServer != null) {
            mailServer.stop();
        }
    }
}