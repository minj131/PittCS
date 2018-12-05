#include <stdio.h>
#include <unistd.h>

#include "mymalloc.h"

typedef struct Node {
	int size;		// The size of this chunk of memory
	int is_free;		// Whether it is free(1) or empty(0)
	struct Node *next;	// A pointer to the next node
	struct Node *prev; 	// A pointer to the previous node
} *Node;


// Keeps track of the first and last node of the LL
static Node ll_first;
static Node ll_last;

Node add_node(int size)
{
	ll_last->next = (Node*)sbrk(sizeof(struct Node)+size);
	ll_last->next->prev = ll_last;
	ll_last->next->size = size;
	
	ll_last->next->next = NULL;
	ll_last->next->is_free = 0;
	ll_last = ll_last->next;
	
	return ll_last;
}

Node first_fit(int size)
{
	// Set current node to the beginning of the LL
	Node curr = ll_first;
	
	while (curr != NULL)
	{
		// When the allocated space request is the same size as the first fit found
		if((curr->is_free == 1) && curr->size == size)
		{
			curr->is_free = 0;
			curr->size;
			return curr;
		}
		
		// When there is enough free space, we can simply put the node in
		// and set the size to the new size
		if ((curr->is_free == 1) && (curr->size > size))
		{
			curr->is_free = 0;
			curr->size = size;
			return curr;
		}
		curr = curr->next;
	}
	// If it gets to here, no space could be found. We need to increase heap space
	return (void*)(add_node(size)+1);
}

void *my_firstfit_malloc(int size)
{
	Node node;
	
	// The current heap is empty, so we must add a new node
	// And set the attributes to reflect that
	if (ll_first == NULL)
	{
		ll_first = (Node*)sbrk(size+sizeof(struct Node));
		ll_first->is_free = 0;
		ll_first->prev = NULL;
		ll_first->next = NULL;
		ll_first->size = size;
		ll_last = ll_first;
		
		return (void*)ll_first+sizeof(struct Node);
	}
	else
	{
		// Else find first first space allocation for the request
		node = first_fit(size);
		
		// No new space can be found, so we increase heap space
		if (node == NULL)
		{
			return (void*)add_node(size)+sizeof(struct Node);
		}
		
		// Otherwise, we add the node normally
		else
		{
			node->is_free = 0;
			return (void*)node;
		}
	}
}

void my_free(void *ptr)
{
	Node node = (Node*)(ptr-sizeof(struct Node));
	int n;
	
	// If the ptr is null or the node is already free
	if (node->is_free || ptr == NULL)
		return;
	
	// If the node is in between the first and the last node
	if (node != ll_first && node != ll_last)
	{
		// Left shfts previous node by one then logical ORs it to the next node
		// returns neighbors
		n = ((node->prev->is_free)<<1)|(node->next->is_free);
		// both neighboring nodes are already free
		if (n == 3)
		{
			node->prev->size = node->prev->size + node->size + node->next->size + 2*sizeof(struct Node);
			node->prev->next = node->next->next;
			node->next->next->prev = node->prev;
		}
		// Previous node is free
		else if (n == 2)
		{
			node->prev->size = node->prev->size + node->size + 16;
			node->prev->next = node->next;
			node->next->prev = node->prev;
		}
		// Next ndoe is free
		else if (n == 1)
		{
			node->size = node->size + node->next->size+16;
			node->next->next->prev = node;
			node->next = node->next->next;
			node->is_free = 1;
		}
		// Neither is free
		else if (n == 0)
		{
			// We can just free that node
			node->is_free = 1;
		}
	}
	else
	{
		// Only 1 node, we can easily free the node
		if (ll_first == ll_last)
		{
			sbrk(-(ll_first->size - sizeof(struct Node)));
			ll_first = NULL;
			ll_last = ll_first;
		}
		// More than one node to free
		else
		{
			if (node == ll_first)
			{
				if (node->next->is_free)
				{
					// If the next node is the end of the LL
					if (node->next == ll_last)
					{
						// We can decrease heap size
						sbrk(0-ll_first->size - ll_last->size - 2*sizeof(struct Node));
					}
					ll_first->size = ll_first->next->size + sizeof(struct Node);
					ll_first->is_free = 1;
					ll_first->next->next->prev = ll_first;
					ll_first->next = ll_first->next->next;
				}
				// Else just set the first node as free
				else
				{
					ll_first->is_free = 1;
				}
			}
			// Same as above but in the case the node is the last node
			if (node == ll_last)
			{
				if (ll_last->prev->is_free)
				{
					if (ll_last->prev == ll_first)
					{
						sbrk(0-ll_last->size - ll_last->prev->size - 2*sizeof(struct Node));
						ll_first = NULL;
						ll_last = ll_first;
					}
					else
					{
						sbrk(0-ll_last->size - ll_last->prev->size - 2*sizeof(struct Node));
						ll_last = ll_last->prev->prev;
						ll_last->next = NULL;
					}
				}
				else
				{
					sbrk(0 - ll_last->size - sizeof(struct Node));
					ll_last = ll_last->prev;
					ll_last->next = NULL;
				}
			}
		}
	}
}
