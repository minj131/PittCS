#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <sys/stat.h>

#define MAGIC_NUM 	4294967295 // 2^32 - 1
#define MAX_PAGE	1048575

#define OFFSET 		12

#define CLEAN_BIT 	20
#define DIRTY_BIT 	21
#define REF_BIT 	22

struct mem {
	unsigned int address;
	char mode;
};

struct OPT_page {
	unsigned int page_num;
	unsigned int next_ref;
};

struct age_frame {
	unsigned int page_num;
	unsigned char age;
};

// HELPER METHODS

void set_clean(unsigned int *page_table, unsigned int page)
{
	page_table[page] &= 0xFFFFFFFE;
}

// Checks if page is valid
// RETURNS 1 if valid, 0 if false
int get_valid(unsigned int *page_table, unsigned int page)
{
	return page_table[page] & 1<<CLEAN_BIT;
}

// Sets page number to valid in the page table
void set_valid(unsigned int *page_table, unsigned int page)
{
	page_table[page] |= 1<<CLEAN_BIT;
}

// Checks if page is dirty
// RETURNS 1 if dirty, 0 if false
int get_dirty(unsigned int *page_table, unsigned int page)
{
	return page_table[page] & 1<<DIRTY_BIT;
}

// Sets page number to dirty
void set_dirty(unsigned int *page_table, unsigned int page)
{
	page_table[page] |= 1<<DIRTY_BIT;
}

// Checks if page is referenced
// RETURNS 1 if ref, 0 is false
int get_ref(unsigned int *page_table, unsigned int page)
{
	return page_table[page] & 1<<REF_BIT;
}

// Sets the page number to referenced
void set_ref(unsigned int *page_table, unsigned int page)
{
	page_table[page] |= 1<<REF_BIT;
}

// Sets the page number to unreferenced
void unset_ref(unsigned int *page_table, unsigned int page)
{
	page_table[page] &= (~(1<<REF_BIT));
}

// Returns the next referenced page
unsigned int next_mem_ref(unsigned int page_num, struct mem *instruc,
	unsigned int count, unsigned int init)
{
	unsigned int i;
	unsigned int page = MAGIC_NUM;

	for (i=init; i<count; i++)
	{
		if((instruc[i].address>>OFFSET) == page_num)
		{
			page = i;
			break;
		}
	}
	return page;
}

// Resets ref, dirty, and clean bits of a page number
void reset_status(unsigned int *page_table, unsigned int page)
{
	page_table[page] &= (~(7<<CLEAN_BIT));
}

// Clears reference bit for valid pages
void reset_ref(unsigned int *page_table, int numframes)
{
	int i;
	for (i=0; i<numframes; i++)
	{
		unset_ref(page_table, i);
	}
}

// Clears reference bits for pages used in aging sim
void reset_ref_aging(unsigned int *page_table, struct age_frame *IPT, int clean)
{
	int i;
	for (i=0; i<clean; i++)
	{
		unset_ref(page_table, IPT[i].page_num);
	}
}

void update_page(unsigned int *page_table, unsigned int page_num,
	unsigned int frame_num, char mode)
{
	page_table[page_num] = frame_num;
	set_ref(page_table, page_num);
	set_valid(page_table, page_num);
	if (mode=='W')
	{
		set_dirty(page_table, page_num);
	}
}

void print_summary(char *algo, int numframes, int mem_access, int numfaults, int numwrites)
{
	printf("%s", algo);
	printf("Number of frames: %i\n", numframes);
    printf("Total memory accesses: %i\n", mem_access);
    printf("Total page faults: %i\n", numfaults);
    printf("Total writes to disk: %i\n", numwrites);
}

// SIM Algorithms Methods

