import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

public class Proxy {
    Proxy(){
        
    }

    public void start() throws IOException{
        // Set up the sockets and the input and out put streams
        ServerSocket serverSocket = new ServerSocket(55545);
        Socket clientSocket = serverSocket.accept();

        Socket smtpClient = new Socket("localhost", 25);

        //this one communicates with the client
        PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //this one communicates with the server
        PrintWriter smtpOut = new PrintWriter(smtpClient.getOutputStream(), true);
        BufferedReader smtpIn = new BufferedReader(new InputStreamReader(smtpClient.getInputStream()));
        
        System.out.println("Starting");

        String serverGreeting = smtpIn.readLine();
        clientOut.println(serverGreeting);

        String line;
        while ((line = clientIn.readLine()) != null) {
            if (line.equalsIgnoreCase("DATA")) {
                smtpOut.println(line);
                String response = smtpIn.readLine();
                clientOut.println(response);
                handleDataSection(clientIn, clientOut, smtpIn, smtpOut);
            } else {
                smtpOut.println(line);
                String response = smtpIn.readLine();
                clientOut.println(response);
            }
        }

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
        StringBuilder emailBody = new StringBuilder();
        String line;

        while ((line = clientIn.readLine()) != null) {
            if (line.equals(".")) {
                break;
            }
            emailBody.append(line).append("\n");
        }

        String fullEmail = emailBody.toString();

        if (containsIlluminati(fullEmail)) {
            smtpOut.println("Hello world");
            smtpOut.println(".");
        } else {
            String modifiedEmail = replaceWords(fullEmail);
            String[] lines = modifiedEmail.split("\n");
            for (String l : lines) {
                smtpOut.println(l);
            }
            smtpOut.println("Please do not take anything in this email seriously!");
            smtpOut.println(".");
        }

        String response = smtpIn.readLine();
        clientOut.println(response);
    }

    private String replaceWords(String text) {
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

    private String replaceWordBoundary(String text, String oldWord, String newWord) {
        return text.replaceAll("(?i)\\b" + oldWord + "\\b", newWord);
    }

    private boolean containsIlluminati(String text) {
        return text.toLowerCase().contains("illuminati");
    }

}
