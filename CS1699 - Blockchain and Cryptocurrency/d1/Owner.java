import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;

public class Owner {
    String name;
    PublicKey pk;
    PrivateKey sk;
    HashSet<String> wallet = new HashSet<>();

    public Owner(String name, String spk, String ssk) throws Exception {
        this.name = name;
        this.pk = SignVerify.loadPublicKey(spk);
        this.sk = SignVerify.loadPrivateKey(ssk);
    }

    public Owner(String name, String spk) throws Exception {
    	this.name = name;
    	this.pk = SignVerify.loadPublicKey(spk);
    }
}
