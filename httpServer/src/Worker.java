import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;

public class Worker implements Runnable {
    //very important variables
    private final Socket socket;
    private final String CRLF = "\r\n";


    private final BufferedReader reader;
    private final OutputStream writer;


    public Worker(Socket socket) {
        Controller.logger.log(socket.getInetAddress() + " connected");
        this.socket = socket;
        InputStream bufferin;
        OutputStream bufferout;
        try {
            bufferin = socket.getInputStream();
            bufferout = socket.getOutputStream();
        } catch (IOException e) {
            bufferin = null;
            bufferout = null;
        }
        writer = bufferout;
        reader = new BufferedReader(new InputStreamReader(bufferin));
    }

    /***
     * The method is started by the Thread pool
     * Starts the whole data processing
     */
    public void run() {

       if (writer != null && reader !=null){
           try {
               process();
           } catch (IOException e) {
            Controller.logger.log("Error while writing to the output stream of "+socket.getInetAddress()+", the connection is closed");
        }
       }
        close();
    }

    /**
     *processes the request
     * @throws IOException only if the output stream is unwriteable
     */
    public void process() throws IOException {

        String request = readRequest();


        RequestParser parser = new RequestParser(request);

        ResponseBuilder response = null;

            if (!parser.error) {
                Controller.logger.log(parser.requestType + " " + parser.requestedFile);
                if (parser.requestType.equalsIgnoreCase("get")) {

                    parser.requestedFile = Controller.WEBROOT + parser.requestedFile;
                    response = GEThandler(parser);

                } else if (parser.requestType.equalsIgnoreCase("post")) {

                    response = POSThandler(parser);
                }
                if (response != null) {
                    writer.write(response.getResponse());
                }
            } else {
                //Handles the error
                writer.write(("HTTP/1.1 " + parser.errorType + CRLF + "Connection: close" + CRLF + CRLF).getBytes());
            }
            writer.flush();

    }

    /**
     * IN TEST PHASE!!!
     *Handles the POST request
     */
    private ResponseBuilder POSThandler(RequestParser parser) {
        ResponseBuilder response = new ResponseBuilder();
        response.addToHeader("HTTP/1.1 200 ok");
        ResponseBuilder file = GEThandler(new RequestParser(Controller.WEBROOT + "/index.html", "html"));
        response.addToHeader(file.getByteHeader());
        response.addToBody(file.getByteBody());
        return response;
    }


    /**
     * Handles the GET requests according to the HTTP standard
     * reads the file into the ram stores it in a ResponseBuilder
     *
     * @return a ResponseBuilder stroring the header and the body of the Response in a byte array form
     */
    private ResponseBuilder GEThandler(RequestParser parser) {
        File f = new File(parser.requestedFile);
        ContentType ct = new ContentType();
        ResponseBuilder response = new ResponseBuilder();
        if ((f.exists() || parser.requestedFile.equals("/")) && !f.isDirectory()) {
            byte[] content;
            try {
                content = Files.readAllBytes(f.toPath());
            } catch (IOException e) {
                Controller.logger.log("Error while reading from the " + f.getName() + " file");
                content = new byte[0];
            }

            if (ct.isText(parser.fileType)) {

                response.addToHeader("HTTP/1.1 200 ok");
                response.addToHeader("Content-Type: text/" + parser.fileType + "; charset=utf-8");
                response.addToHeader("Content-Length: " + content.length);
                response.addToHeader("Connection: close");
                response.addToBody(content);


            } else {

                response.addToHeader("HTTP/1.1 200 ok");
                response.addToHeader("Content-Type: image/" + parser.fileType);
                response.addToHeader("Connection: close");
                response.addToBody(content);

            }

        } else {
            if ((parser.fileType.equals("null") || ct.isText(parser.fileType)) && Controller.conf.getBool("404")) {
                f = new File(Controller.WEBROOT + "/404/404.html");
                byte[] content;
                try {
                    content = Files.readAllBytes(f.toPath());
                } catch (IOException e) {
                    Controller.logger.log("Error while reading from the " + f.getName() + " file");
                    content = new byte[0];
                }
                response.addToHeader("HTTP/1.1 404 Not Found");
                response.addToHeader("Content-Type: text/html");
                response.addToHeader("Content-Length: " + content.length);
                response.addToHeader("Connection: close");
                response.addToBody(content);
            } else {
                response.addToHeader("HTTP/1.1 404 Not Found");
                response.addToHeader("Connection: close");
            }

        }
        return response;
    }

    /**
     * reads the request from the InputStream
     *
     * @return the readed request
     * @return error and type of the error based on the HTTP standard
     */
    private String readRequest() {
        final StringBuilder request = new StringBuilder();
        Instant start = Instant.now();
        int timeOut = Controller.conf.getInt("timeOut");
        try {
            while (!reader.ready()) {
                Instant end = Instant.now();
                Duration duration = Duration.between(start, end);
                if (duration.toSeconds() >= timeOut) {
                    return "error 408 \r\n";
                }
            }
            while (reader.ready()) {
                request.append((char) reader.read());
            }
        } catch (IOException e) {
            Controller.logger.log("Error while reading from " + socket.getInetAddress() + "'s input stream");
            return "error 500 \r\n";
        }
        return request.toString();
    }

    /**
     *Closes all resources and the socket
     */
    private void close() {
        try {
            if (reader != null && writer != null) {
                reader.close();
                writer.close();
            } else
                Controller.logger.log("The socket variables haven't been initialized.");

            Controller.logger.log(socket.getInetAddress() + " connection closed");
            socket.close();
        } catch (IOException e) {
            Controller.logger.log("Resources can't be released");
        }
    }
}