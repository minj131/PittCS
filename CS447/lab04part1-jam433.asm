		.data
msg1:		.asciiz		"Please enter your string:\n"
msg2:		.asciiz		"Please enter the character to replace:\n"
msg3:		.asciiz		"\nHere is the output:\n"
some_string:	.space		64

		.text
main:		
		li		$s0, '*'		# replacement char

		# prompt user for string
		la		$a0, msg1
		li		$v0, 4
		syscall

		# get string
		la		$a0, some_string
		la		$a1, 64
		li		$v0, 8
		move		$s2, $a0		# saves string to $s2
		syscall
		
		# prompts user for char
		la 		$a0, msg2
		li		$v0, 4
		syscall
		
		# gets char
		li		$v0, 12
		syscall
		move		$s1, $v0
		
		jal		replace
		
		# prompts converted string
		la		$a0, msg3
		li		$v0, 4
		syscall
		
		# runs rot13 method
		jal		rot13
		
		# prints converted string 
		jal		print
		
		# terminates program
		j		exit

		
replace:
		move		$t0, $s2

rep_loop:
		lbu		$t1, 0($t0)
		beq		$t1, $0, rep_exit	# null terminated
		sub		$t1, $t1, $s1		# check if char
		bne		$t1, $0, rep_next	# if not, go to next
		sb		$s0, 0($t0)		# if, then replace
		
rep_next:	
		addi		$t0, $t0, 1
		j		rep_loop
		
rep_exit:
		jr		$ra
		
rot13:
		move		$t0, $s2

		# checks if char is in range of A - M
rot_loop1:
		lbu		$t1, 0($t0)
		beq		$t1, $0, rot_exit
		blt		$t1, 'A', rot_loop2
		bgt		$t1, 'M', rot_loop2
		addi		$t1, $t1, 13
		sb		$t1, 0($t0)
		j		rot_next
		
		# checks if char is in range of N - Z
rot_loop2:
		blt		$t1, 'N', rot_loop3
		bgt		$t1, 'Z', rot_loop3
		addi		$t1, $t1, -13
		sb		$t1, 0($t0)
		j		rot_next
		
		# checks if char is in range of a - m
rot_loop3:
		blt		$t1, 'a', rot_loop4
		bgt		$t1, 'm', rot_loop4
		addi		$t1, $t1, 13
		sb		$t1, 0($t0)
		j		rot_next
		
		# checks if char is in range of n - z
rot_loop4:
		blt		$t1, 'n', rot_next
		bgt		$t1, 'z', rot_next
		addi		$t1, $t1, -13
		sb		$t1, 0($t0)
		j		rot_next
		
rot_next:
		addi		$t0, $t0, 1
		j		rot_loop1
		
rot_exit:
		jr		$ra
		
print:
		la		$a0, some_string	# save $v0
		li		$v0, 4
		syscall
		move		$v0,$t0	
		jr		$ra

exit:				
		li		$v0, 10
		syscall


		


		
