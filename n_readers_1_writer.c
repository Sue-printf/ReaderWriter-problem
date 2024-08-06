// Description of the Program
/*
The given C code describes the reader-writer problem solution using pthreads and semaphores. It captures, an initializes the shared common structure; contain semaphores and mutex locks as the primitives of the synchronization process. The reader function should take permission to read, access the shared counter, and print its ID together with the counter value. The writer function calls for exclusive access, increments the shared counter by a fixed number, and signals the readers to go ahead. The objective of the code is, first, to make sure that all processes will have chance to use the shared resource. Then, it prevents race conditions, deadlock, and starvation.
*/


#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>

typedef struct {
    int counter;
    sem_t writeSemaphore;
    sem_t readerSemaphore;
    int numReaders;
    pthread_mutex_t mutex;
    pthread_cond_t writerDone; // Condition variable for writer signal
} SharedCounter;

void initSharedCounter(SharedCounter* sc, int numReaders) {
    sc->counter = 0;
    sc->numReaders = numReaders;
    sem_init(&sc->writeSemaphore, 0, 1); // Allow the writer to enter
    sem_init(&sc->readerSemaphore, 0, 0); // Prevent readers from entering
    pthread_mutex_init(&sc->mutex, NULL);
    pthread_cond_init(&sc->writerDone, NULL); // Initialize condition variable
}

void* reader(void* arg) {
    SharedCounter* sc = (SharedCounter*)arg;
    int readerId = *((int*)arg + 1); // Extract reader ID from argument
    int counterValue = *((int*)arg); // Extract counter value from argument
    
    sem_wait(&sc->readerSemaphore); // Wait for permission to read
    pthread_mutex_lock(&sc->mutex); // Ensure exclusive access
    // No need to access the counter since we have its value already
    pthread_mutex_unlock(&sc->mutex);
    sem_post(&sc->readerSemaphore); // Release the semaphore
    
    printf("I'm reader%d, counter = %d\n", readerId, counterValue);
    
    return NULL;
}

void* writer(void* arg) {
    SharedCounter* sc = (SharedCounter*)arg;
    sem_wait(&sc->writeSemaphore); // Wait for exclusive access
    for (int i = 0; i < 25000; i++) {
        sc->counter++;
    }
    sem_post(&sc->readerSemaphore); // Signal readers to proceed
    sem_post(&sc->writeSemaphore); // Release exclusive access
    return NULL;
}

int main(int argc, char* argv[]) {
    if (argc != 2) {
        printf("Please provide the number of readers as a command line argument.\n");
        return 1;
    }

    int numReaders = atoi(argv[1]);
    if (numReaders < 1 || numReaders > 12) {
        printf("The number of readers must be between 1 and 12.\n");
        return 1;
    }

    SharedCounter sc;
    initSharedCounter(&sc, numReaders);

    printf("Executing... please wait...\n");

    pthread_t writerThread;
    pthread_create(&writerThread, NULL, writer, &sc);

    pthread_join(writerThread, NULL);

    printf("Writer Done!\n");

    pthread_t* readerThreads = malloc(numReaders * sizeof(pthread_t));

    for (int i = 0; i < numReaders; i++) {
        int* arg = malloc(2 * sizeof(int)); // Allocate memory for argument
        arg[0] = sc.counter; // Store counter value
        arg[1] = i + 1; // Store reader ID
        pthread_create(&readerThreads[i], NULL, reader, arg); // Pass the address of argument
    }

    for (int i = 0; i < numReaders; i++) {
        pthread_join(readerThreads[i], NULL);
    }

    sem_destroy(&sc.writeSemaphore);
    sem_destroy(&sc.readerSemaphore);
    pthread_mutex_destroy(&sc.mutex);
    free(readerThreads);

    return 0;
}