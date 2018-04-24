		.data
names: 		.asciiz 	"alex\n", "sam\n", "jamie\n", "andi\n", "riley\n"
cities: 	.asciiz 	"boston", "new york", "chicago", "pittsburgh", "denver"
msg1:		.asciiz		"Please enter a name:\n"
msg2:		.asciiz		"City is: "
msg3:		.asciiz		"Not found!"
input:		.space		20

		.text
main:
		la		$s0, names
		la		$s1, cities
		
		# prompt user for name
		la		$a0, msg1
		li		$v0, 4
		syscall
		
		# takes in string
		la		$a0, input
		la		$a1, 20
		li		$v0, 8
		syscall
		
name0:
		# checks if input matches first name
		la		$a0, ($s0)		# passes name into arg 0
		la		$a1, input		# passes input into arg 1
		
		jal		checkName
		
		beq		$t9, 1, name1		# checks $t9 for index counter, if name0 is not found, branch to name1
		
		jal		useName	
		
		# prints city at index
		la		$a0, ($s1)
		li		$v0, 4
		syscall
		j		exit
		
name1:
		# checks if input matches second name
		la		$a0, 6($s0)		# passes name into arg0
		la		$a1, input		# passes input into arg1
		
		jal 		checkName
		
		beq		$t9, 2, name2		# checks $t9 for index counter, if name1 is not found, branch to name2
		jal		useName
		
		# prints city at index
		la		$a0, 7($s1)
		li		$v0, 4
		syscall
		j		exit
		
name2:
		# checks if input matches third name
		la		$a0, 11($s0)		# passes name into arg0
		la		$a1, input		# passes input into arg1
		
		jal 		checkName
		
		beq		$t9, 3, name3		# checks $t9 for index counter, if name2 is not found, branch to name3
		jal		useName
		
		# prints city at index
		la		$a0, 16($s1)
		li		$v0, 4
		syscall
		j		exit
		
name3:
		# checks if input matches fourth name
		la		$a0, 18($s0)		# passes name into arg0
		la		$a1, input		# passes input into arg1
		
		jal 		checkName
		
		beq		$t9, 4, name4		# checks $t9 for index counter, if name3 is not found, branch to name4
		jal		useName
		
		# prints city at index
		la		$a0, 24($s1)
		li		$v0, 4
		syscall
		j		exit
		
name4:
		# checks if input matches last name
		la		$a0, 24($s0)		# passes name into arg0
		la		$a1, input		# passes input into arg1
		
		jal 		checkName

		jal		useName
		
		# prints city at index
		la		$a0, 35($s1)
		li		$v0, 4
		syscall
		j		exit
		
checkName:
		la		$t0, ($a0)		# moves arg0 to $t0
		la		$t1, ($a1)		# moves arg1 to $t1
		
checkLoop:
		lb		$t2, 0($t0)		# loads a byte from string 0	
		lb		$t3, 0($t1)		# loads a byte from string 1
		
		bne		$t2, $t3, notEqual	# branch to not equal if byte 0 != byte 1
		beq		$t2, $0, isEqual	# branch to is equal if stirng is null terminated with byte 0 = byte 1 
		
		addi		$t0, $t0, 1		
		addi		$t1, $t1, 1
		j		checkLoop

notEqual:
		li		$t8, 0			# if not equal, set $t8 flag = 0
		addi		$t9, $t9, 1		# set index counter = 1
		j		checkReturn

isEqual:	
		li		$t8, 1			# sets $t8 flag = 1
		
checkReturn:
		jr		$ra
		
useName:
		beq		$t8, 1, lookUp
		la		$a0, msg3
		li		$v0, 4
		syscall
		j		exit

lookUp:
		la 		$a0, msg2
		li		$v0, 4	
		syscall
		jr		$ra

exit:
		li		$v0, 10
		syscall
