#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include "e.h"

#define BYTE_OFFSET 2

int main (int argc, char* argv[])
{
	int i, x, y, num_digits;
	int e_dev;
	char *buf;
	
	/*
 	* Checks if correct number of arguments
 	*/ 
	if (argc != 3)
	{
		printf("Illegal command line arguments. Please use only two arguments.\n");
		return 1;
	}

	/*
 	* We need to determine the number of digits to retrieve
 	*/
	x = atoi(argv[1]);
	y = atoi(argv[2]);

	/*
 	* Checks if second argument is greater than the first argument 
 	*/
	if (y < x)
	{
		printf("Error : Second argument must be greater than the first.\n");
		return 1;
	}

	/*
 	* Checks if both arguments are positive
 	*/ 
	if (x < 0 || y < 0)
	{
		printf("Error : Arguments cannot be negative.\n");
		return 1;
	}

	/*
 	* We set num_digits offset by 1 due to the number of digits
 	* being needed as plus 1 (ex: 0 - 3) would be 4 digits [0,1,2,3]
 	*/
	num_digits = y + 1;

	/*
 	* Allocates space for the requested digits
 	*/ 
	buf = (char*)malloc((num_digits)*sizeof(char));
	
	/*
 	* Reads in the device driver
 	* read() syscall takes three params
 	* e_file: file descripter of where to read input
 	* buf: is the buffer that stores the space for the digits
 	* num_digits: needs an offset to account for the number of bytes to read before trunacting data
 	*    some cases work with no offset
 	*/ 
	e_dev = open("/dev/e", O_RDONLY);	
	read(e_dev, buf, num_digits+BYTE_OFFSET);

	/*
 	* Print out the requested digits
 	*/ 
	for (i=x; i<=y; i++)
		printf("%c", buf[i]);

	printf("\n");

	/*
 	* Free resources
 	*/ 
	free(buf);
	return 0;
}
