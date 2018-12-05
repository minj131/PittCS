import java.util.Random;

public class MultiDS<T> implements PrimQ<T>, Reorder {

    private T[] arr;
    private T[] temp;
    private int count;
    private int size;

    public MultiDS(int size) {
        arr = (T[]) new Object[size]; //type safe danger zone
        this.size = size;
    }

    public boolean addItem(T item) {
        if (!full()) {
            arr[count] = item;
            count++;
            return true;
        }   else return false;
    }

    public T removeItem() {
        if (!empty()) {
            count--;
            T temp = arr[0];
            arr[0] = null;
            shiftLeft();
            return temp;
        }   else return null;
    }

    public boolean full() {
        if (count == size) return true;
        else return false;
    }

    public boolean empty() {
        if (count == 0) return true;
        else return false;
    }

    public int size() {return count;}

    public void clear() {
        for (int i = 0; i < count; i++) arr[i] = null;
        count = 0;
    }

    public void reverse() {
        for(int i = 0; i < count/2; i++) {
            T temp = arr[i];
            arr[i] = arr[count - (i + 1)];
            arr[count - (i + 1)] = temp;
        }
    }

    public void shiftRight() {
        T shifted = arr[count - 1];
        for (int i = count-1; i > 0; i--) arr[i] = arr[i-1];
        arr[0] = shifted;
    }

    public void shiftLeft() {
        T shifted = arr[0];
        for (int i = 0; i < count; i++) {
            arr[i] = arr[i + 1];
        }   arr[count] = shifted;
        //this loop takes care of null values produced by removing items from non-resizable arrays
        for (int i = 0; i < count; i++) {
            if (arr[i] == null) {
                arr[i] = arr[i+1];
                arr[i+1] = null;
            }
        }
    }

    public void shuffle() {
        Random rng = new Random();
        for (int i = count - 1; i > 0; i--) {
            int r = rng.nextInt(i+1);
            T temp = arr[r];
            arr[r] = arr[i];
            arr[i] = temp;
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Contents: \n");
        for (int i = 0; i < count; i++) s.append(arr[i] + " ");
        return s.toString();
    }
}
