import java.util.HashMap;

/**
 * The RequestParser class is responsible for parsing HTTP requests and extracting relevant information
 * such as request type, requested file, file type, headers, and body content.
 */

public class RequestParser {

    String requestType; // type of the request for example: GET or POST
    String requestedFile; // the files name and type (index.html)
    String fileType; //just the type of the file without dot (html)
    Boolean error; //shows if there is an error
    String errorType = ""; //the error type based on the HTTP standard
    HashMap<String, String> headerContent = new HashMap<>();//content of the header
    HashMap<String, String> bodyContent = new HashMap<>();//content of the body

    /**
     *
     * @param request The HTTP request string to parse.
     */
    public RequestParser(String request) {

        String[] splitedRequest = request.split("\r\n\r\n");
        String header = splitedRequest[0];
        String body = "";
        if (splitedRequest.length == 2) {
            body = splitedRequest[1];
        }

        parseHeader(header);
        if (!body.isEmpty()) {
            parseBody(body);
        }

    }

    /**
     * starts parsing the header
     * @param header the string content of the header
     */
    private void parseHeader(String header) {
        String[] headerSplited = new String[0];
        try {
            headerSplited = header.split("\r\n");
            String[] firstLine = headerSplited[0].split(" ");
            requestType = firstLine[0];
            requestedFile = firstLine[1];
            requestedFile = requestedFile.replaceAll("%20", " ");
        } catch (RuntimeException e) {
           setError("400 Bad Request");
        }
        if (!requestType.equalsIgnoreCase("error") || error) {
            parseHeaderInNormalCase(headerSplited);
        } else {
            parseHeaderInCaseOfError();
        }

    }

    /***
     *parses the header content if there is no error
     * @param headerSplited the header content spilt into an array
     */

    private void parseHeaderInNormalCase(String[] headerSplited) {
        switch (requestedFile) {
            case "/" -> {
                fileType = "html";
                requestedFile = "/index.html";
            }
            default -> {
                String[] arr = requestedFile.split("\\.");
                fileType = arr[arr.length - 1];
                if (fileType.equals(requestedFile))
                    fileType = "null";
            }
        }
        error = false;
        try {
            for (int i = 1; i < headerSplited.length; i++) {
                String[] headerData = headerSplited[i].split(": ");
                this.headerContent.put(headerData[0], headerData[1]);
            }
        } catch (RuntimeException e) {
            setError("400 Bad Request");
        }
    }

    /***
     * Parses the header in case of an error
     */

    private void parseHeaderInCaseOfError() {
        switch (requestedFile) {
            case "408" -> {
                setError("408 Request Timeout");
                Controller.logger.log("Request time out.");
            }
            default -> {
                setError("500 Internal Server Error");
                Controller.logger.log("Internal Server Error");
            }
        }
    }

    /**
     * parses the body
     * @param body string content of the http request's body
     */
    private void parseBody(String body) {
        String[] bodySplited = body.split("\r\n");
        try {

            for (String s : bodySplited) {
                String[] bodyData = s.split(": ");
                this.bodyContent.put(bodyData[0], bodyData[1]);
            }
        } catch (RuntimeException e) {
            setError("400 Bad Request");
        }
    }

    /**
     * sets the error and releases the resources
     * @param type Type of the error based on the HTTP standard
     */
    private void setError(String type){
        errorType = type;
        error = true;
        requestType = null;
        requestedFile = null;
        bodyContent = null;
        headerContent = null;
    }

    /**
     * For tests and better code organization
     * @param requestedFile the requested file
     * @param fileType the file's type
     */

    public RequestParser(String requestedFile, String fileType) {
        this.requestedFile = requestedFile;
        this.fileType = fileType;
        requestType = null;
        error = false;
        errorType = null;
        bodyContent = null;
        headerContent = null;
    }
}
