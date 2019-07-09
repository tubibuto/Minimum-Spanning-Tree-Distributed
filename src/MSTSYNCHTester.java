import java.io.*;
import java.util.*;

public class MSTSYNCHTester {
    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(args[0], myId, numProc);
        int initCost[] = new int[numProc];
        try {
            BufferedReader dIn = new BufferedReader(new FileReader("../data/costs.txt"));
            String getline = dIn.readLine();
            int idx = 0;
            while (getline != null) {
                if (idx++ == myId) {
                    StringTokenizer st = new StringTokenizer(getline);
                    idx = 0;
                    while (idx < numProc && st.hasMoreTokens()) {
                        int cost = Integer.parseInt(st.nextToken());
                        initCost[idx++] = cost;
                    }
                    break;
                }
                getline = dIn.readLine();
            }
        } catch (FileNotFoundException e) {
            for (int i = 0; i < numProc; ++i) {
                initCost[i] = i + 1;
            }
        }
        SimpleSynch pulser = new SimpleSynch(comm);
        MSTsynch app = new MSTsynch(comm, initCost, pulser);
        for (int i = 0; i < numProc; ++i) 
            if (i != myId)
               (new ListenerThread(i, pulser)).start();
        app.initiate();       
    }
}