package in.ac.ducic.fileshare;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HttpServerConnection implements Runnable {
    private UriInterpreter theUriInterpreter;
    private Socket mconnectionsocket;
    private UriInterpreter mfileUri;


    public HttpServerConnection(UriInterpreter fileUri, Socket mconnectionsocket) {
        this.mfileUri = fileUri;
        this.mconnectionsocket = mconnectionsocket;

    }


    public void run() {
       InputStream theInputStream;
        try {
            theInputStream = mconnectionsocket.getInputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        OutputStream theOuputStream;
        try {
            theOuputStream = mconnectionsocket.getOutputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(
                theInputStream));

        DataOutputStream output = new DataOutputStream(theOuputStream);
        http_handler(input, output);
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void http_handler(BufferedReader input, DataOutputStream output) {
        String header;
        try {
            header = input.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        String upperCaseHeader = header.toUpperCase();

        if (!upperCaseHeader.startsWith("GET")) {
            dealWithUnsupportedMethod(output);
            return;
        }
        String path = getRequestedFilePath(header);
        if (path == null || path == "") {
            return;
        }
        if (mfileUri == null) {
            return;
        }
        String fileUriStr = mfileUri.getUri().toString();
        try {
            theUriInterpreter = mfileUri;
        } catch (java.lang.SecurityException e) {
            e.printStackTrace();
            return;
        }
        if (path.equals("/")) {
            redirectToFinalPath(output, theUriInterpreter.getName());
            return;
        }
        shareOneFile(output, fileUriStr);
    }

    private void shareOneFile(DataOutputStream output, String fileUriStr) {

        InputStream requestedfile = null;

        if (!theUriInterpreter.isDirectory()) {
            try {
                requestedfile = theUriInterpreter.getInputStream();
            } catch (FileNotFoundException e) {
                try {
                    output.writeBytes(construct_http_header(200, "text/plain"));
                    output.writeBytes(fileUriStr);
                    return;
                } catch (IOException e2) {
                    return;
                }
            }
        }


        String outputString = construct_http_header(200, theUriInterpreter.getMime());

        try {
            output.writeBytes(outputString);
            byte[] buffer = new byte[4096];
            for (int n; (n = requestedfile.read(buffer)) != -1; ) {
                output.write(buffer, 0, n);
            }
            requestedfile.close();
        } catch (IOException e) {
        }
    }

    private void redirectToFinalPath(DataOutputStream output, String thePath) {

        String redirectOutput = construct_http_header(302, null, thePath);
        try {
            output.writeBytes(redirectOutput);
        } catch (IOException e2) {
        }
    }

    private String getRequestedFilePath(String inputHeader) {
        String path;
        String tmp2 = new String(inputHeader);

        int start = 0;
        int end = 0;
        for (int a = 0; a < tmp2.length(); a++) {
            if (tmp2.charAt(a) == ' ' && start != 0) {
                end = a;
                break;
            }
            if (tmp2.charAt(a) == ' ' && start == 0) {
                start = a;
            }
        }
        path = tmp2.substring(start + 1, end);

        return path;
    }

    private void dealWithUnsupportedMethod(DataOutputStream output) {
        try {
            output.writeBytes(construct_http_header(501, null));
            return;
        } catch (Exception e3) {
            e3.getMessage();
        }
    }


    private static String httpReturnCodeToString(int return_code) {
        switch (return_code) {
            case 200:
                return "200 OK";
            case 302:
                return "302 Moved Temporarily";
            case 400:
                return "400 Bad Request";
            case 403:
                return "403 Forbidden";
            case 404:
                return "404 Not Found";
            case 500:
                return "500 Internal Server Error";
            case 501:
            default:
                return "501 Not Implemented";
        }
    }

    private String construct_http_header(int return_code, String mime) {
        return construct_http_header(return_code, mime, null);
    }


    private String getFileSizeHeader() {
        if (theUriInterpreter == null) {
            return "";
        }
        if ( theUriInterpreter.getSize() > 0) {
            return "Content-Length: "
                    + Long.toString(theUriInterpreter.getSize()) + "\r\n";
        }
        return "";
    }

    private String construct_http_header(int return_code, String mime,
                                         String location) {

        StringBuilder output = new StringBuilder();
        output.append("HTTP/1.1 ");
        output.append(httpReturnCodeToString(return_code) + "\r\n");
        output.append(getFileSizeHeader());
        SimpleDateFormat format = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzz");
        format.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        output.append("Date: " + format.format(new Date()) + "\r\n");

        output.append("Connection: close\r\n"); // we can't handle persistent

        output.append("Server: ShareFile  1.0.0"
                + "\r\n");
        if (location != null) {
            try {
                location = URLEncoder.encode(location, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                Log.getStackTraceString(e);
            }
            output.append("Location: " + location + "\r\n");
            output.append("Expires: Tue, 03 Jul 2001 06:00:00 GMT\r\n");
            output.append("Cache-Control: no-store, no-cache, must-revalidate, max-age=0\r\n");
            output.append("Cache-Control: post-check=0, pre-check=0\r\n");
            output.append("Pragma: no-cache\r\n");
        }
        if (mime != null) {
            output.append("Content-Type: " + mime + "\r\n");
        }
        output.append("\r\n");
        return output.toString();
    }

}