#include <sys/mman.h>
#include <sys/ioctl.h>
#include <sys/unistd.h>
#include <sys/select.h>
#include <linux/fb.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>
#include <fcntl.h>
#include <termios.h>

int fd = -1;
int total_size = 0;

// 16 bit unsigned short that represents RBG
typedef unsigned short color_t;

// screen structs
struct fb_var_screeninfo fbvs;
struct fb_fix_screeninfo fbfs;
unsigned short *mem_map;

// terminal settings
struct termios terminal;

void init_graphics()
{
	
	// open graphic device
	fd = open("/dev/fb0", O_RDWR);
	
	// retrieve screen info
	ioctl(fd, FBIOGET_VSCREENINFO, &fbvs);
	ioctl(fd, FBIOGET_FSCREENINFO, &fbfs);
	
	total_size = fbvs.xres_virtual * fbfs.line_length;
	
	// load memory map
	mem_map = (unsigned short *)mmap(0, total_size,
							PROT_WRITE, MAP_SHARED, fd, 0);
							
	
	ioctl(STDIN_FILENO, TCGETS, &terminal);
	terminal.c_lflag &= ~(ICANON | ECHO);
	ioctl(STDIN_FILENO, TCSETS, &terminal);
}

void exit_graphics()
{
	// close buffer
	close(fd);
	
	// undo mapping
	munmap(mem_map, total_size);
	
	// enable canonical and echo mode
	ioctl(STDIN_FILENO, TCGETS, &terminal);
	terminal.c_lflag |= (ICANON | ECHO);
	ioctl(STDIN_FILENO, TCSETS, &terminal);
}

void clear_screen()
{
	char *esc_code = "\033[2J";
	write(1, esc_code, sizeof(esc_code));
}

char getKey()
{
	char input;
	
	// struct for timeout
	struct timeval tv;
	tv.tv_sec = 3;	// wait for 3 seconds
	tv.tv_usec = 0;
	
	fd_set fdv;
	FD_ZERO(&fdv);
	FD_SET(0, &fdv);
	
	int selector_val = select(STDIN_FILENO+1, &fdv,
											NULL, NULL, &tv);
											
	if (selector_val)
		read(0, &input, sizeof(input));
	
	return input;
}

void sleep_ms(long ms)
{
	// time struct
	struct timespec tss;
	tss.tv_sec = 0;
	tss.tv_nsec = ms * 1000000;
	
	nanosleep(&tss, NULL);
}

void draw_pixel(int x, int y, color_t color)
{
	*(mem_map + y * fbvs.xres_virtual + x) = color;
}

void draw_rect(int x1, int y1, int width, int height, color_t color)
{
	int x, y;
	
	for (x=x1; x<x1+width; x++)
	{
		for (y=y1; y<y1+height; y++)
		{
			draw_pixel(x, y, color);
		}
	}
}

// Midpoint circle algorithm
// Usage see: https://en.wikipedia.org/wiki/Midpoint_circle_algorithm
void draw_circle(int x1, int y1, int radius, color_t color)
{
	int x = radius-1;
	int y = 0;
	int dx = 1;
	int dy = 1;
	int err = dx - (radius<<1);
	
	while (x >= y)
	{
		draw_pixel(x1 + x, y1 + y, color);
        draw_pixel(x1 + y, y1 + x, color);
        draw_pixel(x1 - y, y1 + x, color);
        draw_pixel(x1 - x, y1 + y, color);
        draw_pixel(x1 - x, y1 - y, color);
        draw_pixel(x1 - y, y1 - x, color);
        draw_pixel(x1 + y, y1 - x, color);
        draw_pixel(x1 + x, y1 - y, color);
		
		if (err <= 0)
		{
			y++;
			err += dy;
			dy += 2;
		} 
		else 
		{
			x--;
			dx += 2;
			err += dx - (radius<<1);
		}
	}
}




