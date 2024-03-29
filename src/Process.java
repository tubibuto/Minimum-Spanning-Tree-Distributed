import java.io.*;
import java.lang.*;

public class Process implements MsgHandler {
    int N;
    int myId;
    Linker comm;

    public Process (Linker initComm) {
        comm = initComm;
        myId = comm.getMyId();
        N = comm.getNumProc();
    }

    public synchronized void handleMsg (Msg m, int src, String tag) {}
    public void sendMsg (int destId, String tag, String msg) {
        Util.println("Sending msg to " + destId + ":" + tag + " " + msg);
        comm.sendMsg(destId, tag, msg);
    }
    public void sendMsg (int destId, String tag, int msg) {
        sendMsg(destId, tag, String.valueOf(msg) + " ");
    }
    public void sendMsg (int destId, String tag, int msg1, int msg2) {
        sendMsg(
            destId, 
            tag, 
            String.valueOf(msg1) + " " + String.valueOf(msg2) + " ");
    }
    public void sendMsg (int destId, String tag) {
        sendMsg(destId, tag, " 0 ");
    }
    public void broadcastMsg (String tag, int msg) {
        for (int i = 0; i < N; ++i) {
            if (i == myId) {
                continue;
            }
            sendMsg(i, tag, msg);
        }
    }
    public void sendToNeighbors (String tag, int msg) {
        for (int i = 0; i < N; ++i) {
            if (!isNeighbor(i)) {
                continue;
            }
            sendMsg(i, tag, msg);
        }
    }
    public boolean isNeighbor (int i) {
        return comm.neighbors.contains(i);
    }
    public Msg receiveMsg (int fromId) {
        try {
            return comm.receiveMsg(fromId);
        } catch (IOException e) {
            System.out.println(e);
            comm.close();
            return null;
        }
    }
    public synchronized void myWait () {
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}