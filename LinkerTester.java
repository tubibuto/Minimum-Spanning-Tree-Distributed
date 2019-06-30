public class LinkerTester {
    public static void main (String[] args) {
        Linker comm = null;
        Msg m;
        try {
            String baseName = args[0];
            int myId = Integer.parseInt(args[1]);
            int numProc = Integer.parseInt(args[2]);
            comm = new Linker(baseName, myId, numProc);
            
            for (int i = 0; i < numProc; ++i) {
                if (i == myId) {
                    continue;
                }
                comm.sendMsg(i, "my_tag", "poruka_od_" + myId);
            }

            for (int i = 0; i < numProc; ++i) {
                if (i == myId) {
                    continue;
                }
                m = comm.receiveMsg(i);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}