#include <stdio.h>
#include <string.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
	FILE *file;

	if(argc < 2)
	{
		printf("Illegal Argument: Expected a file as second command line argument. Found none.\n");
		return 0;
	}
	else if(argc > 2)
	{
		printf("Illegal Argument: Expected a file as second line argument. Found more than one.\n");
		return 0;
	}
	else
	{
		file = fopen(argv[1], "rb");
		if(file==NULL)
		{
			printf("Illegal File. Either the file name is incorrect or the file does not exist.\n");
			return 0;
		}
	}

	// gets next character
	char read_char;
	fread(&read_char, sizeof(char), 1, file);
	
	// sets current buffer count to 0
	int char_count = 0;

	// allocates buffer size arbitrarily set to 4
	char *buffer = malloc(4*sizeof(char));

	// while not end of file
	while (!feof(file))
	{
		// if char is in range of printable characters
		if(read_char >= 32 && read_char <= 126)
		{
			// does strlen once and uses result
			int len = strlen(buffer);

			// if char in current string reaches limit of the buffer size
			// double the array
			if (len != 0 && char_count == len)
			{
				int resize = len*2*sizeof(char);
				buffer = (char*)realloc(buffer, resize);
			}
			
			// adds current char to buffer and increments char count
			buffer[char_count] = read_char;
			char_count++;
		}
		// if character is not in printable range, prnt the char
		else
		{
			if(char_count >= 4)
			{
				printf("%s\n", buffer);
			}
			
			// reset count and reinitialize buffer
			char_count=0;
			buffer = (char*)malloc(4*sizeof(char));
		}
		fread(&read_char, sizeof(char), 1, file);
	}
	// free mallocd memory and close file stream
	free(buffer);
	fclose(file);
	return 0;
}
