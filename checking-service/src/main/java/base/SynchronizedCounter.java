package base;

class SynchronizedCounter {

    private int count = 0;

    public int getCount() {
        return count;
    }

    public synchronized int increment() {
        count = count + 1;
        return count;
    }
}