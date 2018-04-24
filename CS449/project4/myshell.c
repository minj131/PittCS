#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>


char *DELIM ="()<>|&; \n\t";

int get_num_args(char* input)
{
	char args[1024];
	char* buffer;
	int count = 0;
	
	// Copy input into temp array since we
	// want to keep integrity of original input
	strcpy(args, input);
	buffer = strtok(args, DELIM);
	
	// While current buffer still contains
	// more tokenizable items
	while (buffer != NULL)
	{
		count++;
		buffer = strtok(NULL, DELIM);
	}
	return count;
}

int main()
{
	char** args_array;
	char* arg;
	char input[1024];
	char token[1024];
	char path[512];
	
	int num_args = 0;
	int count = 0;
	int status;
	
	while(1)
	{
		printf("JAM433 $ ");
		fgets(input, 1024, stdin);
		
		// Obaints number of arguments
		num_args = get_num_args(input);
		args_array = malloc(sizeof(char*)*(num_args+1));
		
		// Reinitialize count to zero
		// Copies current input to the token array
		// Tokenzies input
		count = 0;
		strcpy(token, input);
		arg = strtok(token, DELIM);
		
		while (arg != NULL)
		{
			// Allocates space for the arguments
			args_array[count] = (char*)malloc(sizeof(char)*strlen(arg));
			strcpy(args_array[count], arg);
			
			// Takes next token and increments count
			arg = strtok(NULL, DELIM);
			count++;
		}
		args_array[num_args] = NULL;
		
		if (args_array[0] == NULL)
			continue;
		
		if (0 == strcmp(args_array[0], "exit"))
		{
			exit(0);
		}
		else if (0 == strcmp(args_array[0], "cd"))
		{
			if (0 > chdir(args_array[1]))
			{
				// No directory is specified
				if (args_array[1] == NULL)
					printf("cd: Please specify a directory\n");
				// Or directory cannot be found or is invalid
				else
					printf("cd: %s: No such file or directory\n", args_array[1]);
			}
			else
			{
				// Prints current directory
				getcwd(path, 512);
				printf("Directory has been changed to: %s\n", path);
			}
		}
		// Handles all other UNIX commands
		else 
		{
			// Forks the process
			// If the current process is the parent, wait
			if (fork() != 0)
			{
				wait(&status);
				//printf("Hi from parent!\n");
			}
			else
			{
				if (strstr(input, ">>") != NULL)
				{
					freopen(args_array[num_args-1], "a", stdout);
					free(args_array[num_args-1]);
					args_array[num_args-1] = NULL;
				} 
				else if (strstr(input, ">") != NULL)
				{
					freopen(args_array[num_args-1], "w", stdout);
					free(args_array[num_args-1]);
					args_array[num_args-1] = NULL;
				}
				else if (strstr(input, "<") != NULL)
				{
					freopen(args_array[num_args-1], "r", stdin);
					free(args_array[num_args-1]);
					args_array[num_args-1] = NULL;
				}
				execvp(args_array[0], args_array);
				//printf("Hi from child!\n");
				printf("%s : command not found\n", strtok(input, "\n"));
				exit(0);
			}
		}
	}
	return 0;
}
