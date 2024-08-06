// Description of the Program
/*
This Java program implements a solution to the Reader-Writers problem using semaphores for synchronization. It allows multiple read threads to concurrently access shared data while writer threads at the same time acquire exclusive access to data. The program uses two semaphores writeSemaphore and readerSemaphore for controlling access to the shared counter. The Reader threads acquire a reader semaphore before reading and release it after that on the readers end, this allows multiple readers to read simultaneously. Io thread captures the writeSemaphore lock for the counter operation, and as a result, it lets other threads call it again via the releaseSemaphore. The program also implements a CountDownLatch to be set after the writer thread has finished. It guarantees that all the readers will only start after the writer is done. Its implementation seeks to obviate the deadlocks and guarantee fairness between the threads, showing a semaphore synchronization in a multi-threaded environment.
*/


import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class SharedCounter {
    private final Semaphore writeSemaphore = new Semaphore(1);
    private final Semaphore readerSemaphore = new Semaphore(0); // Start with 0 permits
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int numReaders;
    private final CountDownLatch latch;

    public SharedCounter(int numReaders) {
        this.numReaders = numReaders;
        this.latch = new CountDownLatch(1); // Latch for writer completion
    }

    public void read(int readerId) throws InterruptedException {
    readerSemaphore.acquire(); // Wait for writer to signal
    for (int i = 0; i < 2000000; i++) { // Each reader accesses the counter 2,000,000 times
        // Simulate reading the counter
        int currentValue = counter.get();
    }
    System.out.println("I'm reader" + readerId + ", counter = " + counter.get());
    readerSemaphore.release(); // Allow other readers to start reading
}


    public void write() throws InterruptedException {
       writeSemaphore.acquire(); // Acquire write lock
        for (int i = 0; i < 25000; i++) { // Increment the counter 25,000 times
            counter.incrementAndGet();
        }
        latch.countDown(); // Signal that the writer has completed
        writeSemaphore.release(); // Release write lock
    }

    public void startReading() {
        readerSemaphore.release(numReaders); // Signal all readers to start reading
    }

    public void awaitWriterCompletion() throws InterruptedException {
        latch.await(); // Wait for the writer to complete
    }
}

class Reader implements Runnable {
    private final SharedCounter counter;
    private final int readerId;

    public Reader(SharedCounter counter, int readerId) {
        this.counter = counter;
        this.readerId = readerId;
    }

    @Override
    public void run() {
        try {
            counter.read(readerId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Writer implements Runnable {
    private final SharedCounter counter;

    public Writer(SharedCounter counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        try {
            counter.write();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ReaderWriter {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please provide the number of readers as a command line argument.");
            return;
        }

        int numReaders = Integer.parseInt(args[0]);
        if (numReaders < 1 || numReaders > 12) {
            System.out.println("The number of readers must be between 1 and 12.");
            return;
        }

        SharedCounter counter = new SharedCounter(numReaders);

        System.out.println("Executing... please wait...");

        // Create and start reader threads
        for (int i = 1; i <= numReaders; i++) {
            new Thread(new Reader(counter, i)).start();
        }

        // Create and start writer thread
        new Thread(new Writer(counter)).start();

        try {
            counter.awaitWriterCompletion(); // Wait for the writer to complete
            counter.startReading(); // Start allowing readers to read
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
            System.out.println("Writer Done!");
    }
}