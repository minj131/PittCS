	.data
Array_A:.word		0xa1a2a3a4, 0xa5a6a7a8, 0xacabaaa9
msg1:	.asciiz 	"Please enter the index:\n"
msg2:	.asciiz		"Here is the output (word):\n"
msg3:	.asciiz		"\nHere is the output (half word):\n"
msg4:	.asciiz		"\nHere is the output (byte):\n"
msg5:	.asciiz		"Error: Index is out of range 1 - 3"

	.text
	# sets intial values registers to handle conditionals for words one, two, and three
	addi		$s0, $zero, 1
	addi		$s1, $zero, 2
	addi		$s2, $zero, 3
	
	# prompts user for index and stores index at register $t0
	la		$a0, msg1
	addi		$v0, $zero, 4
	syscall
	
	li		$v0, 5
	syscall
	move 		$t0, $v0
	
	la 		$s0, Array_A
	
	# conditional statements to handle different index positions
	beq		$t0, $s0, L1
	beq		$t0, $s1, L2
	beq		$t0, $s2, L3
	bgt 		$t0, $s2, Q4
	
	# prints out index values at index 1
L1:	lw		$t1, 0($s0)
	lhu		$t2, 0($s0)
	lbu  		$t3, 0($s0)
	j		print
	
	# prints out indices at index 2	
L2:	lw		$t1, 4($s0)
	lhu		$t2, 2($s0)
	lbu  		$t3, 1($s0)
	j		print
	
	# prints out indices at index 3
L3:	lw		$t1, 8($s0)
	lhu		$t2, 4($s0)
	lbu  		$t3, 2($s0)
	j		print
	
	# prints out error message due to index being greater than number of words
Q4:	la		$a0, msg5
	addi		$v0, $zero, 4
	syscall
	
	# terminate program
	li		$v0, 10
	syscall
	
print:	# prints word
	la 		$a0, msg2
	addi 		$v0, $zero, 4
	syscall
	
	la 		$a0, ($t1)
	li 		$v0, 34
	syscall
	
	# prints half word
	la		$a0, msg3
	addi		$v0, $zero, 4
	syscall
	
	la 		$a0, ($t2)
	li		$v0, 34
	syscall
	
	# prints byte
	la		$a0, msg4
	addi		$v0, $zero, 4
	syscall
	
	la		$a0, ($t3)
	li		$v0, 34
	syscall
	
	# terminate program
	li		$v0, 10
	syscall
