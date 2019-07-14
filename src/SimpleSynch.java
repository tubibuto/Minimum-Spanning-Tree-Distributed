import java.util.LinkedList;

public class SimpleSynch extends Process implements Synchronizer {
    int pulse = 0;
    MsgHandler prog;
    boolean rcvEnabled [];
    IntLinkedList pendingS = new IntLinkedList();
    IntLinkedList pendingR = new IntLinkedList();

    public SimpleSynch(Linker initComm){
       super(initComm);
       rcvEnabled = new boolean[N];
       for (int i = 0; i < N; i++)
           rcvEnabled[i] = true;
       //rcvEnabled[i] = false;     //before
    }
    
    public synchronized void initialize(MsgHandler initProg){
        prog = initProg;
        pendingS.addAll(comm.neighbors);
        System.out.println("SimpleSynch: initialize with "+ initProg + " PendingS size: "+ pendingS.size());
        notifyAll();
    }
    
    public synchronized void handleMsg(Msg m, int src, String tag){
        System.out.println("handleMsg in SimpleSynch, tag: "+ tag );
        System.out.println("receive anabled [" + src + "] = "+rcvEnabled[src]);
        while (!rcvEnabled[src]) myWait();
        pendingR.removeObject(src);
        if (pendingR.isEmpty()) notifyAll();
        boolean isSynchNull = tag.equals("synchNull");
        if (!isSynchNull) {
            //System.out.print(", calling prog.handleMsg");
            prog.handleMsg(m, src, tag);
        }
        //System.out.println("before the end");
        rcvEnabled[src] = false;
    }
    
    public synchronized void sendMessage(int destId, String tag, int msg){
        if (pendingS.contains(destId)) {
            pendingS.removeObject(destId);
            sendMsg(destId, tag, msg);
        } else
            System.err.println("Error: sending two messages/pulse");
    }

    public synchronized void sendMessage(int destId, String tag, String msg){
        if (pendingS.contains(destId)) {
            pendingS.removeObject(destId);
            sendMsg(destId, tag, msg);
        } else
            System.err.println("Error: sending two messages/pulse");
    }

    public synchronized void nextPulse(){
        //first send all the messages you need
        while (!pendingS.isEmpty()) {
            int dest = pendingS.removeHead();
            sendMsg(dest, "synchNull", 0);
        }
        System.out.println(pendingR);
        //then receive all the messages you need
        while (!pendingR.isEmpty()) myWait();
        pulse++;
        Util.println("**** new pulse ****:" + pulse);
        pendingS.addAll(comm.neighbors);
        pendingR.addAll(comm.neighbors);
        for (int i = 0; i < N; i++)
            rcvEnabled[i] = true;
        notifyAll();
        System.out.print("rcvEnabled: ");
        for (int i = 0; i < N; i++)
            System.out.print(rcvEnabled[i]+ " ");
        System.out.print("\n");
        //System.out.println(pendingR);
        //while (!pendingR.isEmpty()) myWait();
    }
}