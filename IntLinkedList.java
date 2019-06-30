import java.util.LinkedList;

public class IntLinkedList extends LinkedList {
    public void add (int i) {
        super.add(new Integer(i));
    }
    public boolean contains (int i) {
        return super.contains(new Integer(i));
    }
    public int removeHead () {
        Integer i = (Integer)super.removeFirst();
        return i.intValue();
    }
    public boolean removeObject (int i) {
        return super.remove(new Integer(i));
    }
    public int getEntry (int idx) {
        Integer i = (Integer)super.get(idx);
        return i.intValue();
    }
}