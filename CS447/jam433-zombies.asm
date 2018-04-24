		.data
maze1:		.word		3,0,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2
				# "  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
maze2:		.word		2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2,2,0,0,0,0,0,0,2
				# "x      xx      xx      xx      xx      xx      xx      xx      x"
maze3:		.word		2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2,2,0,2,2,2,2,0,2
				# "x xxxx xx xxxx xx xxxx xx xxxx xx xxxx xx xxxx xx xxxx xx xxxx x"
maze4:		.word	 	2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2,2,0,2,0,0,2,0,2
				# "x x  x xx x  x xx x  x xx x  x xx x  x xx x  x xx x  x xx x  x x"
maze5:		.word		2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2 
				# "x                                                              x"
maze6:		.word		2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2,2,0,2,2,2,2,2,2 
				# "x xxxxxxx xxxxxxx xxxxxxx xxxxxxx xxxxxxx xxxxxxx xxxxxxx xxxxxx"
maze7:		.word		2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0
				# "  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  "
msg_win:	.asciiz		"Success! You won! Your total move score is: "
msg_lose:	.asciiz		"Sorry... You were captured."
zombie_loc:	.space		7	#0 = z1 X, 1 = z2 Y, 2 = z3 X, 3 = z3 Y, ... 6 = z4 X, 7 = z4 Y
direction:	.byte		0,1,2,3 # 
		.text
init:
	li	$s0, 0 		# x counter
	li	$s1, 0 		# y counter
	li	$s7, 0 		# line counter
	
	li	$s6, 3969	# flag to check if user is in (63, 63) 63*63=3969
	
maze:	
	la	$s2, maze1
	jal	getMaze
	
	beq	$s7, 7, bottom
	
drawMaze:
	# draws design of maze using defined line templates
	addi	$s1, $s1, 1
	la	$s2, maze2
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze3
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze4
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze4
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze3
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze5
	jal	getMaze
	
	addi	$s1, $s1, 1
	la	$s2, maze6
	jal	getMaze
	
	beq	$s7, 7, maze
	
	addi	$s1, $s1, 1
	la	$s2, maze6
	jal	getMaze
	
	addi	$s7, $s7, 1
	j	drawMaze
	
getMaze:
	# inits maze draw and stack
	addi	$sp, $sp, -16
	sw	$ra, 0($sp)
	sw 	$s0, 4($sp)
	sw	$s1, 8($sp)
	sw	$s2, 12($sp)

print_loop:
	# prints through each x and y of each line
	move	$a0, $s0 # x coord
	move	$a1, $s1 # y coord
	lw 	$a2, 0($s2)
	
	jal	_setLED
	
	addi	$s2, $s2, 4
	beq	$s0, 63, print_done
	addi	$s0, $s0, 1
	j	print_loop
	
print_done:
	# resets sp and stack
	lw	$s2, 12($sp)
	lw	$s1, 8($sp)
	lw	$s0, 4($sp)
	lw	$ra, 0($sp)
	addi	$sp, $sp, 16
	jr 	$ra
	
# void _setLED(int x, int y, int color)
	#   sets the LED at (x,y) to color
	#   color: 0=off, 1=red, 2=yellow, 3=green
	#
	# arguments: $a0 is x, $a1 is y, $a2 is color
	# trashes:   $t0-$t3
	# returns:   none
	#
_setLED:
	# byte offset into display = y * 16 bytes + (x / 4)
	sll	$t0,$a1,4      # y * 16 bytes
	srl	$t1,$a0,2      # x / 4
	add	$t0,$t0,$t1    # byte offset into display
	li	$t2,0xffff0008 # base address of LED display
	add	$t0,$t2,$t0    # address of byte with the LED
	# now, compute led position in the byte and the mask for it
	andi	$t1,$a0,0x3    # remainder is led position in byte
	neg	$t1,$t1        # negate position for subtraction
	addi	$t1,$t1,3      # bit positions in reverse order
	sll	$t1,$t1,1      # led is 2 bits
	# compute two masks: one to clear field, one to set new color
	li	$t2,3		
	sllv	$t2,$t2,$t1
	not	$t2,$t2        # bit mask for clearing current color
	sllv	$t1,$a2,$t1    # bit mask for setting color
	# get current LED value, set the new field, store it back to LED
	lbu	$t3,0($t0)     # read current LED value	
	and	$t3,$t3,$t2    # clear the field for the color
	or	$t3,$t3,$t1    # set color field
	sb	$t3,0($t0)     # update display
	jr	$ra
	
	# int _getLED(int x, int y)
	#   returns the value of the LED at position (x,y)
	#
	#  arguments: $a0 holds x, $a1 holds y
	#  trashes:   $t0-$t2
	#  returns:   $v0 holds the value of the LED (0, 1, 2 or 3)
	#
