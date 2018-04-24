#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <linux/unistd.h>

struct cs1550_sem {
	int value;
	struct Node *head;
	struct Node *tail;
};

// Up/Down Wrapper functions
void down(struct cs1550_sem *sem)
{
	syscall(__NR_cs1550_down, sem);
}

void up(struct cs1550_sem *sem)
{
	syscall(__NR_cs1550_up, sem);
}

int main(int argc, char* argv[])
{
	int i,j, status;

	int producers = 0;
	int consumers = 0;
	int buffer_size = 0;

	void *sem_mem;
	void *shared_mem;

	int *producer_ptr = 0;
	int *consumer_ptr = 0;
	int *buffer_ptr;
	int *size_ptr;

	if (argc != 4)
	{
		printf("Invalid number of arguments. Usage: (3) arguments required.\n");
		return 1;
	}
	else
	{
		producers = atoi(argv[1]);
		consumers = atoi(argv[2]);
		buffer_size = atoi(argv[3]);

		// Shared memory in prodcon
		// Need multiple processes to be able to share the same memory region
		sem_mem = mmap(NULL, 3*sizeof(struct cs1550_sem), PROT_READ|PROT_WRITE,
			MAP_SHARED|MAP_ANONYMOUS, 0, 0);

		// Shared buffer for prodcon
		// Allowing for IPC between the prods and cons
		shared_mem = mmap(NULL, sizeof(int)*(buffer_size+3), PROT_READ|PROT_WRITE,
			MAP_SHARED|MAP_ANONYMOUS, 0, 0);

		struct cs1550_sem *empty = (struct cs1550_sem*)sem_mem;		// resources available
		struct cs1550_sem *full = (struct cs1550_sem*)sem_mem+1;	// resources used
		struct cs1550_sem *mutex = (struct cs1550_sem*)sem_mem+2;	// lock on critical regions

		// Empty semaphore
		empty->value = buffer_size;
		empty->head = NULL;
		empty->tail = NULL;

		// Full semaphore
		full->value = 0;
		full->head = NULL;
		full->tail = NULL;

		// Mutex semaphore, lock on critical region
		mutex->value = 1;
		mutex->head = NULL;
		mutex->head = NULL;

		// Semaphore pointers mapped to memory
		size_ptr = (int*)shared_mem;
		producer_ptr = (int*)shared_mem+1;
		consumer_ptr = (int*)shared_mem+2;
		buffer_ptr = (int*)shared_mem+3;

		// Create producers
		for (i=0; i<producers; i++)
		{
			// Child process
			if (fork() == 0)
			{
				int p_item;

				while(1)
				{
					down(empty);
					down(mutex);

					// Critical region
					p_item = *producer_ptr;
					buffer_ptr[*producer_ptr] = p_item;

					printf("Chef %c Produced: Pancake%d\n", (i+65), p_item);

					*producer_ptr = (*producer_ptr+1) % buffer_size;

					// Leave critical region
					up(mutex);
					up(full);
				}
			}
		}

		for (j=0; j<consumers; j++)
		{
			if (fork() == 0)
			{
				int c_item;

				while(1)
				{
					down(full);
					down(mutex);

					// Critical region
					c_item = buffer_ptr[*consumer_ptr];

					printf("Customer %c consumed: Pancake%d\n", j+65, c_item);
					*consumer_ptr = (*consumer_ptr+1) % buffer_size;

					// Leave critical region
					up(mutex);
					up(empty);
				}
			}
		}
		wait(&status);
	}
	return 0;
}