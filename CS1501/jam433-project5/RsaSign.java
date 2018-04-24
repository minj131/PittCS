import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;

public class RsaSign {

    public RsaSign(String[] args) {

        if (args.length != 2) {
            System.out.println("Invalid command arguments. Please check format and try again.");

        } else {
            char flag = args[0].charAt(0);
            if(flag != 's' && flag != 'v') {
                System.out.println(args[0] + " : Invalid command type. Please use 's' to sign or 'v' to verify.");
                return;
            }

            File file = new File(args[1]);
            if (!file.exists()) {
                System.out.println(args[1] + " : File could not be found or does not exist.");
                return;
            }

            // Signs a file using RSA
            if (flag == 's')
                try {

                    // Create MD instance of SHA-256
                    byte[] data = Files.readAllBytes(file.toPath());
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(data);
                    byte[] digest = md.digest();

                    File f = new File("privkey.rsa");
                    if (!f.exists()) {
                        System.out.println("Private key not found.");
                        return;
                    }

                    // Takes in the private key and gets value of d and n
                    FileInputStream privKey = new FileInputStream(f);
                    ObjectInputStream readKey = new ObjectInputStream(privKey);
                    LargeInteger d = (LargeInteger) readKey.readObject();
                    LargeInteger n = (LargeInteger) readKey.readObject();
                    readKey.close();

                    // Creates a new object of the hash value
                    LargeInteger hash = new LargeInteger(digest);

                    BigInteger hashB = new BigInteger(hash.getVal());
                    BigInteger dB = new BigInteger(d.getVal());
                    BigInteger nB = new BigInteger(n.getVal());

                    BigInteger decrypt = hashB.modPow(dB, nB);

                    // Writes the signed file to a .sig file
                    FileOutputStream signed = new FileOutputStream(file.getName()+".sig");
                    ObjectOutputStream write = new ObjectOutputStream(signed);
                    write.writeObject(data);
                    write.writeObject(decrypt);
                    write.close();

                } catch (Exception e) {
                    System.out.println("Exception : " + e);
                    e.printStackTrace();
                }
            else
                // Verifies signature
                try {

                    // Checks if the .sig file exists
                    File sigFile = new File(file.getName() + ".sig");
                    if (!sigFile.exists()) {
                        System.out.println(sigFile.toString() + " : No signed file could be found. The signature could not be verified.");
                        return;
                    }

                    // Reads in data from sig file
                    FileInputStream signed = new FileInputStream(sigFile);
                    ObjectInputStream read = new ObjectInputStream(signed);
                    byte[] encryptData = (byte[]) read.readObject();
                    BigInteger decrypt = (BigInteger) read.readObject();
                    read.close();

                    // Creates MD instance of SHA-256
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(Files.readAllBytes(file.toPath()));

                    byte[] digest = md.digest();

                    // Takes encrypted data, hashes it, and stores it in a LargeInteger object
                    md.update(encryptData);
                    LargeInteger hash = new LargeInteger(digest);
                    BigInteger hashB = new BigInteger(hash.getVal());

                    // Now checks if public key exists
                    File pubFile = new File("pubkey.rsa");
                    if (!pubFile.exists()) {
                        System.out.println("Public key not found.");
                        return;
                    }

                    // Read in e and n values from public key
                    FileInputStream pubKey = new FileInputStream(pubFile);
                    ObjectInputStream readKey = new ObjectInputStream(pubKey);
                    LargeInteger e = (LargeInteger) readKey.readObject();
                    LargeInteger n = (LargeInteger) readKey.readObject();
                    readKey.close();

                    BigInteger eB = new BigInteger(e.getVal());
                    BigInteger nB = new BigInteger(n.getVal());

                    BigInteger encrypt = decrypt.modPow(eB, nB); // c = m^e % n

                    if (encrypt.compareTo(hashB) == 0)
                        System.out.println("SUCCESS : The signature is verified.");
                    else
                        System.out.println("WARNING : The signature is not valid.");

                } catch (Exception e) {
                    System.out.println("Exception : " + e);
                    e.printStackTrace();
                }
        }
    }
    public static void main(String[] args){new RsaSign(args);}
}
