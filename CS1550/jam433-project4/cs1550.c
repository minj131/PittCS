/*
	FUSE: Filesystem in Userspace
	Copyright (C) 2001-2007  Miklos Szeredi <miklos@szeredi.hu>

	This program can be distributed under the terms of the GNU GPL.
	See the file COPYING.
*/

#define	FUSE_USE_VERSION 26

#include <fuse.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>

//size of 5MB disk in bytes
#define DISK_SIZE 5242880

//size of a disk block
#define	BLOCK_SIZE 512

// Size of bitmap buffer
#define BITMAP_SIZE 	10240		//(DISK_SIZE / BLOCK_SIZE)

//we'll use 8.3 filenames
#define	MAX_FILENAME 8
#define	MAX_EXTENSION 3

//How many files can there be in one directory?
#define MAX_FILES_IN_DIR (BLOCK_SIZE - sizeof(int)) / ((MAX_FILENAME + 1) + (MAX_EXTENSION + 1) + sizeof(size_t) + sizeof(long))

//The attribute packed means to not align these things
struct cs1550_directory_entry
{
	int nFiles;	//How many files are in this directory.
				//Needs to be less than MAX_FILES_IN_DIR

	struct cs1550_file_directory
	{
		char fname[MAX_FILENAME + 1];	//filename (plus space for nul)
		char fext[MAX_EXTENSION + 1];	//extension (plus space for nul)
		size_t fsize;					//file size
		long nStartBlock;				//where the first block is on disk
	} __attribute__((packed)) files[MAX_FILES_IN_DIR];	//There is an array of these

	//This is some space to get this to be exactly the size of the disk block.
	//Don't use it for anything.  
	char padding[BLOCK_SIZE - MAX_FILES_IN_DIR * sizeof(struct cs1550_file_directory) - sizeof(int)];
} ;

typedef struct cs1550_root_directory cs1550_root_directory;

#define MAX_DIRS_IN_ROOT (BLOCK_SIZE - sizeof(int)) / ((MAX_FILENAME + 1) + sizeof(long))

struct cs1550_root_directory
{
	int nDirectories;	//How many subdirectories are in the root
						//Needs to be less than MAX_DIRS_IN_ROOT
	struct cs1550_directory
	{
		char dname[MAX_FILENAME + 1];	//directory name (plus space for nul)
		long nStartBlock;				//where the directory block is on disk
	} __attribute__((packed)) directories[MAX_DIRS_IN_ROOT];	//There is an array of these

	//This is some space to get this to be exactly the size of the disk block.
	//Don't use it for anything.  
	char padding[BLOCK_SIZE - MAX_DIRS_IN_ROOT * sizeof(struct cs1550_directory) - sizeof(int)];
} ;


typedef struct cs1550_directory_entry cs1550_directory_entry;

//How much data can one block hold?
#define	MAX_DATA_IN_BLOCK (BLOCK_SIZE - sizeof(long))


struct cs1550_disk_block
{
	//The next disk block, if needed. This is the next pointer in the linked 
	//allocation list
	long nNextBlock;

	//And all the rest of the space in the block can be used for actual data
	//storage.
	char data[MAX_DATA_IN_BLOCK];
};

typedef struct cs1550_disk_block cs1550_disk_block;


/******************************************************************************
 *
 *  HELPER METHODS
 *
 *****************************************************************************/

static void get_root(cs1550_root_directory *root);
static void get_subdirectory(char *directory, cs1550_directory_entry *dir_entry);
static int get_next_block(void);
static int is_directory(char *directory);
static int is_file(char *directory, char *filename, char *extension);
static void create_directory(char *directory);
static void create_file(char *directory, char *filename, char *extension);
static void create_block(cs1550_disk_block *db, long seek);
static void update_root(cs1550_root_directory *root);
static void update_bitmap(int block_number);
static void update_directory(cs1550_directory_entry *dir_entry, char *directory);

