.data
	firstint:  .asciiz "What is the first value?\n"
	secondint: .asciiz "What is the second value?\n"
	msg1:      .asciiz "The product of "
	msg2:      .asciiz " and "
	msg3:      .asciiz " is "
.text

	# prompt user for first value
	la 		$a0, firstint
	addi 		$v0, $zero, 4
	syscall

	# get first integer and store into $t0
	li 		$v0, 5
	syscall
	move 		$t0, $v0
	
	# prompt user for second value
	la 		$a0, secondint
	addi 		$v0, $zero, 4
	syscall
	
	# get second integer and store into $t1
	li 		$v0, 5
	syscall
	move 		$t1, $v0
	
	# get product and move from lo register to $t2
	mult 		$t0, $t1
	mflo 		$t2
	
	# print out the product msg and the product 
	la 		$a0, msg1
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 1
	move 		$a0, $t0
	syscall
	
	la 		$a0, msg2
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 1
	move 		$a0, $t1
	syscall
	
	la 		$a0, msg3
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 1
	move 		$a0, $t2
	syscall
	
	# terminate program
	li		$v0, 10
	syscall
