University of Pittsburgh
CS447 jrMIPS Project

Jamie Min
jam433@pitt.edu


###################
###     HOW TO USE     ###
###################

Currently the program has the second example given in the reference manual
in its memory (test2.txt and test2.dat) :

.data
a: 10
b: 0

.text
la $r1,a
lw $r2,$r1
loop: add $r3,$r2
addi $r2,-1
bp $r2,loop
la $r1,b
sw $r3,$r1
lw $r4,$r1
put $r4,0 # answer should be 0x37
halt

It correctly runs the program and loads 37 as the result.

To use the program, load the individual ROM and RAM elements and enable the simulation.

I have included the first example in the reference manual. It is "test.txt".


###################
###        GENERAL        ###
###################

I decided to initially approach this by defining individual sub circuits and using those, but instead
decided to create a general decoder for all of the instructions. The R_I format is abstracted into the decoder
and set as the ALU_src and works as far as I know.

The LED display shows the register value when "put" is called, and the HALT LED lights up when "halt" is called.
The HALT terminates the program.

reg_file: 
	The subcircuit holds the register files. It uses only one write port which still works with all of the instructions.

ALU:
	The ALU subcircuit determines what arithmetic logic operations to carry out. It takes in the OPCODE and the ALUOP and evaluates AND, NOR, ADD, SUB, MULT, and DIV.
	
decoder:
	Holds the decoder information for all of the instructions. Takes OPCODE in and outputs the control signal depending on the type of instruction. Also houses the PUT/HALT control signal.
	
sign_extend:
	Takes input and uses sub_OP to determine whether to zero extend and sign extend.
	
shift_logic:
	Takes Rs, Rt, and shift amount determined by the instruction and the subop to determine which direction to shift and outputs the shift.
	
branch_logic:
	Takes in Rs, and the OPCODE plus sub_OP to determine what kind of branch instruction to do in accordance to the instruction sheet. I abstracted the jump logic out and used the branch_logic
	to also determine what type of jump instruction to do should the jump control signal be activated.
	
I decided not to use a fields list as I wanted more direct control over the specific bits of the instruction. I also used a lot of tunnels to keep the workspace easier to see.

#################################
###    WHAT WORKS / KNOWN PROBLEMS     ###
#################################

All of the instructions listed in the instruction sheet PDF should work. 

The only issue I had was abstracting the jump logic to work with the various logic and mux operations I used in the main program. As far as I know, the jump instructions do work
as intended and should output to the right location depending on the jump instruction. Though, if I had more time to work on this problem, I would definitely define the specific jump_logic
and use a subcircuit to determine what type of jump operation to do. I also wasn't sure how to incorporate the immediate value to the jump instruction. I assume it's the same as the branch type.

Other than that little quirk, all instruction set functionality should work as intended.

