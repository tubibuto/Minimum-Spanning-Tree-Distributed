import java.util.*;

public class NameTable {
    final int maxSize = 100;
    private String[] names = new String[maxSize];
    private String[] hosts = new String[maxSize];
    private int[] ports = new int[maxSize];
    private int dirsize = 0;

    int search (String s) {
        for (int i = 0; i < dirsize; ++i) {
            if (names[i].equals(s)) {
                return i;
            }
        }
        return -1;
    }
    int insert (String s, String hostName, int portNumber) {
        int oldIdx = search(s);
        if (oldIdx == -1 && dirsize < maxSize) {
            names[dirsize] = s;
            hosts[dirsize] = hostName;
            ports[dirsize] = portNumber;
            ++dirsize;
            return 1;
        }
        return 0;   
    }
    int getPort (int idx) {
        return ports[idx];
    }
    String getHostName (int idx) {
        return hosts[idx];
    }
}