void OPT_sim(struct mem *instruc, unsigned int count, 
	unsigned int *page_table, unsigned int page, int numframes)
{
	char *algo = "\nOPT\n";

	// Need a way to store the next reference
	struct OPT_page valid_page[numframes];

	unsigned int i, j, page_num;
	int index;

	int next_frame = 0;
	int faults = 0;
	int writes = 0;


	for (i=0; i<numframes; i++)
		valid_page[i].page_num = MAGIC_NUM;

	for (i=0; i<count; i++)
	{
		// get page number
		page_num = instruc[i].address>>OFFSET;

		// If the page is not valid
		if (!get_valid(page_table, page_num))
		{
			faults++;
			// If memory is full, swap
			if (next_frame == numframes)
			{
				index = 0;
				for (j=0; j<next_frame; j++)
				{
					// Prefer clean, then dirty
					if (valid_page[j].next_ref > valid_page[index].next_ref)
					{
						index = j;
					}
					else if (valid_page[j].next_ref == valid_page[index].next_ref)
					{
						if (!get_dirty(page_table, valid_page[index].page_num))
						{
							index = j;
						}
					}
				}
				// If dirty, we evict
				if (get_dirty(page_table, valid_page[index].page_num))
				{
					writes++;
					printf("On %i: Page Fault - Evict Dirty\n", i);
				}
				else
				{
					printf("On %i: Page Fault - Evict Clean\n", i);
				}

				// swap
				reset_status(page_table, valid_page[index].page_num);

				// Update to next page
				valid_page[index].page_num = page_num;
				valid_page[index].next_ref = next_mem_ref(page_num, instruc, count, i+1);
				update_page(page_table, page_num, index, instruc[i].mode);
			}
			// Memory is not full
			else
			{
				printf("On %i: Page Fault - No Eviction\n", i);
				valid_page[next_frame].page_num = page_num;
				valid_page[next_frame].next_ref = next_mem_ref(page_num, instruc, count, i+1);

				// Update page table
				update_page(page_table, page_num, next_frame, instruc[i].mode);
				next_frame++;
			}
		}
		else
		{
			printf("On %i: Hit\n", i);
			valid_page[page_table[page_num]&MAX_PAGE].next_ref = next_mem_ref(page_num, instruc, count, i+1);
			// If we have to do a write
			if (instruc[i].mode == 'W')
			{
				set_dirty(page_table, page_num);
			}
		}
	}
	print_summary(algo, numframes, i, faults, writes);
}

void clock_sim(struct mem *instruc, unsigned int count, 
	unsigned int *page_table, unsigned int page, int numframes)
{
	char *algo = "\nClock\n";

	// Array to hold the valid frames
	unsigned int *RAM;

	int faults = 0;
	int writes = 0;

	int clock_hand = 0;
	int valid_page = 0;
	int i, R;

	unsigned int page_num = 0;

	RAM = malloc(numframes*sizeof(unsigned int));

	for (i=0; i<count; i++)
	{
		page_num = instruc[i].address>>OFFSET;

		// If page is not valid
		if (!get_valid(page_table, page_num)) {
			faults++;

			// If memory is not full
			if (valid_page < numframes)
			{
				// just add page
				RAM[valid_page] = page_num;
				printf("On %i: Page Fault - No Eviction\n", i);
				update_page(page_table, page_num, valid_page, instruc[i].mode);
				valid_page++;
			}
			else
			{
				// Need to swap since memory is full
				R = get_ref(page_table, clock_hand);
				while (R)
				{
					unset_ref(page_table, clock_hand);
					clock_hand++;
					clock_hand %= numframes;
					R = get_ref(page_table, clock_hand);	// Clock now points to evict page
				}

				if (get_dirty(page_table, RAM[clock_hand])) 
				{
					writes++;
					printf("On %i: Page Fault - Evict Dirty\n", i);
				}
				else
				{
					printf("On %i: Page Fault - Evict Clean\n", i);
				}
				reset_status(page_table, RAM[clock_hand]);
				RAM[clock_hand] = page_num;
				update_page(page_table, page_num, clock_hand, instruc[i].mode);

				page_num = clock_hand;
				clock_hand++;
				clock_hand %= numframes;
			}
		}
		else
		{
			printf("On %i: Hit\n", i);
			if (instruc[i].mode == 'W')
			{
				set_dirty(page_table, page_num);
			}
		}
		set_ref(page_table, page_num);
	}
	print_summary(algo, numframes, i, faults, writes);
	free(RAM);
}

