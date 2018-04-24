#include <stdio.h>
#include <string.h>

#define APP1 0xE1FF

// STRUCTURE FOR THE HEADER
struct jpg_header
{
	unsigned short start_marker;
	unsigned short app1_marker;
	unsigned short app1_length;
	char exif[4];
	unsigned short NUL_marker;
	char endianness[2];
	unsigned short version_num;
	unsigned int offset;
};

// STRUCTURE FOR THE TIFF TAGS

struct tiff_tag
{
	unsigned short tiff_id;
	unsigned short tiff_type;
	unsigned int tiff_num_items;
	unsigned int tiff_offset;
};


void get_sub_tags(FILE *image, int sub_count);

int main( int argc, char *argv[] ) 
{
	// VARIABLE DECLARATIONS	
	FILE *image;
	struct jpg_header header;
	struct tiff_tag tag;

	unsigned short tag_count;
	unsigned short MANF_STR = 0x010F;
	unsigned short CAMMOD_STR = 0x0110;
	unsigned short EXIF_SUB = 0x8769;
	unsigned short sub_count;

	int i;
	int curr_pos;
	char tag_string[100];

	// VERIFY CMD ARGS
	if (argc != 2) 
	{
		printf("Illegal format. Usage: './exifview img1.jpg'\n");
		return 0;
	}
	
	image = fopen(argv[1], "rb");
	
	if (image == NULL)
	{
		printf("File does not exist. Please check filename and try again.\n");
		return 0;
	}
	
	fread(&header, sizeof(header), 1, image);

	// VERIFY APP FORMAT
	if (header.app1_marker != APP1)
	{
		printf("Illegal format. Image contains APP0 marker.\n\n");
		return 0;
	}
	// VERIFY ENDIANNESS
	if (strcmp(header.endianness, "II*") != 0)
	{
		printf("Illegal format. Does not support MM type endianness.\n\n");
		return 0;
	}
	// VERIFY EXIF POSITION
	if (strcmp(header.exif, "Exif") != 0)
	{
		printf("Illegal format. The images does not contain EXIF data or is in the wrong position.\n");
		return 0;
	}

	// if main reaches here, image is formatted properly and ready to be extracted
	fread(&tag_count, sizeof(tag_count), 1, image);
	
	// iterate through image file tag_count times and find relevant information
	for (i=0; i<tag_count; i++)
	{
		fread(&tag, sizeof(tag), 1, image);
		curr_pos = ftell(image);

		// if tag matches manufacturer string
		if (tag.tiff_id == MANF_STR)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&tag_string, sizeof(tag_string[0]), tag.tiff_num_items, image);
			printf("%-20s %s\n", "Manufacturer:", tag_string);
		}
		// if tag matches camera model string
		else if (tag.tiff_id == CAMMOD_STR)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&tag_string, sizeof(tag_string[0]), tag.tiff_num_items, image);
			printf("%-20s %s\n", "Model:", tag_string);
		}
		// if tag matches exif sub block, proceed to get_sub_tags method
		else if (tag.tiff_id == EXIF_SUB)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&sub_count, sizeof(sub_count), 1, image);
			get_sub_tags(image, sub_count);
		}		
		fseek(image, curr_pos, SEEK_SET);
	}
	return 0;
}

void get_sub_tags(FILE *image, int sub_count)
{	
	// VARIABLE DECLARATIONS
	struct tiff_tag tag;
	
	int curr_pos;
	int i;
	int numer;
	int denom;

	unsigned short WIDTH = 0xA002;
	unsigned short HEIGHT = 0xA003;
	unsigned short ISO_SPEED = 0x8827;
	unsigned short EXP_SPEED = 0x829A;
	unsigned short F_STOP = 0x829D;
	unsigned short FOC_LEN = 0x920A;
	unsigned short DATE = 0x9003;				

	char tag_string[100];
	
	// iterate through count except this time look for relevant sub block information
	for (i=0; i<sub_count; i++)
	{
		fread(&tag, sizeof(tag), 1, image);
		curr_pos = ftell(image);
		
		// if id matches exposure speed
		if (tag.tiff_id == EXP_SPEED) 
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&numer, sizeof(int), 1, image);
			fread(&denom, sizeof(int), 1, image);
			printf("%-20s %d/%d second\n", "Exposure Time:", numer, denom);
		}
		// if id matches f_stop string
		else if (tag.tiff_id == F_STOP)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&numer, sizeof(int), 1, image);
			fread(&denom, sizeof(int), 1, image);
			printf("%-20s f/%.1f\n","F Stop:", (double)numer/denom);
		}
		// if id matches iso speed
		else if (tag.tiff_id == ISO_SPEED)
		{
			printf("%-20s ISO %d\n", "ISO:", tag.tiff_offset);
		}
		// if id matches date
		else if (tag.tiff_id == DATE)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&tag_string, sizeof(tag_string[0]), tag.tiff_num_items, image);
			printf("%-20s %s\n", "Date:", tag_string);
		}
		// if id matches focal lens
		else if (tag.tiff_id == FOC_LEN)
		{
			fseek(image, tag.tiff_offset+12, SEEK_SET);
			fread(&numer, sizeof(int), 1, image);
			fread(&denom, sizeof(int), 1, image);
			printf("%-20s %.0f mm\n", "Focal Length:", (double)numer/denom);
		}
		// if id matches width
		else if (tag.tiff_id == WIDTH)
		{
			printf("%-20s %d pixels\n", "Width:", tag.tiff_offset);
		}
		//if id matches height
		else if (tag.tiff_id == HEIGHT)
		{
			printf("%-20s %d pixels\n", "HEIGHT:", tag.tiff_offset);
		}
		fseek(image, curr_pos, SEEK_SET);
	}
}
