		.data
msg1:		.asciiz		"Please enter a number n:\n"
msg2:		.asciiz		"\nPlease enter another number k:\n"
msg3:		.asciiz		"\nThe chosen value is "

		.text
main:
		li		$t3, 1				# base case 1 reference
		
		la		$a0, msg1
		li		$v0, 4
		syscall
	
		li		$v0, 5
		syscall
		move		$t0, $v0
	
		la		$a0, msg2
		li		$v0, 4
		syscall
	
		li		$v0, 5
		syscall
		move		$t1, $v0
		
		bgt		$t0, $t1, loop			# branch if $t0 > $t1
		j		check_base
		
		bne		$t0, $t1, loop			# branch if $t0 != $t1
		j		check_base			# else if n = k, return 1
		
loop:
		move		$a0, $t0			# moves n to $a0
		subi		$a1, $a0, 1			# moves n-1 to $a1
		jal		loop_top
	
loop_top:	# n! main loop
		beq		$a1, $t3, loop_bottom		# return if arg1 = 1
		mult		$a0, $a1			# mult n(n-1)
		mflo		$a0				# stores value in $a0
		subi		$a1, $a1, 1			# decrements n
		jr		$ra				# loop

		
loop_bottom:	# sets k! (k! is computed at loop_top)
		move		$s0, $a0			# moves return value to $s0
		move		$a0, $t1			# moves k to $a0
		subi		$a1, $a0, 1			# moves k-1 to $a1
		beq		$a1, $t3, next
		jal		loop_top
		
next:		# sets (n-k)!
		move		$s1, $a0			# moves return value to $s1	
		sub		$t2, $t0, $t1			# $t2 = n - k
		move		$a0, $t2
		subi		$a1, $a0, 1
		jal		final_loop
		
final_loop:	# computes (n-k)!
		beq		$a1, $t3, choose		# branch when loop ends
		mult		$a0, $a1
		mflo		$a0
		subi		$a1, $a1, 1
		jr		$ra
		
choose:
		move		$s2, $a0			# moves return value to $s2
		mult		$s1, $s2			# k! * (n-k)!
		mflo		$a0				# moves product to $a0
		div		$s0, $s0, $a0
		
		la		$a0, msg3			# prints chosen value string
		li		$v0, 4
		syscall
		
		la		$a0, ($s0)			# prints chosen int
		li		$v0, 1
		syscall
		
		li		$v0, 10				# system exit
		syscall
		

check_base:	# checks if n = 1 and k = 1	
		la		$a0, msg3
		li		$v0, 4
		syscall
		
		blt		$t0, $t1, check_zero
		la		$a0, ($t3)
		li		$v0, 1
		syscall
		
		li		$v0, 10
		syscall

check_zero:	# prints 0 when n < k
		la		$a0, ($zero)
		li		$v0, 1
		syscall
		
		li		$v0, 10
		syscall