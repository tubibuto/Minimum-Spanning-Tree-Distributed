import java.util.*;

public class Msg {
    int srcId;
    int destId;
    String tag;
    String msgBuf;

    public Msg (int s, int t, String msgType, String buf) {
        srcId = s;
        destId = t;
        tag = msgType;
        msgBuf = buf;
    }

    public int getSrcId () {
        return srcId;
    }
    public int getDestId () {
        return destId;
    }
    public String getTag () {
        return tag;
    }
    public String getMessage () {
        return msgBuf;
    }
    
    public int getMessageInt () {
        StringTokenizer st = new StringTokenizer(msgBuf);
        return Integer.parseInt(st.nextToken());
    }
    public static Msg parseMsg (StringTokenizer st) {
        int srcId = Integer.parseInt(st.nextToken());
        int destId = Integer.parseInt(st.nextToken());
        String tag = st.nextToken();
        String msgBuf = st.nextToken();
        return new Msg(srcId, destId, tag, msgBuf);
    }
    public String toString () {
        String s = 
            String.valueOf(srcId) + " " + 
            String.valueOf(destId) + " " + 
            tag + " " + 
            msgBuf + "#";
        return s;
    }
}