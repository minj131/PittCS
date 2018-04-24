import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Random;

public class RsaKeyGen {

    // Setter for a new LargeInt object with value of 1, 2, 3
    final static byte[] ONE = {(byte) 1};
    final static byte[] TWO = {(byte) 2};
    final static byte[] THREE = {(byte) 3};

    public static void main(String[] args) {
        Random rng = new Random();

        // Generates p and q
        LargeInteger p = new LargeInteger(256, rng);
        LargeInteger q = new LargeInteger(256, rng);

//        System.out.println("p: " + p.toString() + "\n" + "q: " + q.toString());

        //n = pq results in a 512-bit number
        LargeInteger n = p.multiply(q);

//        System.out.println("n: " + n.toString());

        LargeInteger pPhi = p.subtract(new LargeInteger(ONE));  // p-1
        LargeInteger qPhi = q.subtract(new LargeInteger(ONE));  // q-1

//        System.out.println("pPhi: " + pPhi.toString() + "\n" + "qPhi: " + qPhi.toString());

        LargeInteger phi = pPhi.multiply(qPhi); // phi(n)=(p-1)*(q-1)
//        System.out.println("phi: " + phi.toString());

        // Sets initial e value to prime 3, then determines if GCD to phi is 1
        // While it is not, finds next prime and checks GCD to phi
        LargeInteger e = new LargeInteger(THREE);
        while (!phi.XGCD(e)[0].isOne())
            e = e.add(new LargeInteger(TWO));

        BigInteger eB = new BigInteger(e.getVal());
        BigInteger phiB = new BigInteger(phi.getVal());
        BigInteger dB = eB.modInverse(phiB);

        LargeInteger d = new LargeInteger(dB.toByteArray());

//        System.out.println("d: " + d.toString());

        try {
            // Writes e and n to public key
            FileOutputStream pubKey = new FileOutputStream("pubkey.rsa");
            ObjectOutputStream pubWrite = new ObjectOutputStream(pubKey);
            pubWrite.writeObject(e);
            pubWrite.writeObject(n);
            pubWrite.close();

            // Writes d and n to private key
            FileOutputStream privKey = new FileOutputStream("privkey.rsa");
            ObjectOutputStream privWrite = new ObjectOutputStream(privKey);
            privWrite.writeObject(d);
            privWrite.writeObject(n);
            privWrite.close();

        } catch(IOException ex) {
            System.out.println("Exception : " + ex.toString());
            ex.printStackTrace();
        }
    }
}
