public class SpanTreeTester {
    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        Linker comm = new Linker(args[0], myId, numProc);
        SpanTree t = new SpanTree(comm, myId == 0);
        for (int i = 0; i < numProc; ++i) {
            if (i == myId) {
                continue;
            }
            (new ListenerThread(i, t)).start();
        }
        t.waitForDone();
        Util.println(myId + ": " + t.children.toString());
    }
}