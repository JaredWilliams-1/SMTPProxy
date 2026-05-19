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

        Socket smtpClient = new Socket("localhost", 1025); //imma use 1025 coz 25 is in use

        //this one communicates with the client
        PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        //this one communicates with the server
        PrintWriter smtpOut = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader smtpIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        System.out.println("Starting");

        String serverGreeting = smtpIn.readLine();
        clientOut.println(serverGreeting + "\n");

        String line;
        while ((line =clientIn.readLine()) != null) {
            clientOut.println("PROXY IS ACTIVE");

            break;

        }
            // Loop while the client wants to continue (if the client does not say quit)
            // Receive client input
                // if it is part of the login stuff then login to the server on behalf of the client
                // otherwise perform replacements
            
            // Perform checks 
        
        
        smtpIn.close();
        smtpOut.close();
        clientIn.close();
        clientOut.close();
        smtpClient.close();
        serverSocket.close();
        
    }
}
