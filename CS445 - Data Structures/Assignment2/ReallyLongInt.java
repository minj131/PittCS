// CS 0445 Spring 2017
// This is a partial implementation of the ReallyLongInt class.  You need to
// complete the implementations of the remaining methods.  Also, for this class
// to work, you must complete the implementation of the LinkedListPlus class.
// See additional comments below.

public class ReallyLongInt 	extends LinkedListPlus<Integer> 
							implements Comparable<ReallyLongInt>
{
	// Instance variables are inherited.  You may not add any new instance variables
	
	// Default constructor
	private ReallyLongInt()
	{
		super();
	}

	// Note that we are adding the digits here in the FRONT. This is more efficient
	// (no traversal is necessary) and results in the LEAST significant digit first
	// in the list.  It is assumed that String s is a valid representation of an
	// unsigned integer with no leading zeros.
	public ReallyLongInt(String s)
	{
		super();
		char c;
		int digit;
		// Iterate through the String, getting each character and converting it into
		// an int.  Then make an Integer and add at the front of the list.  Note that
		// the add() method (from A2LList) does not need to traverse the list since
		// it is adding in position 1.  Note also the the author's linked list
		// uses index 1 for the front of the list.
		for (int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if (('0' <= c) && (c <= '9'))
			{
				digit = c - '0';
				this.add(1, new Integer(digit));
			}
			else throw new NumberFormatException("Illegal digit " + c);
		}
	}

	// Simple call to super to copy the nodes from the argument ReallyLongInt
	// into a new one.
	public ReallyLongInt(ReallyLongInt rightOp)
	{
		super(rightOp);
	}
	
	// Method to put digits of number into a String.  Since the numbers are
	// stored "backward" (least significant digit first) we first reverse the
	// number, then traverse it to add the digits to a StringBuilder, then
	// reverse it again.  This seems like a lot of work, but given the
	// limitations of the super classes it is what we must do.
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (numberOfEntries > 0)
		{
			this.reverse();
			for (Node curr = firstNode; curr != null; curr = curr.next)
			{
				sb.append(curr.data);
			}
			this.reverse();
		}
		return sb.toString();
	}

	// You must implement the methods below.  See the descriptions in the
	// assignment sheet

	public ReallyLongInt add(ReallyLongInt rightOp)
	{
		Node right = firstNode;
		Node left = rightOp.firstNode;

		ReallyLongInt result = new ReallyLongInt();

		int sum, carry = 0;

		while (right != null || left != null) {

			// Ternary operator that sets data to add depending on whether value to be added is null or not
			// if right != null, use right.data, else use 0
			// if left != null, use left.data, else use 0

			sum = carry + (right != null ? right.data : 0) + (left != null ? left.data : 0);

			if (sum >= 10) carry = 1;
			sum %= 10;

			result.add(sum);

			if (right !=  null) right = right.next;
			if (left != null) left = left.next;

		}	if (carry > 0) result.add(carry);

		return result;
	}
	
	public ReallyLongInt subtract(ReallyLongInt rightOp)
	{
		Node right = firstNode;
		Node left = rightOp.firstNode;

		ReallyLongInt result = new ReallyLongInt();

		int diff, carry = 0;

		if (this.compareTo(rightOp) < 0) throw new ArithmeticException("Invalid Difference -- Negative Number");

		while (right != null || left != null) {

			// if right != null, use right.data, else use 0
			// if left != null, use left.data, else use 0
			diff = carry + (right != null ? right.data : 0) - (left != null ? left.data : 0);
			if (diff < 0) {
				diff += 10;
				carry = -1;
			}

			if (diff > 0) result.add(diff);

			if (right !=  null) right = right.next;
			if (left != null) left = left.next;

		}	return result;
	}

	public int compareTo(ReallyLongInt rOp)
	{

		if (numberOfEntries > rOp.numberOfEntries) return 1;
		else if (numberOfEntries < rOp.numberOfEntries) return -1;
		else {

			Node right = firstNode;
			Node left = rOp.firstNode;

			while (right != null && left != null) {

				if (right.data > left.data) return 1;
				else if (left.data > right.data) return -1;

				right = right.next;
				left = left.next;
			}
		}	return 0;
	}

	public boolean equals(Object rightOp) { return this.compareTo((ReallyLongInt)rightOp) == 0; }

	public void multTenToThe(int num) { for (int i = 0; i < num ; i++) add(i, 0);}

	public void divTenToThe(int num) { leftShift(num-1); }
}
