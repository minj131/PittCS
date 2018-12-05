import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Mine {

	static final BigInteger MAX_TARGET = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
	static final int MAX_BLOCK = 16;
	static final char MIN_NONCE = 32;
	static final char MAX_NONCE = 126;
	static final String MINER_ADD = ";1333dGpHU6gQShR596zbKHXEeSihdtoyLb>";
	static final int MINER_CB = 50;

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Invalid arguments - see usage: java Mine *candidate_transaction_file* *difficulty* *prev_hash*");
			System.exit(-1);
		}

		File file = new File(args[0]);
		BigInteger difficulty = new BigInteger(Integer.toHexString(Integer.parseInt(args[1])), 16);
		BigInteger target = MAX_TARGET.divide(difficulty);
		String prevhash = args[2];


		// Variable for concatenation of all of the transactions used for the block
		StringBuilder concat = new StringBuilder();
		String concatRoot;

		// total amount of coins going to miner + the initial 50
		int ccount = MINER_CB;
		int tcount = 1;

		// fee -> Set -> transactions
		TreeMap<Integer, TreeSet<String>> transactions = new TreeMap<>(Collections.reverseOrder());
		ArrayList<String> tlist = new ArrayList<>();
		populateTransactionList(file, transactions);
		

		// for knapsack algo where each fee (val) is mapped to its transaction address and weights by its index
		ArrayList<String> tx_lines = new ArrayList<>();
		ArrayList<Integer> tx_values = new ArrayList<>();
		ArrayList<Integer> tx_weights = new ArrayList<>();

		// Iterate over our treemap and populate the values and weights we are going to 
		// use for the knapsack algorithm
		for (Map.Entry<Integer, TreeSet<String>> e : transactions.entrySet()) {
			int fee = e.getKey();
			TreeSet<String> tline = e.getValue();
			for (String s : tline) {
				tx_lines.add(s);
				tx_values.add(fee);
				tx_weights.add(getTransactionsCount(s));
			}
		}

		// knapsack to maximize fees
		int len = tx_values.size();
		int w = 15;	// max for a block minus the coinbase tx
		int tkp[][] = new int[len+1][w+1];

		for (int i=0; i<=len; i++) {
			for (int j=0; j <= w; j++) {
				if (i==0 || j==0)
					tkp[i][j] = 0;
				else if (tx_weights.get(i-1) <= j)
					tkp[i][j] = Math.max(tx_values.get(i-1) + tkp[i-1][j-tx_weights.get(i-1)], tkp[i-1][j]);
				else
					tkp[i][j] = tkp[i-1][j];
			}
		}

		// store and access results to get indices of the transactiosn that maximise fees
		int fees = tkp[len][w];
		ccount += fees;
		for (int i=len; i>0 && fees>0; i--) {
			if (fees == tkp[i-1][w])
				continue;
			else {
				concat.insert(0, tx_lines.get(i-1));
				tlist.add(0, tx_lines.get(i-1));
				tcount += tx_weights.get(i-1);
				fees -= tx_values.get(i-1);
				w -= tx_weights.get(i-1);
			}
		}

		// add miner to transaction lst and concat root
		tlist.add(MINER_ADD + Integer.toString(ccount));
		concat.append(MINER_ADD + Integer.toString(ccount));

		// // Calculate concat root hash
		Sha256Hash sha = new Sha256Hash();
		concatRoot = sha.calculateHash(concat.toString());

		// Get the block hash
		long curr = System.currentTimeMillis();
		String nonce = "    ";

		// 0 -> blockhash
		// 1 -> nonce
		String[] blockhash = calculateBlockHash(prevhash, Integer.toString(tcount), Long.toString(curr), args[1], nonce, concatRoot, target);

		if (blockhash.equals(null)) {
			System.out.println("No valid nonce found for the given difficulty");
			System.exit(0);
		}

		printBlock(blockhash[0], prevhash, tcount, curr, args[1], blockhash[1], concatRoot, tlist);
	}

	public static void printBlock(String hash, String prevhash, int numtrans, long ts,
							 String difficulty, String nonce, String concat, ArrayList<String> tlist) {
		System.out.println("CANDIDATE BLOCK = Hash " + hash);
		System.out.println("---");
		System.out.println(prevhash);
		System.out.println(numtrans);
		System.out.println(ts);
		System.out.println(difficulty);
		System.out.println(nonce);
		System.out.println(concat);
		for (String t : tlist) {
			System.out.println(t);
		}
	}

	public static void populateTransactionList(File file, TreeMap<Integer, TreeSet<String>> transactions) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				int txinput = 0;
				int txoutput = 0;
				// 0 -> inputs
				// 1 -> outputs
				String[] tokens = line.split(";");
				String[] inputs = tokens[0].split(",");
				String[] outputs = tokens[1].split(",");

				// get the total coins for inputs
				for (String i : inputs) {
					// where the coins are the 2nd index of txi
					String[] txi = i.split(">");
					txinput += Integer.parseInt(txi[1]);
				}

				// get the total coins for outputs
				for (String i : outputs) {
					String[] txo = i.split(">");
					txoutput += Integer.parseInt(txo[1]);
				}
				int fee = txinput - txoutput;
				// if fee already exists, get treeset and add new transaction
				if (transactions.containsKey(fee))
					transactions.get(fee).add(line);
				else {
					TreeSet<String> set = new TreeSet<>();
					set.add(line);
					transactions.put(fee, set);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static int getTransactionsCount(String line) {
		String[] tokens = line.split(";");
		String[] inputs = tokens[0].split(",");
		String[] outputs = tokens[1].split(",");
		return inputs.length + outputs.length;
	}

	public static String[] calculateBlockHash(String prevhash, String numtrans, String ts, String difficulty,
									String nonce, String concatRoot, BigInteger target) {
		Sha256Hash sha = new Sha256Hash();

		// keep checking new nonce until we reach target or reach max nonce value
		while(true) {
			if (nonce.equals(null))
				return null;

			StringBuilder bh = new StringBuilder();
			// build the block hash
			bh.append(prevhash).append(numtrans).append(ts).append(difficulty).append(nonce).append(concatRoot);
			// calculate hash
			String hash = sha.calculateHash(bh.toString());
			BigInteger bighash = new BigInteger(hash, 16);

			// if nonce satisifies H(block) < target
			if (bighash.compareTo(target) == -1) {
				return new String[] {hash, nonce};
			}
			// else get next nonce
			nonce = incrementStringNonce(nonce);
		}
	}

	public static String incrementStringNonce(String nonce) {
		char[] narray = nonce.toCharArray();
		int len = narray.length-1;

		// simple plus one algorithm
		for (int i=len; i>=0; i--) {
			if (narray[i] < MAX_NONCE) {
				narray[i]++;
				return String.valueOf(narray);
			} else {
				narray[i] = MIN_NONCE;
			}
		}
		// if we reach the max nonce value, 
		// i.e. passing in '~~~~' would result in '    ' for the next nonce
		if (String.valueOf(narray).equals("    "))
			return null;

		return String.valueOf(narray);
	}
}