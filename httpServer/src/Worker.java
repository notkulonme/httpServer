import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;

public class Worker implements Runnable {

    private final Socket socket;
    private final String CRLF = "\r\n";


    private final BufferedReader reader;
    private final OutputStream writer;


    public Worker(Socket socket) {
        this.socket = socket;
        InputStream bufferin;
        OutputStream bufferout;
        try {
            bufferin = socket.getInputStream();
            bufferout = socket.getOutputStream();
        }catch (IOException e) {
            bufferin = null;
            bufferout = null;
        }
        writer = bufferout;
        reader = new BufferedReader(new InputStreamReader(bufferin));
    }

    public void run() {

        Controller.logger.log(socket.getInetAddress() + " connected");
        String request;

        try {

            request = readRequest();
            RequestParser parser = new RequestParser(request);

            ResponseBuilder response;
            if (!parser.error) {
                Controller.logger.log(parser.requestType + " " + parser.requestedFile);
                if (parser.requestType.equalsIgnoreCase("get")) {
                    File f = new File(Controller.WEBROOT + parser.requestedFile);

                    response = GEThandler(f, parser);

                    writer.write(response.getResponse());
                } else if (parser.requestType.equalsIgnoreCase("post")) {

                    response = POSThandler(parser);

                    writer.write(response.getResponse());
                }
            } else {
                //in case of an error
                writer.write(("HTTP/1.1 " + parser.errorType + CRLF + "Connection: close" + CRLF + CRLF).getBytes());
            }
            writer.flush();


        } catch (IOException e) {

            //this probably need more error handling but i don't wanna do it :c
            Controller.logger.log("Error while handling the request");
        }
        try {
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private ResponseBuilder POSThandler(RequestParser parser) throws IOException {
        ResponseBuilder response = new ResponseBuilder();
        response.addToHeader("HTTP/1.1 200 ok");
        ResponseBuilder file = GEThandler(new RequestParser(Controller.WEBROOT+"/index.html","html"));
        response.addToHeader(file.getByteHeader());
        response.addToBody(file.getByteBody());
        return response;
    }




    private ResponseBuilder GEThandler(File f, RequestParser parser) throws IOException {
        ContentType ct = new ContentType();
        ResponseBuilder response = new ResponseBuilder();
        if ((f.exists() || parser.requestedFile.equals("/")) && !f.isDirectory()) {
            byte[] content = Files.readAllBytes(f.toPath());
            if (ct.isText(parser.fileType)) {

                response.addToHeader("HTTP/1.1 200 ok");
                response.addToHeader("Content-Type: text/" + parser.fileType + "; charset=utf-8");
                response.addToHeader("Content-Length: " + content.length);
                response.addToHeader("Connection: close");
                response.addToBody(f);


            } else {

                response.addToHeader("HTTP/1.1 200 ok");
                response.addToHeader("Content-Type: image/" + parser.fileType);
                response.addToHeader("Connection: close");
                response.addToBody(f);

            }

        } else {
            if ((parser.fileType.equals("null") || ct.isText(parser.fileType)) && Controller.conf.getBool("404")) {
                f = new File(Controller.WEBROOT + "/404/404.html");
                byte[] content = Files.readAllBytes(f.toPath());
                response.addToHeader("HTTP/1.1 404 Not Found");
                response.addToHeader("Content-Type: text/html");
                response.addToHeader("Content-Length: " + content.length);
                response.addToHeader("Connection: close");
                response.addToBody(f);
            } else {
                response.addToHeader("HTTP/1.1 404 Not Found");
                response.addToHeader("Connection: close");
            }

        }
        return response;
    }

    private ResponseBuilder GEThandler(RequestParser parser) throws IOException {
        File f = new File(parser.requestedFile);
        ResponseBuilder response = new ResponseBuilder();
        ContentType ct = new ContentType();
        if (f.exists() && !f.isDirectory()) {
            byte[] content = Files.readAllBytes(f.toPath());
            if (ct.isText(parser.fileType)) {
                response.addToHeader("Content-Type: text/" + parser.fileType + "; charset=utf-8");
                response.addToHeader("Content-Length: " + content.length);
                response.addToHeader("Connection: close");
                response.addToBody(f);


            } else {
                response.addToHeader("Content-Type: image/" + parser.fileType);
                response.addToHeader("Connection: close");
                response.addToBody(f);
            }
        }
        return response;
    }


        private String readRequest () throws IOException {
            final StringBuilder request = new StringBuilder();
            Instant start = Instant.now();
            while (!reader.ready()) {
                Instant end = Instant.now();
                Duration duration = Duration.between(start, end);
                if (duration.toSeconds() >= Controller.conf.getInt("timeOut")) {
                    return "error 408 \r\n";
                }
            }
            while (reader.ready()) {
                request.append((char) reader.read());
            }
            return request.toString();
        }

        private void close () throws IOException {
            if (reader != null && writer != null) {
                reader.close();
                writer.close();
            } else
                Controller.logger.log("The socket variables haven't been initialized.");

            Controller.logger.log(socket.getInetAddress() + " connection closed");
            socket.close();
        }
    }