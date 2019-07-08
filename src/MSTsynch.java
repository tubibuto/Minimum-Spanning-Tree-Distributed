import java.util.*;

public class MSTsynch extends Process{
    public int parent;
    int numRounds;
    int leader;
    int deficit;
    int minCost;
    int mwoeSrc;
    int mwoeDest;
    int edgeWeight[] = null;
    boolean done = false;
    IntLinkedList marked = new IntLinkedList();
    Synchronizer s;
    
    public MSTsynch(Linker initComm, int initCost[], Synchronizer initS){
         super(initComm);
         edgeWeight = initCost;
         //numRounds = (int)(Math.log(N) / Math.log(2));
         numRounds = (int)Math.log(N);
         s = initS;
         
         parent = myId;
         leader = myId;
         //don't run algorithm now, one process will start it with synchronizer
    }
    
    
    public void initiate(){
        s.initialize(this);
        for( int pulse = 0; ; pulse++){
            if (pulse == 0){
                for ( int i = 0; i < N ; i++)
                    if (isNeighbor(i))
                        s.sendMessage(i, "start_algorithm", 0);
            }
            runAlgorithm();
        }
    }
    
    
    void runAlgorithm() {
        System.out.println("Round " + numRounds);
        if (numRounds-- == 0) {
            System.out.println("End");
            done = true;
            notify();
            return;
        }
        mwoeSrc = -1;
        mwoeDest = -1;
        //deficit counts the amount of messages we need to receive in one round
        deficit = 0;
        //deficit for "search mwoe"
        for (int i = 0; i < marked.size(); ++i) {
            deficit += marked.getEntry(i) != parent ? 1 : 0;
        }
        //deficit for "examine"
        for (int i = 0; i < N; ++i) {
            deficit += isNeighbor(i) && !marked.contains(i) ? 1 : 0;
        }
        //leader starts the step
        if (leader != myId) {
            return;
        }
        
        //send messages in broadcast to marked
        searchMwoe();
        //send examination messages to unmarked neighbors
        examine();
    }
    
    
    //send message to all marked that are not parent
    void searchMwoe() {
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            if (id != parent) 
                s.sendMsg(id, "search_mwoe", leader);
        }
    }
    
    //send message to all unmarked that are neighbors
    void examine (){
        for (int i = 0; i < N; ++i) 
            if (isNeighbor(i) && !marked.contains(i)) 
                s.sendMsg(i, "examine", leader);
    }
    
    
    void addMwoe (int localId, int remoteId) {
        //if proces is localId then add remote to MST
       if (myId == localId) 
            marked.add(remoteId);

        //if process is remoteId, check if you are new leader
       if (myId == remoteId && myId == Util.max(localId, remoteId)){
           //remoteId has recived addMwoe message, it can send newLeader message in the same pulse
           System.out.println("marked size: " + marked.size());
           newLeader(myId, myId);
           return;
       }
       //if proces is not localId then broadcast "add_mwoe"
       for (int i = 0; i < N; ++i) {
           //send message to all children
            if (isNeighbor(i) && marked.contains(i) && i != parent)
                s.sendMsg(
                    i, 
                    "add_mwoe", 
                    String.valueOf(localId) + ":" + String.valueOf(remoteId));
        }
                   //check if you are new leader
        if(myId == localId && myId == Util.max(localId, remoteId))
            //if you are max start broadcast that you are new leader
            //since you already sent add_mwoe, wait until next pulse
            System.out.println("marked size: " + marked.size());
            s.nextPulse();
            newLeader(myId, myId);
    }
    
    
    void newLeader (int src, int leader) {
        this.leader = leader;
        parent = src;
        
        for (int i = 0; i < N; ++i) {
           //send message to all children
            if (isNeighbor(i) && marked.contains(i) && i != parent)
                s.sendMsg(i, "new_leader", leader);          
        }
    }
    
    
    public synchronized void waitForDone () {
        // block till children know
        while (!done) { 
            myWait();
        }
    }
    
    
    public synchronized void handleMsg(Msg m, int src, String tag) {
        if (tag.equals("search_mwoe")) {
            //send messages in broadcast to marked
            searchMwoe();
            //send examination messages to unmarked neighbors
            examine();
        }else if (tag.equals("examine")) {
            int hisLeader = m.getMessageInt();
            if (hisLeader != leader) {
                s.sendMsg(
                    src, 
                    "reply_mwoe", 
                    String.valueOf(src) + ":" + 
                        String.valueOf(myId) + ":" + 
                        String.valueOf(edgeWeight[src]));
            }
        }else if (tag.equals("reply_mwoe")) {
            String content = m.getMessage();
            StringTokenizer st = new StringTokenizer(content, ":#");
            int localId = Integer.parseInt(st.nextToken());
            int remoteId = Integer.parseInt(st.nextToken());
            int cost = Integer.parseInt(st.nextToken());
            //lower deficit on each received report
            --deficit;    
            
            if (mwoeSrc == -1 || cost < minCost) {
                minCost = cost;
                mwoeSrc = localId;
                mwoeDest = remoteId;
            }
            
            //if deficit == 0 process received all reports and can send
            //his report to his parent
            if (deficit == 0)
                //if process is not leader send his report to his parent
                //continue convergecast
                if (myId != leader) {
                    s.sendMsg(
                        parent, 
                        "reply_mwoe", 
                        String.valueOf(mwoeSrc) + ":" + 
                        String.valueOf(mwoeDest) + ":" + 
                        String.valueOf(minCost));
                }else{
                    System.out.println("MWOE src: " + mwoeSrc + " dest: " + mwoeDest);
                    addMwoe(mwoeSrc, mwoeDest); 
                } 
        }else if (tag.equals("add_mwoe")) {
            //continue broadcast
            String content = m.getMessage();
            StringTokenizer st = new StringTokenizer(content, ":#");
            int localId = Integer.parseInt(st.nextToken());
            int remoteId = Integer.parseInt(st.nextToken());
            addMwoe(localId, remoteId);
        }else if (tag.equals("new_leader")) {
            //continue broadcast
            int hisLeader = m.getMessageInt();
            newLeader(src, hisLeader);
            
            //after proces finishes sending new_leader to marked neighbors, 
            //wait until next pulse to start next round of algorithm
            s.nextPulse();
            runAlgorithm();
            return;
        }
        s.nextPulse();
    }
}