// Get root directory
static void get_root(cs1550_root_directory *root)
{
	FILE *fp = fopen(".disk", "rb");

	if (fp != NULL)
	{
		fread(root, sizeof(struct cs1550_root_directory), 1, fp);
		fclose(fp);
	}
}

// get sub directory
static void get_subdirectory(char *directory, cs1550_directory_entry *dir_entry)
{
	FILE *fp;
	cs1550_root_directory root;
	get_root(&root);

	int start_block = 0;
	int i;
	for (i=0; i <root.nDirectories; i++)
	{
		if (strcmp(root.directories[i].dname, directory) == 0)
		{
			start_block = root.directories[i].nStartBlock;
		}
	}

	fp = fopen(".disk", "rb");
	if (fp != NULL)
	{
		fseek(fp, start_block, SEEK_SET);
		fread(dir_entry, BLOCK_SIZE, 1, fp);
		fclose(fp);
	}
}

// returns position of next back
// returns -1 if no free block available
static int get_next_block()
{
	FILE *fp = fopen(".disk", "rb");
	int res = -1;
	int OFFSET = 0 - BITMAP_SIZE;
	int i;

	fseek(fp, OFFSET, SEEK_END);
	for (i=0; i<BITMAP_SIZE; i++)
	{
		unsigned char next = fgetc(fp);
		if (i != 0 && next == 0)
		{
			res = i;
			break;
		}
		OFFSET++;
		fseek(fp, OFFSET, SEEK_END);
	}
	fclose(fp);
	return res;
}

// Check to see if directory exists
// Returns 1 if exists
// Else returns -1 if not exist
static int is_directory(char *directory)
{
	printf("directory to find: %s\n", directory);

	int res = -1;
	cs1550_root_directory root;
	get_root(&root);

	int i = 0;

	for (i=0; i <root.nDirectories; i++)
	{
		if (strcmp(root.directories[i].dname, directory) == 0)
		{
			res = 1;
		}
	}
	// printf("directory exists res is: %d\n", res);
	return res;
}

// checks to see if file exists
// returns size of file on success
static int is_file(char *directory, char *filename, char *extension)
{
	cs1550_directory_entry dir_entry;
	get_subdirectory(directory, &dir_entry);

	int res = -1;
	int i;
	for (i=0; i<dir_entry.nFiles; i++)
	{
		if ((strcmp(dir_entry.files[i].fname, filename) == 0) &&
			(strcmp(dir_entry.files[i].fext, extension) == 0))
		{
			res = dir_entry.files[i].fsize;
		}
	}
	return res;
}

// creates directory
// and updates bitmap and root accordingly
static void create_directory(char *directory)
{
	int block = get_next_block();
	if (block != -1)
	{
		cs1550_root_directory root;
		get_root(&root);
		strcpy(root.directories[root.nDirectories].dname, directory);
		root.directories[root.nDirectories].nStartBlock = (long) BLOCK_SIZE*block;
		root.nDirectories++;

		update_root(&root);
		update_bitmap(block);
	}
}

// creates file and updates directory entry 
static void create_file(char *directory, char *filename, char *extension)
{
	int i;
	cs1550_root_directory root;
	get_root(&root);

	for (i=0; i<root.nDirectories; i++)
	{
		if (strcmp(root.directories[i].dname, directory) == 0)
		{
			// get next free block
			int free_block = get_next_block();
			printf("free block is %i\n", free_block);
			update_bitmap(free_block);

			cs1550_directory_entry dir_entry;
			get_subdirectory(directory, &dir_entry);
			
			// copy file and extension
			strcpy(dir_entry.files[dir_entry.nFiles].fname, filename);
			strcpy(dir_entry.files[dir_entry.nFiles].fext, extension);

			dir_entry.files[dir_entry.nFiles].fsize = 0;
			dir_entry.files[dir_entry.nFiles].nStartBlock = (long)(BLOCK_SIZE*free_block);
			dir_entry.nFiles++;

			
			FILE *fp = fopen(".disk", "rb+");
			if (fp != NULL)
			{
				fseek(fp, 0, SEEK_END);
				int size = ftell(fp);

				fseek(fp, 0, SEEK_SET);
				char *buf = (char *)malloc(size);
				fread(buf, size, 1, fp);

				fseek(fp, 0, SEEK_SET);
				memmove(buf+root.directories[i].nStartBlock, &dir_entry, BLOCK_SIZE);
				fwrite(buf, size, 1, fp);
				free(buf);
				fclose(fp);
			}
		}
	}
}

