import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;


public class Main {
    /**
     *
     * @param args should contain the path to the .rson file
     */
    public static void main(String[] args) {
        final var controller = new Controller(args[0]); //collector of the most important variables across threads
        final var threadPool = Executors.newFixedThreadPool(Controller.conf.getInt("maxThread")+1); //a thread pool

        Controller.logger.log(controller.startMessage);

        threadPool.execute(controller::userController);//starts the user controller

        while (Controller.run) {
            Socket socket = null;
            try {
                socket = controller.server.accept();
            } catch (SocketException e) {
                if (Controller.run)
                    Controller.logger.log("Error while communicating with the client");
            } catch (IOException e) {
                Controller.logger.log("Error while accepting connection");
            }

            if (socket != null) {
                final var worker = new Worker(socket);
                threadPool.execute(worker);
            }
        }
        threadPool.shutdownNow();
        Controller.logger.log("http server stopped");
    }
}
