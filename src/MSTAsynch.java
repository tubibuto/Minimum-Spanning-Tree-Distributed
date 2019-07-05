import java.util.*;

public class MSTAsynch extends Process {
    public int parent;
    int edgeWeight[] = null;
    int leader;
    IntLinkedList marked = new IntLinkedList();
    int numRounds;
    int deficit;
    int minCost;
    int mwoeSrc;
    int mwoeDest;
    boolean done = false;

    public MSTAsynch (Linker initComm, int initCost[]) {
        super(initComm);
        edgeWeight = initCost;
        parent = myId;
        leader = myId;
        numRounds = (int)(Math.log(N) / Math.log(2));
        runAlgorithm();
    }

    void runAlgorithm () {
        System.out.println("Round " + numRounds);
        if (numRounds-- == 0) {
            System.out.println("End");
            done = true;
            notify();
            return;
        }
        deficit = 0;
        for (int i = 0; i < marked.size(); ++i) {
            deficit += marked.getEntry(i) != parent ? 1 : 0;
        }
        for (int i = 0; i < N; ++i) {
            deficit += isNeighbor(i) && !marked.contains(i) ? 1 : 0;
        }
        mwoeSrc = -1;
        mwoeDest = -1;
        if (leader != myId) {
            return;
        }
        searchMwoe();
        examine();
    }

    void searchMwoe () {
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            if (parent == id) {
                continue;
            }
            sendMsg(id, "search_mwoe", leader);
        }
    }

    void examine () {
        for (int i = 0; i < N; ++i) {
            if (!isNeighbor(i) || marked.contains(i)) {
                continue;
            }
            sendMsg(i, "examine", leader);
        }
    }

    void replyMwoe (Msg m) {
        String content = m.getMessage();
        StringTokenizer st = new StringTokenizer(content, ":#");
        int localId = Integer.parseInt(st.nextToken());
        int remoteId = Integer.parseInt(st.nextToken());
        int cost = Integer.parseInt(st.nextToken());

        if (mwoeSrc == -1 || cost < minCost) {
            minCost = cost;
            mwoeSrc = localId;
            mwoeDest = remoteId;
        }
        if (--deficit == 0 && myId != leader) {
            sendMsg(
                parent, 
                "reply_mwoe", 
                String.valueOf(mwoeSrc) + ":" + 
                    String.valueOf(mwoeDest) + ":" + 
                    String.valueOf(minCost));
        }
        if (deficit == 0) {
            addMwoe(mwoeSrc, mwoeDest);
        }
    }

    void addMwoe (int localId, int remoteId) {
        if (localId == myId) {
            marked.add(remoteId);
        }
        if (remoteId == myId) {
            marked.add(localId);
            leader = Util.max(localId, remoteId);
            newLeader(leader, leader);
            return;
        }
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            if (id == parent) {
                continue;
            }
            sendMsg(
                id, 
                "add_mwoe", 
                String.valueOf(localId) + ":" + String.valueOf(remoteId));
        }
    }

    void newLeader (int src, int leader) {
        this.leader = leader;
        parent = src;
        for (int i = 0; i < marked.size(); ++i) {
            int id = marked.getEntry(i);
            if (id == parent) {
                continue;
            }
            sendMsg(id, "new_leader", leader);
        }
    }

    public synchronized void waitForDone () {
        // block till children know
        while (!done) {
            myWait();
        }
    }
    public synchronized void handleMsg (Msg m, int src, String tag) {
        if (tag.equals("search_mwoe")) {
            searchMwoe();
            examine();
        } else if (tag.equals("examine")) {
            int hisLeader = m.getMessageInt();
            if (hisLeader != leader) {
                sendMsg(
                    src, 
                    "reply_mwoe", 
                    String.valueOf(src) + ":" + 
                        String.valueOf(myId) + ":" + 
                        String.valueOf(edgeWeight[src]));
            }
        } else if (tag.equals("reply_mwoe")) {
            replyMwoe(m);
        } else if (tag.equals("add_mwoe")) {
            String content = m.getMessage();
            StringTokenizer st = new StringTokenizer(content, ":#");
            int localId = Integer.parseInt(st.nextToken());
            int remoteId = Integer.parseInt(st.nextToken());
            addMwoe(localId, remoteId);
        } else if (tag.equals("new_leader")) {
            int hisLeader = m.getMessageInt();
            newLeader(src, hisLeader);
            runAlgorithm();
        }
    }
}