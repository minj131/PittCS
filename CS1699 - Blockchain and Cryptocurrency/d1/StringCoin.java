import com.sun.media.sound.InvalidDataException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class StringCoin {
    // name -> owner
    HashMap<String, Owner> owners = new HashMap<>();
    // coinid -> pk
    TreeMap<String, PublicKey> coins = new TreeMap<>();

    StringCoin(String file) throws Exception {
        createOwners();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String curr = null;
            String prev;
            String line;
            int numLine = 1;
            while ((line = br.readLine()) != null) {
                prev = curr;
                curr = line;
                String[] tokens = curr.split(",");
                assert tokens.length == 5 : "Invalid token length at line " + numLine;
                verify(prev, tokens);
                numLine++;
            }
        } catch (Exception e) {
            System.out.println("Caught exception " + e);
            e.printStackTrace();
            System.out.println("Quitting...");
            System.exit(0);
        }
        print(coins);
    }

    private boolean verify(String line, String[] tokens) throws Exception {
        String prev = tokens[0];
        // 0 = create; 1 = transfer
        int type = (tokens[1].equals("CREATE")) ? 0 : 1;

        // If genesis block
        if (prev.equals("0")) {
            assert type == 0 : "Genesis block must create a coin";
            createCoin(tokens);
        // Not a genesis block
        } else {
            // Verify previous hash
            String hash = Sha256Hash.calculateHash(line);
            if (prev.equals(hash)) {
                // if we are creating a coin
                if (type == 0) {
                    createCoin(tokens);
                // transfer coin
                } else {
                    transferCoin(tokens);
                }
            } else throw new InvalidDataException("Invalid hash: " + hash + " does not match " + tokens[0]);
        }
        return true;
    }

    private void createCoin(String[] tokens) throws Exception {
        String coinID = tokens[2];
        // get the byte array of the signed coin
        byte[] b = SignVerify.convertHexToBytes(tokens[3]);
        // if the signature of the coin is verified
        if (SignVerify.verify(coinID, b, owners.get("Bill").pk)) {
            // gets the signature of the message to byte array
            byte[] sigToBytes = SignVerify.convertHexToBytes(tokens[4]);
            StringBuilder sig = new StringBuilder(tokens[0]);
            sig.append(",").append(tokens[1]).append(",").append(tokens[2]).append(",").append(tokens[3]);
            // if the signature of the line is verified
            if (SignVerify.verify(sig.toString(), sigToBytes, owners.get("Bill").pk)) {
                // if coin exists
                if (coins.containsKey(coinID)) {
                	throw new InvalidDataException("Invalid coin creation: " + coinID + " already exists");
            	// add coin to total existing
                } else {
                	coins.put(coinID, owners.get("Bill").pk);
                }
                // add the coin to the wallet, checks if already created
                if (!owners.get("Bill").wallet.add(coinID)) {
                	throw new InvalidDataException("Invalid transaction: " + coinID + " exists in wallet");
                }
            } else throw new InvalidDataException("Invalid line " + sig.toString());
        } else throw new InvalidDataException("Invalid coin " + coinID);
    }

    private void transferCoin(String[] tokens) throws Exception {
        // gets the signature of the coin
        String coinID = tokens[2];
        PublicKey recipient = SignVerify.loadPublicKey(tokens[3]);
        PublicKey owner = coins.get(coinID);
        // if owner doesn't exist
        if (!owners.containsKey(tokens[3])) {
        	addOwner(tokens[3], tokens[3]);
        }
        // Check message signature
        byte[] sigToBytes = SignVerify.convertHexToBytes(tokens[4]);
        StringBuilder sig = new StringBuilder(tokens[0]);
        sig.append(",").append(tokens[1]).append(",").append(tokens[2]).append(",").append(tokens[3]);

        if (SignVerify.verify(sig.toString(), sigToBytes, owner)) {
            if (coins.containsKey(coinID)) {
                // remove coin from owner
                for (Map.Entry<String, Owner> e : owners.entrySet()) {
                    if (owner.equals(e.getValue().pk)) {
                        coins.remove(coinID);
                        e.getValue().wallet.remove(coinID);
                    }
                }
                // add coin to recipient
                for (Map.Entry<String, Owner> e : owners.entrySet()) {
                    if (recipient.equals(e.getValue().pk)) {
                        coins.put(coinID, recipient);
                        e.getValue().wallet.add(coinID);
                    }
                }
            } else throw new InvalidDataException("Invalid transfer: " + coinID + " does not exist");
        } else throw new InvalidDataException("Invalid line " + sig.toString());
    }

    private void createOwners() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("keypairs.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String pk = br.readLine();
                String sk = br.readLine();
                Owner owner = new Owner(line, pk, sk);
                owners.put(owner.name, owner);
            }
        } catch (IOException ioe) {
            System.out.println("Error parsing keypairs text file.");
            ioe.printStackTrace();
        }
    }

    // in the case the owner doesn't exist, create an owner with pk as identity
    private void addOwner(String name, String pk) {
    	String sk = null;
    	try {
    		Owner owner = new Owner(name, pk);
    		owners.put(pk, owner);
    	} catch (Exception e) {
            System.out.println("Error parsing keypairs text file.");
            e.printStackTrace();
        }
    }

    private void print(TreeMap<String, PublicKey> coins) {
        for (Map.Entry<String, PublicKey> e : coins.entrySet()) {
            String pk = SignVerify.convertBytesToHexString(e.getValue().getEncoded());
            System.out.println("Coin " + e.getKey() + " / Owner = " + pk);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Incorrect Argument\nUsage: `java StringCoin *blockchain_file_name*`");
            System.exit(0);
        }
        new StringCoin(args[0]);
    }
}