void aging_sim(struct mem *instruc, unsigned int count, 
	unsigned int *page_table, unsigned int page, int numframes, int ref_rate)
{
	char *algo = "\nAging\n";

	struct age_frame *IPT;
	unsigned int i, j, page_num;

	int faults = 0;
	int writes = 0;
	int valid_page = 0;
	int evict = 0;

	IPT = malloc(numframes*sizeof(struct age_frame));

	// for each instruction
	for (i=0; i<count; i++)
	{
		if (i%ref_rate == 0)
		{
			for (j=0; j<valid_page; j++)
			{
				// if referenced, bitwise OR with shift 7
				// then reset R bits
				IPT[j].age = IPT[j].age>>1;
				if(get_ref(page_table, IPT[j].page_num))
				{
					IPT[j].age = IPT[j].age | (1<<7);
				}
			}
			reset_ref_aging(page_table, IPT, valid_page);
		}

		page_num = instruc[i].address>>OFFSET;

		// if not valid, not in memory
		if(!get_valid(page_table, page_num))
		{
			faults++;
			// If memory is not full
			if(valid_page < numframes)
			{
				// Reset age
				printf("On %i: Page Fault - No Eviction\n", i);
				IPT[valid_page].page_num = page_num;
				IPT[valid_page].age = 0;
				update_page(page_table, page_num, valid_page, instruc[i].mode);
				valid_page++;
			}
			// Memory is full
			else
			{
				evict = 0;
				for (j=0; j<numframes; j++)
				{
					// Find frames to evict, first find oldest
					if (IPT[j].age > IPT[evict].age)
					{
						evict = j;
					}
					// If age is equal, prefer clean then dirty
					else if (IPT[j].age == IPT[evict].age)
					{
						if (!get_dirty(page_table, IPT[j].page_num))
						{
							evict = j;
						}
					}
				}

				// Evict the aged page
				if (get_dirty(page_table, IPT[evict].page_num))
				{
					writes++;
					printf("On %i: Page Fault - Evict Dirty\n", i);
				}
				else
				{
					printf("On %i: Page Fault - Evict Clean\n", i);
				}

				// swap and update page to evict
				reset_status(page_table, IPT[evict].page_num);
				IPT[evict].page_num = page_num;
				IPT[evict].age = 0;

				update_page(page_table, page_num, evict, instruc[i].mode);
			}
		}
		else
		{
			printf("On %i: Hit\n", i);
			set_ref(page_table, page_num);
			if (instruc[i].mode == 'W')
			{
				set_dirty(page_table, page_num);
			}
		}
	}
	print_summary(algo, numframes, i, faults, writes);
	free(IPT);
}