// creates a block for buffer
static void create_block(cs1550_disk_block *db, long seek)
{
	FILE *fp = fopen(".disk", "rb+");

	if (fp != NULL)
	{
		fseek(fp, 0, SEEK_END);
		int size = ftell(fp);

		fseek(fp, 0, SEEK_SET);
		char *buf = (char *)malloc(size);
		fread(buf, size, 1, fp);

		fseek(fp, 0, SEEK_SET);
		memmove(buf+seek, db, BLOCK_SIZE);
		fwrite(buf, size, 1, fp);
		free(buf);
		fclose(fp);
	}
}

static void update_root(cs1550_root_directory *root)
{
	int size = 0;
	FILE *fp = fopen(".disk", "rb+");
	if (fp != NULL)
	{
		fseek(fp, 0, SEEK_END);
		size = ftell(fp);

		fseek(fp, 0, SEEK_SET);
		char *buf = (char *)malloc(size);
		fread(buf, size, 1, fp);
		fseek(fp, 0, SEEK_SET);

		memmove(buf, root, BLOCK_SIZE);
		fwrite(buf, size, 1, fp);
		fclose(fp);
		free(buf);
	}
}

static void update_bitmap(int block_number)
{
	FILE *fp = fopen(".disk", "rb+");

	fseek(fp, 0, SEEK_END);
	int size = ftell(fp);
	int offset = size - BITMAP_SIZE;

	fseek(fp, 0, SEEK_SET);
	char *bitmap = (char *)malloc(size);
	fread(bitmap, size, 1, fp);

	fseek(fp, 0, SEEK_SET);
	bitmap[offset+block_number] = 1;
	fwrite(bitmap, size, 1, fp);

	fclose(fp);
	free(bitmap);
}

static void update_directory(cs1550_directory_entry *dir_entry, char *directory)
{
	cs1550_root_directory root;
	get_root(&root);
	int i;

	for (i=0; i<root.nDirectories; i++)
	{
		if (strcmp(root.directories[i].dname, directory) == 0)
		{
			int block = (int)root.directories[i].nStartBlock;
			
			FILE *fp = fopen(".disk", "rb+");
			if (fp != NULL)
			{
				fseek(fp, 0, SEEK_END);
				int size = ftell(fp);

				fseek(fp, 0, SEEK_SET);
				char *bitmap = (char *)malloc(size);
				fread(bitmap, size, 1, fp);

				fseek(fp, 0, SEEK_SET);
				memmove(bitmap+block, dir_entry, BLOCK_SIZE);
				fwrite(bitmap, size, 1, fp);
				free(bitmap);
				fclose(fp);
			}
			break;
		}
	}
}

/*
 * Called whenever the system wants to know the file attributes, including
 * simply whether the file exists or not. 
 *
 * man -s 2 stat will show the fields of a stat structure
 */
