import Logger.Logger;
import RSON.RSON;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {


    //these variables are just read only

    final public ServerSocket server;
    final private String confPath;
    volatile public static RSON conf = null;
    public static int PORT = -1;
    public static String WEBROOT = null;
    public static volatile boolean run = true;
    public static int maxThread = -1;
    public static Logger logger = null;
    public String startMessage = "The server is started";

    public Controller(String confPath) {
        this.confPath = confPath;
        load();
        ServerSocket server;
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            server = null;
            Controller.logger.log("Listener port is invalid");
            run = false;
        }
        this.server = server;
        Thread uc = new Thread(this::userController);
        uc.start();
    }

    private void load() {
        Controller.conf = new RSON(this.confPath);
        Controller.PORT = conf.getInt("port");
        Controller.WEBROOT = conf.getValue("webroot");
        Controller.maxThread = conf.getInt("maxThread");
        logger = new Logger(conf.getValue("loggerpath"));
    }


    private void userController() {
        ServerSocket ssc = null;
        try {
            ssc = new ServerSocket(28852);
            BufferedReader reader = null;
            BufferedWriter writer = null;
            while (run) {
                Socket sc = ssc.accept();
                logger.log(sc.getInetAddress() + ": connected to the controller");
                reader = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                String command = reader.readLine();
                logger.log("user command: " + command);
                writer = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream()));
                switch (command) {
                    case "q", "stop" -> {
                        writer.write("server is stopped");
                        run = false;
                        try {
                            server.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    case "status" -> {
                        logger.log("The server is running");
                        writer.write("The server is running");
                    }
                    case "help" -> {
                        String msg = """
                                All commands:
                                \t\tstatus -> prints the status
                                \t\tstop -> stops the server""";
                        writer.write(msg);
                    }

                    default -> {
                        logger.log("invalid command type 'help' for help");
                        writer.write("invalid command type 'help' for help");
                    }
                }
                try {
                    writer.write("\"");
                    writer.flush();
                    logger.log("response sent to the controller");
                } catch (RuntimeException e) {
                    logger.log("error while sending data");
                }
                reader.close();
                writer.close();
                sc.close();
            }
            ssc.close();
        } catch (IOException e) {
            logger.log("error while communicating with the controller");
        }
    }

}
