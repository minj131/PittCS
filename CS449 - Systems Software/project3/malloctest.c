#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

#include "mymalloc.h"

int main()
{
	// TEST 1
	printf("The value of the original val brk is %p\n", sbrk(0));
	printf("Test 1: Malloc(5)");
	void* addr1 = my_firstfit_malloc(5);
	printf("\nThe current addr is %p\n", addr1);
	printf("The current brk  is %p\n", sbrk(0));
	printf("\n");

	// TEST 2
	printf("The value of the original val brk is %p\n", sbrk(0));
	printf("Test 2: Malloc(10)");
	void* addr2 = my_firstfit_malloc(10);
	printf("\nThe current addr is %p\n", addr1);
	printf("The current brk  is %p\n", sbrk(0));
	printf("\n");

	// TEST 3
	printf("The value of the original val brk is %p\n", sbrk(0));
	printf("Test 3: Malloc(100)");
	void* addr3 = my_firstfit_malloc(100);
	printf("\nThe current addr is %p\n", addr1);
	printf("The current brk  is %p\n", sbrk(0));
	printf("\n");

	// TEST 4
	printf("The value of the original val brk is %p\n", sbrk(0));
	printf("Test 4: Malloc(200)");
	void* addr4 = my_firstfit_malloc(200);
	printf("\nThe current addr is %p\n", addr1);
	printf("The current brk  is %p\n", sbrk(0));

	printf("Freeing address 1...\n");
	my_free(addr1);

	printf("Freeing address 2...\n");
	my_free(addr2);

	printf("Freeing address 3...\n");
	my_free(addr3);

	printf("Freeing address 4...\n");
	my_free(addr4);

	printf("Final val brk %p\n", sbrk(0));
	return 0;
}