static int cs1550_getattr(const char *path, struct stat *stbuf)
{
	int res = 0;

	memset(stbuf, 0, sizeof(struct stat));

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];
   
	//is path the root dir?
	if (strcmp(path, "/") == 0) {
		stbuf->st_mode = S_IFDIR | 0755;
		stbuf->st_nlink = 2;
	} else {
		memset(directory, 0, MAX_FILENAME+1);
		memset(filename, 0, MAX_FILENAME+1);
		memset(extension, 0, MAX_EXTENSION+1);

		sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

		if (strlen(filename) == 0)
		{
			//Check if name is subdirectory
			if (is_directory(directory) == 1)
			{
				printf("directory exists\n");
				//Might want to return a structure with these fields
				stbuf->st_mode = S_IFDIR | 0755;
				stbuf->st_nlink = 2;
			}
			else
			{
				printf("directory does not exist\n");
				res = -ENOENT;
			}
		}
		else
		{
			//Check if name is a regular file
			int file_size = is_file(directory, filename, extension);
			if (file_size != -1)
			{
				//regular file, probably want to be read and write
				stbuf->st_mode = S_IFREG | 0666; 
				stbuf->st_nlink = 1; //file links
				stbuf->st_size = (size_t) file_size; //file size - make sure you replace with real size!
				res = 0; // no error
			}
			else
			{
				res = -ENOENT;
			}
		}
	}
	return res;
}

/* 
 * Called whenever the contents of a directory are desired. Could be from an 'ls'
 * or could even be when a user hits TAB to do autocompletion
 */
static int cs1550_readdir(const char *path, void *buf, fuse_fill_dir_t filler,
			 off_t offset, struct fuse_file_info *fi)
{
	int res = 0;

	//Since we're building with -Wall (all warnings reported) we need
	//to "use" every parameter, so let's just cast them to void to
	//satisfy the compiler
	(void) offset;
	(void) fi;

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	int i;

	// If root
	if (strcmp(path, "/") == 0)
	{
		cs1550_root_directory root;
		get_root(&root);
		for (i=0; i<root.nDirectories; i++)
		{
			filler(buf, root.directories[i].dname, NULL, 0);
		}
	}
	else
	{
		sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

		if (is_directory(directory) == 1)
		{
			cs1550_directory_entry dir_entry;
			get_subdirectory(directory, &dir_entry);

			for (i=0; i<dir_entry.nFiles; i++)
			{
				if ((strcmp(dir_entry.files[i].fext, "\0") == 0))
				{
					filler(buf, dir_entry.files[i].fname, NULL, 0);
				}
				else
				{
					filler(buf, (strcat(strcat(dir_entry.files[i].fname, "."),
					dir_entry.files[i].fext)), NULL, 0);
				}
			}
		}
		else
		{
			res = -ENOENT;
		}
	}

	/*
	//add the user stuff (subdirs or files)
	//the +1 skips the leading '/' on the filenames
	filler(buf, newpath + 1, NULL, 0);
	*/
	filler(buf, ".", NULL, 0);
	filler(buf, "..", NULL, 0);	
	return res;
}

/* 
 * Creates a directory. We can ignore mode since we're not dealing with
 * permissions, as long as getattr returns appropriate ones for us.
 */
static int cs1550_mkdir(const char *path, mode_t mode)
{
	(void) path;
	(void) mode;

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(directory, 0, MAX_FILENAME+1);
	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);


	sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	// if not under root
	if (strlen(filename) > 0)
	{
		return -EPERM;
	}
	else
	{
		// if directory doesnt exist
		if (is_directory(directory) == -1)
		{
			// if directory meets filename length
			if (strlen(directory) <= MAX_FILENAME)
			{
				create_directory(directory);
			}
			else
			{
				return -ENAMETOOLONG;
			}
		}
		else
		{
			return -EEXIST;
		}
	}

	return 0;
}

/* 
 * Removes a directory.
 */
static int cs1550_rmdir(const char *path)
{
	(void) path;
    return 0;
}

/* 
 * Does the actual creation of a file. Mode and dev can be ignored.
 *
 */
static int cs1550_mknod(const char *path, mode_t mode, dev_t dev)
{
	int res = 0;

	(void) mode;
	(void) dev;

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(directory, 0, MAX_FILENAME+1);
	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);

	sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	if (strlen(filename) == 0)
	{
		return -EPERM;
	}
	else
	{
		printf("file to add is: %s\n", filename);

		if (strlen(filename) > MAX_FILENAME || strlen(extension) > MAX_EXTENSION)
		{
			return -ENAMETOOLONG;
		}
		else
		{
			// file doesn't exist
			if (is_file(directory, filename, extension) == -1)
			{
				create_file(directory, filename, extension);
			}
			// file already exists
			else
			{
				return -EEXIST;
			}
		}
	}
	return res;
}

