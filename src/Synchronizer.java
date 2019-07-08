public interface Synchronizer extends MsgHandler{
    public void initialize(MsgHandler initProg);
    public void sendMessage(int destId, String tag, int msg);
    public void nextPulse(); //block for the next pulse
}