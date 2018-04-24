.data
	msg1:  .asciiz "Please enter your integer:\n"
	msg2:  .asciiz "Here is the input in binary: "
	msg3:  .asciiz "\nHere is the input in hexadecimal: "
	msg4:  .asciiz "\nHere is the output in binary: "
	msg5:  .asciiz "\nHere is the output in hexadecimal: "
.text

	#prompt user for integer and store integer in $t0
	la 		$a0, msg1
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 5
	syscall
	move 		$t0, $v0
	
	#print user input in binary representation
	la		$a0, msg2
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 35
	move 		$a0, $t0
	syscall
	
	# print user input in hex representation
	la 		$a0, msg3
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 34
	move 		$a0, $t0
	syscall
	
	# bitwise AND for fourth least significant digit
	addi		$t1, $zero, 0x00001000 # hex for 0001 at bit values from range 12 - 15
	and		$t2, $t0, $t1
	srl		$t3, $t2, 12
	
	# bitwise AND for third least significant digit
	addi		$t1, $zero, 0x00002000 # hex for 0010 at bit values from range 12 - 15
	and		$t2, $t0, $t1
	srl		$t4, $t2, 12
	add		$t3, $t3, $t4
	
	# bitwise AND for second least significant digit
	addi		$t1, $zero, 0x00004000 # hex for 0100 at bit values from range 12 - 15
	and		$t2, $t0, $t1
	srl		$t5, $t2, 12
	add		$t3, $t3, $t5
	
	# bitwise AND for first least significant digit
	addi		$t1, $zero, 0x00008000 # hex for 1000 at bit values from range 12 - 15
	and		$t2, $t0, $t1
	srl		$t6, $t2, 12
	add		$t3, $t3, $t6
	
	
	#print output in binary representation
	la		$a0, msg4
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 35
	move 		$a0, $t3
	syscall
	
	# print output in hex representation
	la 		$a0, msg5
	addi 		$v0, $zero, 4
	syscall
	li 		$v0, 34
	move 		$a0, $t3
	syscall
	
	# terminate program
	li		$v0, 10
	syscall