/*
 * Deletes a file
 */
static int cs1550_unlink(const char *path)
{
    (void) path;

    return 0;
}

/* 
 * Read size bytes from file into buf starting from offset
 *
 */
static int cs1550_read(const char *path, char *buf, size_t size, off_t offset,
			  struct fuse_file_info *fi)
{
	(void) fi;

	int res = 0;

	int i, j, count;

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(directory, 0, MAX_FILENAME+1);
	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);

	cs1550_directory_entry dir_entry;
	cs1550_disk_block d_block;
	cs1550_disk_block curr_d_block;

	sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	// If path is a directory
	if (strlen(filename) == 0)
	{
		return -EISDIR;
	}
	else
	{
		if (is_directory(directory) == 1 && size > 0)
		{
			get_subdirectory(directory, &dir_entry);
			for (i=0; i<dir_entry.nFiles; i++)
			{
				if((strcmp(filename, dir_entry.files[i].fname) == 0)
					&& (strcmp(extension, dir_entry.files[i].fext) == 0))
				{
					if (offset <= dir_entry.files[i].fsize)
					{
						int block_offset = offset/BLOCK_SIZE;
						long seek = dir_entry.files[i].nStartBlock;
						long start = 0;
						FILE *fp = fopen(".disk", "rb+");

						for (j=0; j <= block_offset; j++)
						{
							start = seek;
							fseek(fp, seek, SEEK_SET);
							fread(&d_block, BLOCK_SIZE, 1, fp);
							seek = d_block.nNextBlock;
						}
						fseek(fp, 0, SEEK_SET);

						// refresh count
						count = 0;
						int index = 0;

						seek = start;
						while (seek)
						{
							// keep reading until end of block is reached
							fseek(fp, seek, SEEK_SET);
							fread(&curr_d_block, BLOCK_SIZE, 1, fp);

							// send to buffer
							if (count < MAX_DATA_IN_BLOCK)
							{
								buf[index] = (char)curr_d_block.data[count];
								count++;
								index++;
							}
							else
							{
								seek = curr_d_block.nNextBlock;
								count = 0;
							}
						}
						fclose(fp);
						res = size;
					}
				}
			}
		}
	}
	return res;
}

/* 
 * Write size bytes from buf into file starting from offset
 *
 */