void work_sim(struct mem *instruc, unsigned int count, 
	unsigned int *page_table, unsigned int page, int numframes, int ref_rate, int tao)
{
	char *algo = "\nWork\n";

	unsigned int time[numframes];
	unsigned int *RAM;
	unsigned int page_num = 0;

	unsigned int min;

	int valid_page = 0;
	int clock_hand = 0;
	int evict_flag = 0;

	int faults = 0;
	int writes = 0;
	int oldest = 0;
	int access = 0;

	int i, j, k;

	RAM = malloc(numframes*sizeof(unsigned int));

	for (i=0; i<numframes; i++)
	{
		time[numframes] = 0;
	}

	for (i=0; i<count; i++)
	{
		access++;
		if (access%ref_rate == 0)
		{
			reset_ref(page_table, numframes);
		}

		evict_flag = 0;
		page_num = instruc[i].address>>OFFSET;

		// page fault
		if (!get_valid(page_table, page_num))
		{
			faults++;

			for (j=0; j<numframes; j++)
			{
				if (get_ref(page_table, j)) 
				{
					time[j] = access;
				}
			}

			// if memory is not full
			if (valid_page < numframes)
			{
				printf("On %i: Page Fault - No Eviction\n", i);
				RAM[valid_page] = page_num;
				update_page(page_table, page_num, valid_page, instruc[i].mode);
				valid_page++;
			}
			// Memory is full
			else
			{
				// determine which page to evict, use tao first
				for (j=0; j<numframes; j++)
				{
					// if page is older than tao
					if (access - time[clock_hand] > tao)
					{
						// Prefer clean, then dirty
						if (!get_dirty(page_table, RAM[clock_hand]))
						{
							// fix
							// set to evict and stop
							evict_flag = 1;
							break;
						}
						else
						{
							// writes++;
							set_valid(page_table, RAM[clock_hand]);
						}

						clock_hand++;
						clock_hand %= numframes;
					}
				}

				// At this point, nothing is older than tao so we simply evict oldest page
				if (evict_flag == 0)
				{
					oldest = 0;
					min = MAGIC_NUM;
					for (k=0; k<numframes; k++)
					{
						if (time[k] < min)
						{
							min = time[k];
							oldest = k;
						}
					}
					clock_hand = oldest;
				}

				if (get_dirty(page_table, RAM[clock_hand]))
				{
					writes++;
					printf("On %i: Page Fault - Evict Dirty\n", i);
				}
				else
				{
					printf("On %i: Page Fault - Evict Clean\n", i);
				}

				reset_status(page_table, RAM[clock_hand]);
				RAM[clock_hand] = page_num;
				update_page(page_table, page_num, clock_hand, instruc[i].mode);

				page_num = clock_hand;
				// clock_hand++;
				// clock_hand %= numframes;
			}
		}
		else
		{
			printf("On %i: Hit\n", i);
			if (instruc[i].mode == 'W')
			{
				set_dirty(page_table, page_num);
			}
		}
		set_ref(page_table, page_num);
	} 
	print_summary(algo, numframes, i, faults, writes);
	free(RAM);
}

// MAIN

int main(int argc, char *argv[])
{
	FILE *file;

	int numframes;
	char *algo;
	int ref_rate;
	char *tracefile;
	int tao;

	unsigned int address;
	char mode;
	struct mem *instruc;

	unsigned int *page_table;
	unsigned int page = 1<<20;

	int i;
	unsigned int file_size;
	unsigned int instruc_count;

	struct stat buffer;
	struct mem_ref *instr;

	if (argc<6)
	{
		printf("Error. See usage:\n");
		printf("$ ./vmsim –n numframes -a opt|clock|aging|work [-r refresh] [-t tau] tracefile \n");
		return 1;
	}

	page_table = malloc(page*4);
	memset(page_table, 0, page*4);

	numframes = atoi(argv[2]);
	algo = argv[4];

	// If running OPT or Clock
	if (argc == 6) 
	{
		tracefile = argv[5];
	}
	// If running Aging
	else if (argc == 8)
	{
		ref_rate = atoi(argv[6]);
		tracefile = argv[7];
	}
	// If running Work
	else
	{
		ref_rate = atoi(argv[6]);
		tao = atoi(argv[8]);
		tracefile = argv[9];
	}

	// Get file line number to determine virtual time
	stat(tracefile, &buffer);
	file_size = buffer.st_size;
	instruc_count = file_size / 11; // number of bytes per line

	file = fopen(tracefile, "r");
	instruc = malloc(instruc_count*sizeof(struct mem));

	// Pre-load instructions to memory
	for (i=0; i<instruc_count; i++) 
	{
		fscanf(file, "%x %c", &address, &mode);
		instruc[i].address = address;
		instruc[i].mode = mode;
	}

	// Run the appropriate algorithm
	if (0 == strcmp(algo, "opt"))
	{
		OPT_sim(instruc, instruc_count, page_table, page, numframes);
	} 
	else if (0 == strcmp(algo, "clock"))
	{
		clock_sim(instruc, instruc_count, page_table, page, numframes);
	}
	else if (0 == strcmp(algo, "aging")) 
	{
		aging_sim(instruc, instruc_count, page_table, page, numframes, ref_rate);
	}
	else if (0 == strcmp(algo, "work"))
	{
		work_sim(instruc, instruc_count, page_table, page, numframes, ref_rate, tao);
	}
	else
	{
		printf("Please check your spelling. %s is not a recognized algorithm.\n", algo);
	}

	fclose(file);
	free(page_table);
	free(instruc);
	return 0;
}