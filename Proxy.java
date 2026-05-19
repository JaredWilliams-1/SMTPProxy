import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Base64;

public class Proxy {
    Proxy(){
        
    }

    public void start() throws IOException{
        // Set up the sockets and the input and out put streams
        ServerSocket serverSocket = new ServerSocket(55545);
        System.out.println("[PROXY] Listening on port 55545...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("[PROXY] Client connected!");

        Socket smtpClient = new Socket("localhost", 25);
        System.out.println("[PROXY] Connected to SMTP server on port 25");

        //this one communicates with the client
        PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //this one communicates with the server
        PrintWriter smtpOut = new PrintWriter(smtpClient.getOutputStream(), true);
        BufferedReader smtpIn = new BufferedReader(new InputStreamReader(smtpClient.getInputStream()));

        System.out.println("[PROXY] Starting");

        String serverGreeting = smtpIn.readLine();
        System.out.println("[SERVER] " + serverGreeting);
        clientOut.println(serverGreeting);

        String line;
        while ((line = clientIn.readLine()) != null) {
            System.out.println("[CLIENT] " + line);
            if (line.equalsIgnoreCase("DATA")) {
                System.out.println("[PROXY] Detected DATA command, entering special handling");
                smtpOut.println(line);
                String response = smtpIn.readLine();
                System.out.println("[SERVER] " + response);
                clientOut.println(response);
                handleDataSection(clientIn, clientOut, smtpIn, smtpOut);
            } else {
                smtpOut.println(line);
                String response = smtpIn.readLine();
                System.out.println("[SERVER] " + response);
                clientOut.println(response);
            }
        }
        System.out.println("[PROXY] Client disconnected");

        smtpIn.close();
        smtpOut.close();
        clientIn.close();
        clientOut.close();
        smtpClient.close();
        serverSocket.close();
        
    }

    private void relayLinesUntil(BufferedReader input, PrintWriter output, String terminator) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            output.println(line);
            if (line.equals(terminator)) {
                break;
            }
        }
    }

    private void handleDataSection(BufferedReader clientIn, PrintWriter clientOut, BufferedReader smtpIn, PrintWriter smtpOut) throws IOException {
        System.out.println("[PROXY] Reading email headers...");
        StringBuilder headers = new StringBuilder();
        StringBuilder emailBody = new StringBuilder();
        String line;
        boolean isBase64 = false;

        while ((line = clientIn.readLine()) != null) {
            if (line.isEmpty()) {
                headers.append(line).append("\n");
                System.out.println("[PROXY] End of headers reached");
                break;
            }
            System.out.println("[PROXY] Header: " + line);
            headers.append(line).append("\n");
            if (line.toLowerCase().contains("content-transfer-encoding: base64")) {
                isBase64 = true;
                System.out.println("[PROXY] Detected base64 encoding");
            }
        }

        System.out.println("[PROXY] Reading email body...");
        while ((line = clientIn.readLine()) != null) {
            if (line.equals(".")) {
                System.out.println("[PROXY] End of message reached");
                break;
            }
            emailBody.append(line).append("\n");
        }

        String headersStr = headers.toString();
        String bodyStr = emailBody.toString();
        System.out.println("[PROXY] Body length: " + bodyStr.length() + " chars");

        String decodedBody = isBase64 ? decodeBase64(bodyStr) : bodyStr;

        if (containsIlluminati(decodedBody)) {
            System.out.println("[PROXY] Illuminati detected! Replacing with 'Hello world'");
            smtpOut.print(headersStr);
            smtpOut.println("Hello world");
            smtpOut.println(".");
        } else {
            System.out.println("[PROXY] Applying word replacements...");
            String modifiedBody = replaceWords(decodedBody);
            smtpOut.print(headersStr);
            String finalBody = isBase64 ? encodeBase64(modifiedBody) : modifiedBody;
            smtpOut.print(finalBody);
            System.out.println("[PROXY] Adding disclaimer and sending to server...");
            smtpOut.println("Please do not take anything in this email seriously!");
            smtpOut.println(".");
        }

        String response = smtpIn.readLine();
        System.out.println("[SERVER] " + response);
        clientOut.println(response);
    }

    private String decodeBase64(String encoded) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded.replaceAll("\n", "").replaceAll("\r", ""));
            return new String(decoded);
        } catch (IllegalArgumentException e) {
            return encoded;
        }
    }

    private String encodeBase64(String text) {
        byte[] encoded = Base64.getEncoder().encode(text.getBytes());
        String result = new String(encoded);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length(); i += 76) {
            sb.append(result, i, Math.min(i + 76, result.length()));
            if (i + 76 < result.length()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String replaceWords(String text) {
        text = replacePhrase(text, "very good", "plusgood");
        text = replacePhrase(text, "very fast", "plusfast");
        text = replacePhrase(text, "very bad", "plusungood");

        text = replaceWordBoundary(text, "warm", "uncold");
        text = replaceWordBoundary(text, "bad", "ungood");
        text = replaceWordBoundary(text, "fast", "speedful");
        text = replaceWordBoundary(text, "rapid", "speedful");
        text = replaceWordBoundary(text, "quick", "speedful");
        text = replaceWordBoundary(text, "slow", "unspeedful");
        text = replaceWordBoundary(text, "ran", "runned");
        text = replaceWordBoundary(text, "stole", "stealed");
        text = replaceWordBoundary(text, "best", "goodest");
        text = replaceWordBoundary(text, "better", "gooder");
        return text;
    }

    private String replacePhrase(String text, String oldPhrase, String newPhrase) {
        return text.replaceAll("(?i)" + oldPhrase, newPhrase);
    }

    private String replaceWordBoundary(String text, String oldWord, String newWord) {
        return text.replaceAll("(?i)\\b" + oldWord + "\\b", newWord);
    }

    private boolean containsIlluminati(String text) {
        return text.toLowerCase().contains("illuminati");
    }

}
