import java.util.HashMap;

public class RequestParser {
    String wholeRequest;
    String requestType; // type of the request for example: GET or POST
    String requestedFile; // the files name and type (index.html)
    String fileType; //just the type of the file without dot (html)
    Boolean error; // by default, it is false, true if there is an error
    String errorType = "";
    HashMap<String, String> header = new HashMap<>();
    HashMap<String, String> body = new HashMap<>();


    public RequestParser(String request) {
        this.wholeRequest = request;
        String[] splitedRequest = request.split("\r\n\r\n");
        String header = splitedRequest[0];
        String body = "";
        if (splitedRequest.length==2)
            body = splitedRequest[1];

        parseHeader(header);
        if (!body.isEmpty()) parseBody(body);

    }

    public void parseHeader(String header) {
        try {
            error = false;


            String[] headerSplited = header.split("\r\n");
            String[] data = headerSplited[0].split(" ");
            requestType = data[0];
            requestedFile = data[1];
            requestedFile = requestedFile.replaceAll("%20", " ");
            switch (requestedFile) {
                case "/" -> {
                    fileType = "html";
                    requestedFile = "/index.html";
                }
                case "408" -> {
                    requestType = null;
                    requestedFile = null;
                    error = true;
                    errorType = "408 Request Timeout";
                    Controller.logger.log("Request time out.");
                }
                case "closedSocket" -> {
                    requestType = null;
                    requestedFile = null;
                    error = true;
                    errorType = "Closed socket";
                    Controller.logger.log("the socket is closed");
                }
                default -> {
                    String[] arr = requestedFile.split("\\.");
                    fileType = arr[arr.length - 1];
                    if (fileType.equals(requestedFile))
                        fileType = "null";
                }
            }
            try {
                for (int i = 1; i < headerSplited.length; i++) {
                    String[] headerData = headerSplited[i].split(": ");
                    this.header.put(headerData[0], headerData[1]);
                }
            } catch (RuntimeException e) {
                Controller.logger.log("Not properly set header.");
                errorType += "Not properly set header.";
            }


        } catch (Exception e) {
            requestType = null;
            requestedFile = null;
            error = true;

            Controller.logger.log("Error while parsing the request");
            errorType = "500 Internal Server Error";
        }
    }


    public void parseBody(String body) {
        String[] boddySplited = body.split("\r\n");
        try {

            for (String s : boddySplited) {
                String[] bodyData = s.split(": ");
                this.body.put(bodyData[0], bodyData[1]);
            }
        } catch (RuntimeException e) {
            Controller.logger.log("Not properly set body.");
            errorType += "Not properly set body.";
        }
    }

    public RequestParser(String requestedFile, String fileType) {
        this.requestedFile = requestedFile;
        this.fileType = fileType;
    }
}
