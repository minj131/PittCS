import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** ********************************************************
 *  PQ Implementation mostly using book author's code
 *  and modifications made specific for this project
 **********************************************************/

@SuppressWarnings("unchecked")
public class PQ<Key> implements Iterable<Integer> {

    private int n;           // maximum number of elements on PQ
    private int count;       // number of elements on PQ
    private int[] pq;        // binary heap using 1-based indexing
    private int[] qp;        // inverse of pq - qp[pq[i]] = pq[qp[i]] = i
    private Key[] keys;      // keys[i] = priority of i
    private Comparator<Key> comp;

    public PQ(int n, Comparator<Key> comp) {
        if(n < 0) throw new IllegalArgumentException();
        this.n = n;
        this.comp = comp;
        count = 0;

        keys = (Key[]) new Object[n+1];
        pq = new int[n+1];
        qp = new int[n+1];
        for (int i=0; i<=n; i++)
            qp[i] = -1;
    }

    public boolean isEmpty() {return count == 0;}

    public boolean contains(int i) {
        if (i < 0 || i >= n) throw new IllegalArgumentException();
        return qp[i] != -1;
    }

    public int size() {return count;}

    public void insert(int i, Key key) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (contains(i)) throw new IllegalArgumentException("index is already in the priority queue");
        count++;
        qp[i] = count;
        pq[count] = i;
        keys[i] = key;
        swim(count);
    }

    public int minIndex() {
        if (count == 0) throw new NoSuchElementException("Priority queue underflow");
        return pq[1];
    }

    public Key minKey() {
        if (count == 0) throw new NoSuchElementException("Priority queue underflow");
        return keys[pq[1]];
    }

    public int delMin() {
        if (count == 0) throw new NoSuchElementException("Priority queue underflow");
        int min = pq[1];
        exch(1, count--);
        sink(1);
        assert min == pq[count+1];
        qp[min] = -1;            // delete
        keys[min] = null;        // to help with garbage collection
        pq[count+1] = -1;        // not needed
        return min;
    }

    public Key keyOf(int i) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        else return keys[i];
    }

    public void changeKey(int i, Key key) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        keys[i] = key;
        swim(qp[i]);
        sink(qp[i]);
    }

    public void change(int i, Key key) {changeKey(i, key);}

    public void decreaseKey(int i, Key key) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        if (comp.compare(keys[i], key) <= 0)
            throw new IllegalArgumentException("Calling decreaseKey() with given argument would not strictly decrease the key");
        keys[i] = key;
        swim(qp[i]);
    }

    public void increaseKey(int i, Key key) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        if (comp.compare(keys[i], key) >= 0)
            throw new IllegalArgumentException("Calling increaseKey() with given argument would not strictly increase the key");
        keys[i] = key;
        sink(qp[i]);
    }

    public void delete(int i) {
        if (i < 0 || i >= n) throw new IndexOutOfBoundsException();
        if (!contains(i)) throw new NoSuchElementException("index is not in the priority queue");
        int index = qp[i];
        exch(index, count--);
        swim(index);
        sink(index);
        keys[i] = null;
        qp[i] = -1;
    }

    /***************************************************************************
     * General helper functions.
     ***************************************************************************/
    private boolean greater(int i, int j) {
        if (comp == null){
            return ((Comparable<Key>) keys[pq[i]]).compareTo(keys[pq[j]]) > 0;
        } else {
            return comp.compare(keys[pq[i]], keys[pq[j]]) > 0;
        }
    }

    private void exch(int i, int j) {
        int swap = pq[i];
        pq[i] = pq[j];
        pq[j] = swap;
        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }


    /***************************************************************************
     * Heap helper functions.
     ***************************************************************************/
    private void swim(int k) {
        while (k > 1 && greater(k/2, k)) {
            exch(k, k/2);
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= count) {
            int j = 2*k;
            if (j < count && greater(j, j+1)) j++;
            if (!greater(k, j)) break;
            exch(k, j);
            k = j;
        }
    }

    /***************************************************************************
     * Iterators.
     ***************************************************************************/
    public Iterator<Integer> iterator() { return new HeapIterator(); }

    private class HeapIterator implements Iterator<Integer> {
        // create a new pq
        private PQ<Key> copy;

        // add all elements to copy of heap
        // takes linear time since already in heap order so no keys move
        public HeapIterator() {
            copy = new PQ<>(pq.length - 1, comp);
            for (int i=1; i <= count; i++)
                copy.insert(pq[i], keys[pq[i]]);
        }

        public boolean hasNext()  { return !copy.isEmpty();                     }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Integer next() {
            if (!hasNext()) throw new NoSuchElementException();
            return copy.delMin();
        }
    }
}
