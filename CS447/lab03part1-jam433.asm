	.data
	
y:	.byte	33
z:	.byte	0
x:	.byte	16

	.text
	
	la 	$s0, y
	la 	$s1, x
	la	$s2, z

	lb	$t0, ($s0)
	lb	$t1, ($s1)

	sub	$t2, $t0, $t1
	sb	$t2, 0($s0)
	sb	$t2, 4($s0)
	sb	$t2, 8($s0)	