_getLED:
	# byte offset into display = y * 16 bytes + (x / 4)
	sll  	$t0,$a1,4      # y * 16 bytes
	srl  	$t1,$a0,2      # x / 4
	add  	$t0,$t0,$t1    # byte offset into display
	la   	$t2,0xffff0008
	add  	$t0,$t2,$t0    # address of byte with the LED
	# now, compute bit position in the byte and the mask for it
	andi 	$t1,$a0,0x3    # remainder is bit position in byte
	neg  	$t1,$t1        # negate position for subtraction
	addi 	$t1,$t1,3      # bit positions in reverse order
    	sll  	$t1,$t1,1      # led is 2 bits
	# load LED value, get the desired bit in the loaded byte
	lbu  	$t2,0($t0)
	srlv 	$t2,$t2,$t1    # shift LED value to lsb position
	andi 	$v0,$t2,0x3    # mask off any remaining upper bits
	jr   	$ra
	
_getKeyPress:
	la	$t1, 0xffff0000			# status register
	li	$v0, 0				# default to no key pressed
	lw	$t0, 0($t1)			# load the status
	beq	$t0, $zero, _keypress_return	# no key pressed, return
	lw	$v0, 4($t1)			# read the key pressed
	
_keypress_return:
	jr 	$ra

bottom:
	# draws last line
	la	$s2, maze7
	jal	getMaze
	
drawZombie:
	# draws zombie and stores coord in array
	la	$t9, zombie_loc
	
	li	$a0, 17 		# x counter
	li	$a1, 14			# y counter
	li	$a2, 1
	jal	_setLED	
	
	li	$s3, 17
	li	$s4, 14
	sb 	$s3, 0($t9)
	sb	$s4, 1($t9)

	
	
	li	$a0, 49 		# x counter
	li	$a1, 14			# y counter
	li	$a2, 1
	jal	_setLED
	
	li	$s3, 49
	li	$s4, 14
	sb 	$a0, 2($t9)
	sb	$a1, 3($t9)
	
	li	$a0, 17 		# x counter
	li	$a1, 46			# y counter
	li	$a2, 1
	jal	_setLED
	
	li	$s3, 17
	li	$s4, 46
	sb 	$a0, 4($t9)
	sb	$a1, 5($t9)
	
	li	$a0, 49 		# x counter
	li	$a1, 46			# y counter
	li	$a2, 1
	jal	_setLED
	
	li	$s3, 49
	li	$s4, 46
	sb 	$a0, 6($t9)
	sb	$a1, 7($t9)
	
getPlayer:
	# init player coordinate, ignores movement if wall is hit
	li 	$s0, 0 			# initialize x
	li 	$s1, 0 			# initialize y
	li	$t6, 0			# move counter
	
	jal	_getKeyPress
	beq	$v0, 0x42, start	# if b, start
		j getPlayer

start:		
	li	$v0, 30
	syscall
	move	$a0, $t8
		
player_loop:		
	li	$a0, 200
	li	$v0, 32
	syscall
	
	
	jal	_getKeyPress		# get the pressed key from the user
	
	beq 	$v0, 0xE0, up 		# up
	beq 	$v0, 0xE1, down 	# down
	beq 	$v0, 0xE2, left 	# left
	beq 	$v0, 0xE3, right 	# right
	
	jal	zombie_loop
	
	
continue:	
	addi	$t6, $t6, 1		# increment player counter
	multu	$s0, $s1
	mflo	$t7
	beq	$t7, $s6, win
	j	player_loop

