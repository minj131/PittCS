#include <stdio.h>

typedef unsigned short color_t;

void init_graphics();
void exit_graphics();
void clear_screen();
char getKey();
void sleep_ms(long ms);
void draw_rect(int x1, int y1, int width, int height, color_t c);
void draw_circle(int x1, int y1, int radius, color_t c);

int main()
{
	char key;
	int input;
	int x  = (600-20)/2;
	int y = (400-20)/2;
	
	clear_screen();
	
	printf("\n(1) to draw a rectangle.\n");
	printf("(2) to draw a circle.\n");
	printf("(q)uit to exit program anytime.\n");
	printf("Use the WASD keys to draw.\n");
	
	scanf("%d", &input);
	
	if (input == 1)
	{
		clear_screen();
		init_graphics();
		draw_rect(x, y, 60, 30, 20);
		do 
		{	
			key = getKey();
			if(key == 'w')
				y-=5;
			if(key == 's')
				y+=5;
			if(key == 'd')
				x+=5;
			if(key == 'a')
				x-=5;
			draw_rect(x, y, 60, 30, 20);
			sleep_ms(5000);
		}
		while (key != 'q');
		clear_screen();
		exit_graphics();
	}
	else if (input == 2)
	{
		clear_screen();
		init_graphics();
		draw_circle(x, y, 50, 20);
		do 
		{	
			key = getKey();
			if(key == 'w')
				y-=5;
			if(key == 's')
				y+=5;
			if(key == 'd')
				x+=5;
			if(key == 'a')
				x-=5;
			draw_circle(x, y, 50, 20);
			sleep_ms(5000);
		}
		while (key != 'q');
		clear_screen();
		exit_graphics();
	}

	
	clear_screen();
	exit_graphics();
	return 0;
}