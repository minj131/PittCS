import java.io.Serializable;
import java.util.Random;
import java.math.BigInteger;

public class LargeInteger implements Serializable {

	private final byte[] ZERO = {(byte) 0};
	private final byte[] ONE = {(byte) 1};

	private byte[] val;

	/**
	 * Construct the LargeInteger from a given byte array
	 * @param b the byte array that this LargeInteger should represent
	 */
	public LargeInteger(byte[] b) {
		val = b;
	}

	/**
	 * Construct the LargeInteger by generatin a random n-bit number that is
	 * probably prime (2^-100 chance of being composite).
	 * @param n the bitlength of the requested integer
	 * @param rnd instance of java.util.Random to use in prime generation
	 */
	public LargeInteger(int n, Random rnd) {
		val = BigInteger.probablePrime(n, rnd).toByteArray();
	}

	/**
	 * Return this LargeInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/**
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other LargeInteger to sum with this
	 */
	public LargeInteger add(LargeInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		LargeInteger res_li = new LargeInteger(res);

		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public LargeInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		LargeInteger neg_li = new LargeInteger(neg);

		// add 1 to complete two's complement negation
		return neg_li.add(new LargeInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other LargeInteger to subtract from this
	 * @return difference of this and other
	 */
	public LargeInteger subtract(LargeInteger other) {
		return this.add(other.negate());
	}

	/**
	 * Compute the product of this and other (Gradeschool Multiplication)
	 * @param other LargeInteger to multiply by this
	 * @return product of this and other
	 */
	public LargeInteger multiply(LargeInteger other) {
		// Start initial sum to be zero
		LargeInteger result = new LargeInteger(ZERO);

		LargeInteger xCopy = new LargeInteger(tempArray(this.val));
		LargeInteger yCopy = new LargeInteger(tempArray(other.val));

		// Since we're evaluating the multiplication with each digit
		// We loop until we hit a 0 multiplier
		while (!yCopy.isZero()) {
			if (yCopy.isOdd())
				result = result.add(new LargeInteger(increaseByte(xCopy.val)));

			xCopy = xCopy.shiftLeft(1);
			yCopy = yCopy.shiftRight(1);
		}
		return result;
	}

	/**
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another LargeInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	public LargeInteger[] XGCD(LargeInteger other) {
		LargeInteger first = this;

		LargeInteger[] result = new LargeInteger[3];

		int count;
		for (count=0; first.or(other).and().isZero(); count++) {
			first = first.shiftRight(1);
			other = other.shiftRight(1);
		}

		// Shift right until we hit an LSB of 1 (odd)
		while (first.and().isZero())
			first = first.shiftRight(1);

		while (!other.isZero()) {
			while(other.and().isZero())
				other = other.shiftRight(1);

			if (first.compareTo(other) == 1) {
				LargeInteger temp = other;
				other = first;
				first = temp;
			}
			other = other.subtract(first);
		}
		result[0] = first.shiftLeft(count);
		result[1] = first;
		result[2] = other;

		return result;
	}

	//TODO: IMPLEMENT THIS METHOD

	/**
	 * Compute the result of raising this to the power of y mod n
	 * @param y exponent to raise this to
	 * @param n modulus value to use
	 * @return this^y mod n
	 */
	public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
		return null;
	}

	private LargeInteger divide(LargeInteger other){
		return null;
	}

	private LargeInteger modulus(LargeInteger other) {
		return null;
	}

	/********************************************************************************
	*                                HELPER METHODS                                  *
	 **********************************************************************************/

	// Checks if LargeInteger is odd
	// In other words, if LSB is 1 or a 0
	public boolean isOdd() {
		byte lsb = (byte)(val[val.length-1]&0x1);
		return lsb != 0;
	}

	// Checks whether the number is equal to zero
	// Iterates through each byte and checks if value is equal to 0
	public boolean isZero() {
		for (byte b : val)
			if (b != 0)
				return false;

		return true;
	}

	// Shift to the left by n bits
	public LargeInteger shiftLeft(int n) {
		byte[] temp = val;

		for (int i=0; i<n; i++){ //Perform n shifts
			boolean carry = false;	// Initially set carry to false

			// If the MSB is 1, we must allocate more space when shifting left
			if((temp[0] & 128) == 0)	// If there is an overflow
				temp = tempArray(temp);
			else
				temp = increaseByte(temp);

			// Shifts left by 1
			for (int j = temp.length-1; j >= 0; j--){
				byte msb = (byte) (temp[j] & 128);

				temp[j] <<= 1;

				//If there's a carry, set the LSB to 1
				if(carry)
					temp[j] |= 1;

				carry = msb != 0;
			}
		}
		return new LargeInteger(temp);
	}

	// Shift to the right by n bits
	public LargeInteger shiftRight(int n) {
		byte[] temp = tempArray(val);

		for (int i=0; i<n; i++){ //Perform n shifts
			boolean carry = false;

			// Shifts each bit to the right
			for (int j=0; j<temp.length; j++){
				byte lsb = (byte) (temp[j] & 1);

				//Shift right by 1
				temp[j] = (byte) ((temp[j] & 0xFF) >> 1);
				if (carry)
					temp[j] |= 128; //If there's a carry, set the MSB to 1

				carry = lsb != 0;
			}	temp[0] &= 0x7F;
		}
		return new LargeInteger(temp);
	}

	//Returns whether a binary number is equal to the value 1
	public boolean isOne() {
		// Right away we can check if the LSB is 1 and return if it is 0
		if (val[val.length-1] != 1)
			return false;

		// Check if other bytes are equal to 0
		for (int i=0; i<val.length-1; i++){
			if (val[i] != 0)
				return false;
		}
		return true;
	}

	// Logical AND
	public LargeInteger and() {
		byte[] temp = tempArray(val);
		boolean and = (temp[temp.length-1] & 0x1) != 0;

		// Creates a copy
		for(int i=0; i<temp.length; i++)
			temp[i] = 0;

		if(and)
			temp[temp.length-1] = 0x1;

		return new LargeInteger(temp);
	}

	//Logical OR
	public LargeInteger or(LargeInteger other) {
		byte[] temp = other.val;
		byte[] result = new byte[val.length];

		// Iterate for each number
		for (int i=0; i<val.length; i++) {
			int curr = 1;

			// For each bits in a byte
			for (int j=7; j>=0; j--) {
				byte bit1, bit2;

				// If one number is longer than the other, just use that number
				if (i >= temp.length) {
					bit1 = (byte) (val[val.length - i - 1] & curr);
					bit2 = 0;

				} else {
					bit1 = (byte) (val[val.length - i - 1] & curr);
					bit2 = (byte) (temp[temp.length - i - 1] & curr);
				}

				byte bit;

				if (bit1 == 0 && bit2 == 0)
					bit = 0;

				else if (bit1 != 0 && bit2 == 0)
					bit = (byte) curr;

				else if (bit1 != 0)
					bit = (byte) curr;

				else
					bit = (byte) curr;

				result[val.length-i-1] |= bit;
				curr *= 2;
			}
		}
		return new LargeInteger(result);
	}

	// Trims leading zeros
	private LargeInteger trimLeadingZeros() {
		int leadingZeroBytes = 0;
		for (byte b : val) { //Count the number of leading bytes that have value 0
			if (b == 0) leadingZeroBytes++;
			else break;
		}

		byte[] newData = new byte[val.length-leadingZeroBytes]; //Initialize a new byte array with size of the original minus the number of leading 0 bytes
		System.arraycopy(val, leadingZeroBytes, newData, 0, newData.length);

		return new LargeInteger(newData);
	}

	// Creates a temporary object since LargeInteger is immutable
	private static byte[] tempArray(byte[] arr) {
		byte[] temp = new byte[arr.length];
		System.arraycopy(arr, 0, temp, 0, arr.length);
		return temp;
	}

	// Pads number with an additional byte
	public byte[] increaseByte(byte[] arr) {
		byte[] temp = new byte[arr.length+1];
		System.arraycopy(arr, 0, temp, 1, arr.length);
		return temp;
	}

	// Compare two binary numbers.
	// @Returns:
	// 		1 (this > other)
	// 		0 (this == other)
	// 	   -1 (this < other)
	public int compareTo(LargeInteger other){

		LargeInteger trimmedThis = this.trimLeadingZeros();
		LargeInteger trimmedOther = other.trimLeadingZeros();

		// First check length of each
		if(trimmedThis.getBitLength() > trimmedOther.getBitLength())
			return 1;
		else if(trimmedThis.getBitLength() < trimmedOther.getBitLength())
			return -1;

		int len = trimmedThis.val.length;
		byte[] thisVal = trimmedThis.val;
		byte[] otherVal = trimmedOther.val;

		// Sets MSB to 1 with a padding zero to the left
		byte curr = (byte) 0x40;

		// For each byte
		for(int i=0; i<len; i++){

			byte initB1 = (byte) ((thisVal[i] & 0x80) == 0 ? 0 : 1);
			byte intiB2 = (byte) ((otherVal[i] & 0x80) == 0 ? 0 : 1);

			if(initB1 > intiB2)
				return 1;
			else if(initB1 < intiB2)
				return -1;


			for(int j=6; j>=0; j--){
				byte bit1 = (byte) ((thisVal[i] & curr) == 0 ? 0 : 1);
				byte bit2 = (byte) ((otherVal[i] & curr) == 0 ? 0 : 1);

				if(bit1 > bit2)
					return 1;
				else if(bit1 < bit2)
					return -1;

				//RSL 1
				curr >>= 1;
			}
			curr = (byte) 0x40; //Reset currBitVal to 0x01000000
		}
		// If reached here, they are equal
		return 0;
	}

	private int getBitLength(){return val.length * 8;}

	public String toString() {
		String output = "";

		for (byte b : val) {
			output += String.format("%s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		}
		return output;
	}
}
