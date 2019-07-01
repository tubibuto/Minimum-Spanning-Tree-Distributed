import java.lang.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Name {
    BufferedReader din;
    PrintStream pout;

    public void getSocket () throws IOException {
        Socket server = new Socket(Symbols.nameServer, Symbols.ServerPort);
        din = 
            new BufferedReader(new InputStreamReader(server.getInputStream()));
        pout = new PrintStream(server.getOutputStream());
    }
    public int insertName (
        String name, String hname, int portnum) throws IOException {
        getSocket();
        pout.println("insert " + name + " " + hname + " " + portnum);
        pout.flush();
        return Integer.parseInt(din.readLine());
    }
    public PortAddr searchName (String name) throws IOException {
        getSocket();
        pout.println("search " + name);
        pout.flush();
        String res = din.readLine();
        StringTokenizer st = new StringTokenizer(res);
        int portnum = Integer.parseInt(st.nextToken());
        String hname = st.nextToken();
        return new PortAddr(hname, portnum);
    }

    public static void main (String[] args) {
        Name myClient = new Name();
        try {
            myClient.insertName("hello1", "student.math.hr", 1000);
            PortAddr pa = myClient.searchName("hello1");
            System.out.println(pa.getHostName() + ":" + pa.getPort());
        } catch (Exception e) {
            System.err.println("Client aborted: " + e);
        }
    }
}