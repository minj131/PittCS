#include <stdio.h>
#include <string.h>

#define MAX_GUESS 10
#define RED 0
#define ORANGE 1
#define YELLOW 2
#define GREEN 3
#define BLUE 4
#define PURPLE 5

void make_game(char game_array[]);
int check_guess(char player_guess[], char game_array[]);

int main()
{
	char ans[3];
	char game_array[4];
	char player_guess[4];
	int game_flag = 1;
	int curr_guess = 0;
	
	printf("\nWelcome to Mastermind!\n");
	
	while (game_flag == 1) 
	{
		curr_guess = 0;
		printf("\nWould you like to play? ");
		scanf("%s", &ans);
	
		if (strcmp(ans, "yes") == 0)
		{
			make_game(game_array);
			
			while (curr_guess <= MAX_GUESS)
			{
				printf("Enter guess number %i: \n", curr_guess);
				scanf("%s", &player_guess);
				
				if (check_guess(player_guess, game_array) == 1)
				{
					break;
				}
				else
				{
					curr_guess++;
				}
			}
			//if you get to here, game over. 
			if (curr_guess > MAX_GUESS) {
				printf("\nYou lose! The correct order was: %s\n", game_array);	
			}
		} 
		else if  (strcmp(ans, "no") == 0)
		{
			game_flag = 0;
			printf("\nThanks for playing! Goodbye.\n");
		}
		else
		{
			printf("Please enter a valid response(yes/no)!\n");
		}
	}
}

int check_guess(char player_guess[4], char game_array[4])
{
	int i;
	int color_pos = 0;
	int color_nopos = 0;

	for (i=0; i<4; i++)
	{
		if (game_array[i] ==  player_guess[i])
		{
			color_pos++;
			if (color_pos == 4)
			{
				printf("Congratulations, you won!\n\n");			
				return 1;
			}
		}
		else if (strchr(game_array, player_guess[i]) != NULL)
		{
			color_nopos++;
		}
	}
	printf("\nColors in the correct place: %d\n", color_pos);
	printf("Colors correct but in the wrong position: %d\n\n", color_nopos);
	return 0;
}

void make_game(char game_array[4])
{
	int i;
	int rng;
	
	srand((unsigned int)time(NULL));
	for (i=0; i<4; i++)
	{
		rng = rand()%6;
	
		switch(rng)
		{	
			case RED:
				strcpy(&game_array[i], "r");
				break;
			case ORANGE:
				strcpy(&game_array[i], "o");
				break;
			case YELLOW:
				strcpy(&game_array[i], "y");
				break;
			case GREEN:
				strcpy(&game_array[i], "g");
				break;
			case BLUE:
				strcpy(&game_array[i], "b");
				break;
			case PURPLE:
				strcpy(&game_array[i], "p");
				break;
		}
	}
}

