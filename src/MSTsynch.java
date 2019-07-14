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
    boolean algorithm_running = false;
    IntLinkedList marked = new IntLinkedList();
    Synchronizer s;
    int examineCounter = 0;
    int examineReplyCounter = 0;
    int needToSend = 0;
    IntLinkedList procDemandingExamine = new IntLinkedList();
    
    public MSTsynch(Linker initComm, int initCost[], Synchronizer initS){
         super(initComm);
         edgeWeight = initCost;
         numRounds = (int)(Math.log(N)/Math.log(2));
         s = initS;
         
         parent = myId;
         leader = myId;
         //don't run algorithm now, one process will start it with synchronizer
    }
    
    
    public void initiate(){
        System.out.println("IN INITIALIZE with id " + myId + " neighbors: ");
        for ( int i = 0; i < N ; i++)
            if (isNeighbor(i)){
                System.out.print(i+ " ");
                examineCounter++;
            }

        //s.initialize(this);
        runAlgorithm();
    }

    void getNumberOfMessagesNeededToSend(){
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            needToSend += id != parent && isNeighbor(id) ? 1 : 0;
        }
        //deficit for "examine"
        for (int i = 0; i < N; ++i) {
            needToSend += isNeighbor(i) && !marked.contains(i) ? 1 : 0;
        }
    }
    
    
    void runAlgorithm() {
        System.out.println("\nRound " + numRounds);
        if (numRounds-- == 0) {
            System.out.println("End");
            done = true;
            notify();
            return;
        }
        getNumberOfMessagesNeededToSend();;
        mwoeSrc = -1;
        mwoeDest = -1;
        //deficit counts the amount of messages we need to receive in one round
        deficit = 0;
        //deficit for "search mwoe"
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            deficit += id != parent && isNeighbor(id)? 1 : 0;
        }
        //deficit for "examine"
        for (int i = 0; i < N; ++i) {
            deficit += isNeighbor(i) && !marked.contains(i) ? 1 : 0;
        }
        System.out.println("Deficit: "+ deficit);
        //leader starts the step
        if (leader != myId) {
            return;
        }
        
        //send messages in broadcast to marked
        searchMwoe();
        //send examination messages to unmarked neighbors
        examine();
        if( needToSend == 0 && deficit == 0)
            replyExamineAndEnterNewPulse();
        //s.nextPulse();
    }
    
    
    //send message to all marked that are not parent
    void searchMwoe() {
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            if (id != parent) {
                s.sendMessage(id, "search_mwoe", leader);
                needToSend--;
            }
        }
    }
    
    //send message to all unmarked that are neighbors
    void examine (){
        for (int i = 0; i < N; ++i) 
            if (isNeighbor(i) && !marked.contains(i)) {
                s.sendMessage(i, "examine", leader);
                examineReplyCounter++;
                needToSend--;
            }

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
                s.sendMessage(
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
                s.sendMessage(i, "new_leader", leader);
        }
    }
    
    
    public synchronized void waitForDone () {
        // block till children know
        while (!done) { 
            myWait();
        }
    }

    void replyExamineAndEnterNewPulse(){
        System.out.println("entering next pulse");
        s.nextPulse();
        for (int i = 0; i < procDemandingExamine.size(); ++i) {
            int id = procDemandingExamine.getEntry(i);
            s.sendMessage(
                    id,
                    "reply_mwoe",
                    String.valueOf(id) + ":" +
                            String.valueOf(myId) + ":" +
                            String.valueOf(edgeWeight[id]));
        }


    }
    
    public synchronized void handleMsg(Msg m, int src, String tag) {
        System.out.println("In handleMsg, tag: " + tag);
        if (tag.equals("search_mwoe")) {
            //send messages in broadcast to marked
            searchMwoe();
            //send examination messages to unmarked neighbors
            examine();
        }else if (tag.equals("examine")) {
            int hisLeader = m.getMessageInt();
            if (hisLeader != leader) {
                //examineCounter--;
                deficit--;
                procDemandingExamine.add(src);
                System.out.println("Deficit: "+ deficit+ " needToSend: " + needToSend);
                if(deficit == 0 && needToSend == 0)
                    replyExamineAndEnterNewPulse();
            }
            //s.nextPulse();
        }else if (tag.equals("reply_mwoe")) {
            System.out.println("in reply mwoe");
            String content = m.getMessage();
            StringTokenizer st = new StringTokenizer(content, ":#");
            int localId = Integer.parseInt(st.nextToken());
            int remoteId = Integer.parseInt(st.nextToken());
            int cost = Integer.parseInt(st.nextToken());
            //lower deficit on each received report
            --deficit;
            examineReplyCounter--;
            
            if (mwoeSrc == -1 || cost < minCost) {
                minCost = cost;
                mwoeSrc = localId;
                mwoeDest = remoteId;
            }
            
            //if deficit == 0 process received all reports and can send
            //his report to his parent
            if (examineReplyCounter == 0)
                //if process is not leader send his report to his parent
                //continue convergecast
                if (myId != leader) {
                    s.sendMessage(
                        parent, 
                        "reply_mwoe", 
                        String.valueOf(mwoeSrc) + ":" + 
                        String.valueOf(mwoeDest) + ":" + 
                        String.valueOf(minCost));
                    s.nextPulse();
                }else{
                    System.out.println("MWOE src: " + mwoeSrc + " dest: " + mwoeDest);
                    addMwoe(mwoeSrc, mwoeDest);
                    s.nextPulse();
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
        }else if (tag.equals("start_algorithm") && !algorithm_running) {
                int srcId = m.getSrcId();
                for (int i = 0; i < N; ++i)
                    //send message to neighbors
                    if (isNeighbor(i)  && i != srcId)
                        s.sendMessage(i, "start_algorithm", 0);
                s.nextPulse();
                algorithm_running = true;
                runAlgorithm();
        }
        //s.nextPulse();
    }
}