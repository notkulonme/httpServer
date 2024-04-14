import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException {
        final var controller = new Controller(args[0]); //collector of the most important variables across threads and runs the user controller
        final var threadPool = Executors.newFixedThreadPool(Controller.conf.getInt("maxThread"));

        Controller.logger.log(controller.startMessage);

        while (Controller.run) {
            Socket socket = null;
            try {
                socket = controller.server.accept();
            } catch (SocketException e) {
                if (Controller.run)
                    Controller.logger.log("Error while communicating with the client");
            }

            if (socket != null) {
               final var worker = new Worker(socket);
                threadPool.execute(worker);
            }
        }
        threadPool.shutdown();
        Controller.logger.log("http server stopped");
    }
}