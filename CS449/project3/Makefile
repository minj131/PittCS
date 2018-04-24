all: program1 program2

program1: mymalloc.c mallocdrv.c
	gcc mymalloc.c mallocdrv.c -g -o driver -m32

program2: mymalloc.c malloctest.c
	gcc mymalloc.c malloctest.c -g -o malloctest -m32