static int cs1550_write(const char *path, const char *buf, size_t size, 
			  off_t offset, struct fuse_file_info *fi)
{
	(void) fi;

	int res = 0;

	// Set parse buffers
	char directory[MAX_FILENAME+1];
	char filename[MAX_FILENAME+1];
	char extension[MAX_EXTENSION+1];

	memset(directory, 0, MAX_FILENAME+1);
	memset(filename, 0, MAX_FILENAME+1);
	memset(extension, 0, MAX_EXTENSION+1);

	cs1550_directory_entry dir_entry;
	cs1550_disk_block d_block;
	cs1550_disk_block curr_d_block;

	sscanf(path, "/%[^/]/%[^.].%s", directory, filename, extension);

	if (is_directory(directory) == 1 && size > 0)
	{
		int res = 0;
		int i, j, k;	
		int next_block, old_size, new_size;

		get_subdirectory(directory, &dir_entry);

		for (i=0; i<dir_entry.nFiles; i++)
		{
			if((strcmp(filename, dir_entry.files[i].fname) == 0)
					&& (strcmp(extension, dir_entry.files[i].fext) == 0))
			{
				if (offset > dir_entry.files[i].fsize)
				{
					res = -EFBIG;
				}
				else
				{
					long seek = dir_entry.files[i].nStartBlock;
					int block_offset = offset/BLOCK_SIZE;

					FILE *fp = fopen(".disk", "rb+");
					long start = 0;

					for (j=0; j<=block_offset; j++)
					{
						start = seek;
						fseek(fp, seek, SEEK_SET);
						fread(&d_block, BLOCK_SIZE, 1, fp);
						seek = d_block.nNextBlock;
					}

					fseek(fp, 0, SEEK_SET);
					int count = 0; // refresh to 0
					seek = start;
					fseek(fp, seek, SEEK_SET);
					fread(&curr_d_block, BLOCK_SIZE, 1, fp);

					// for each character in the buffer
					for (k=0; k<strlen(buf); k++)
					{
						if (count<MAX_DATA_IN_BLOCK)
						{
							curr_d_block.data[count] = (char)buf[k];
							count++;
						}
						else
						{
							count = 0;
							// while there are blocks to traverse
							if (curr_d_block.nNextBlock != 0)
							{
								create_block(&curr_d_block, seek);
								seek = curr_d_block.nNextBlock;

								fseek(fp, seek, SEEK_SET);
								fread(&curr_d_block, BLOCK_SIZE, 1, fp);
							}
							// end of file block
							// allocate new block
							// and repeat
							else
							{
								long c_seek = seek;
								next_block = get_next_block();
								printf("next block is %i\n", next_block);
								seek = next_block*BLOCK_SIZE;

								curr_d_block.nNextBlock = seek;
								create_block(&curr_d_block, c_seek);
								fseek(fp, seek, SEEK_SET);
								fread(&curr_d_block, BLOCK_SIZE, 1, fp);

								update_bitmap(next_block);
							}
						}
						// if end of block is not reached
						if (k == (strlen(buf)-1))
						{
							create_block(&curr_d_block, seek);
							count = 0;
						}
					}
					
					fclose(fp);
					old_size = dir_entry.files[i].fsize;
					new_size = (2*offset) + (size-old_size);
					dir_entry.files[i].fsize = new_size;

					update_directory(&dir_entry, directory);
					res = size;
				}
			}
		}
	}
	//check to make sure path exists
	//check that size is > 0
	//check that offset is <= to the file size
	//write data
	//set size (should be same as input) and return, or error
	return res;
}


/******************************************************************************
 *
 *  DO NOT MODIFY ANYTHING BELOW THIS LINE
 *
 *****************************************************************************/

/*
 * truncate is called when a new file is created (with a 0 size) or when an
 * existing file is made shorter. We're not handling deleting files or 
 * truncating existing ones, so all we need to do here is to initialize
 * the appropriate directory entry.
 *
 */
static int cs1550_truncate(const char *path, off_t size)
{
	(void) path;
	(void) size;

    return 0;
}


/* 
 * Called when we open a file
 *
 */
static int cs1550_open(const char *path, struct fuse_file_info *fi)
{
	(void) path;
	(void) fi;
    /*
        //if we can't find the desired file, return an error
        return -ENOENT;
    */

    //It's not really necessary for this project to anything in open

    /* We're not going to worry about permissions for this project, but 
	   if we were and we don't have them to the file we should return an error

        return -EACCES;
    */

    return 0; //success!
}

/*
 * Called when close is called on a file descriptor, but because it might
 * have been dup'ed, this isn't a guarantee we won't ever need the file 
 * again. For us, return success simply to avoid the unimplemented error
 * in the debug log.
 */
static int cs1550_flush (const char *path , struct fuse_file_info *fi)
{
	(void) path;
	(void) fi;

	return 0; //success!
}


//register our new functions as the implementations of the syscalls
static struct fuse_operations hello_oper = {
    .getattr	= cs1550_getattr,
    .readdir	= cs1550_readdir,
    .mkdir	= cs1550_mkdir,
	.rmdir = cs1550_rmdir,
    .read	= cs1550_read,
    .write	= cs1550_write,
	.mknod	= cs1550_mknod,
	.unlink = cs1550_unlink,
	.truncate = cs1550_truncate,
	.flush = cs1550_flush,
	.open	= cs1550_open,
};

//Don't change this.
int main(int argc, char *argv[])
{
	return fuse_main(argc, argv, &hello_oper, NULL);
}