up:	
	# passes in coord
	move 	$a0, $s0
	move 	$a1, $s1
	jal 	_getLED
	
	# moves player
	addi 	$t1, $a0, 0
	addi 	$t2, $a1, -1
	
	move 	$a0, $t1
	move	 $a1, $t2
	jal 	_getLED
	move 	$t4, $v0 
	
	# if wall, ignore
	beq 	$t4, 2, ignore
	
	# sets new led
	move 	$a0, $s0
	move 	$a1, $s1
	li 	$a2, 0
	jal 	_setLED
	
	# updates player coord
	addi 	$s0, $s0, 0
	addi 	$s1, $s1, -1
	
	move	$a0, $s0
	move 	$a1, $s1
	li   	$a2, 3 
	jal 	_setLED
	
	# if zombie, print lose message
	beq 	$t4, 1, lose
	j 	zombie_loop
	
right:	
	move 	$a0, $s0
	move 	$a1, $s1
	jal 	_getLED
	
	addi 	$t1, $a0, 1
	addi 	$t2, $a1, 0
	
	move 	$a0, $t1
	move 	$a1, $t2
	jal 	_getLED
	move 	$t4, $v0 
	
	beq 	$t4, 2, ignore
	
	move 	$a0, $s0
	move 	$a1, $s1
	li 	$a2, 0
	jal 	_setLED
	
	addi 	$s0, $s0, 1
	addi 	$s1, $s1, 0
	
	move 	$a0, $s0
	move 	$a1, $s1
	li  	$a2, 3 
	jal 	_setLED
	
	beq 	$t4, 1, lose
	j 	zombie_loop
	
down: 	
	move 	$a0, $s0
	move 	$a1, $s1
	jal 	_getLED
	
	addi 	$t1, $a0, 0
	addi 	$t2, $a1, 1
	
	move 	$a0, $t1
	move 	$a1, $t2
	jal 	_getLED
	
	move 	$t4, $v0 
	
	beq 	$t4, 2, ignore
	
	move 	$a0, $s0
	move 	$a1, $s1
	li 	$a2, 0
	jal 	_setLED
	
	addi 	$s0, $s0, 0
	addi 	$s1, $s1, 1
	
	move 	$a0, $s0
	move 	$a1, $s1
	li  	$a2, 3 
	jal	_setLED
	
	beq 	$t4, 1, lose
	j 	zombie_loop
	
left:	
	move 	$a0, $s0
	move 	$a1, $s1
	jal 	_getLED
	
	addi 	$t1, $a0, -1
	
	blt 	$t1, 0, ignore
	
	addi 	$t2, $a1, 0
	
	move 	$a0, $t1
	move 	$a1, $t2
	jal 	_getLED
	
	move 	$t4, $v0
	
	beq 	$t4, 2, ignore
	
	move 	$a0, $s0
	move 	$a1, $s1
	li 	$a2, 0
	jal 	_setLED
	
	addi 	$s0, $s0, -1
	addi 	$s1, $s1, 0
	
	move 	$a0, $s0
	move 	$a1, $s1
	li   	$a2, 3 
	jal 	_setLED
	
	beq 	$t4, 1, lose
	j 	zombie_loop
	
ignore:
	j 	continue
	
zombie_loop:
	lb	$s2, 0($t9)
	lb	$s3, 1($t9)
	
	move 	$a0, $s2
	move 	$a1, $s3
	jal 	_getLED
	
	addi 	$t1, $a0, 0
	addi 	$t2, $a1, 1
	
	move 	$a0, $t1
	move 	$a1, $t2
	jal 	_getLED
	
	move 	$t4, $v0 
	
	beq 	$t4, 2, ignore
	
	move 	$a0, $s2
	move 	$a1, $s3
	li 	$a2, 0
	jal 	_setLED
	
	addi 	$s2, $s2, 0
	addi 	$s3, $s3, 1
	
	move 	$a0, $s2
	move 	$a1, $s3
	li  	$a2, 1 
	jal	_setLED
	
z_loop:
	j	continue

win:
	la	$a0, msg_win
	li	$v0, 4
	syscall
	
	la	$a0, ($t6)
	li	$v0, 1
	syscall
	
	j	exit

lose:
	la	$a0, msg_lose
	li	$v0, 4
	syscall
	
exit:
	li	$v0, 10
	syscall
