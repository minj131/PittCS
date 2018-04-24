package tdr.sisprjremote;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class TestingActivity extends AppCompatActivity {

    static Set<String> msgs = new HashSet<>();
    TextView testsConsole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        testsConsole = (TextView) findViewById(R.id.testsConsole);
        testsConsole.setMovementMethod(new ScrollingMovementMethod());

        Toast.makeText(this,"Running Tests...",Toast.LENGTH_LONG).show();
        testsConsole.append("Test Cases Result\n");

        testsConsole.append("\nChecking SIS Server Connections...\n");
        if (MainActivity.client.isSocketAlive()) {
            testsConsole.append("PASS -- Successfully connected to SIS Server.\n");
        } else {
            testsConsole.append("FAIL -- Unsuccessfully connected to SIS Server. \n");
        }

        testsConsole.append("\nChecking KVL Message 999...\n");
        MainActivity.sentMessage(KVLLoader.get999());
        if (msgs.isEmpty()) {
            testsConsole.append("PASS -- Sending invalid message returns no response.\n");
        } else {
            testsConsole.append("FAIL -- Sending invalid message returns response.\n");
        }

        testsConsole.append("\nChecking KVL Message 701s...\n");
        MainActivity.sentMessage(KVLLoader.get701A());
        if (msgs.contains("Valid701\n")) {
            testsConsole.append("PASS -- Sending 701A results in 711 response.\n");
        } else {
            testsConsole.append("FAIL -- Sending 701A does not result in 711 response.\n");
        }
        MainActivity.sentMessage(KVLLoader.get701B());
        if (msgs.contains("Invalid701\n")) {
            testsConsole.append("PASS -- Sending 701B results in invalid. Poster out of range.\n");
        } else {
            testsConsole.append("FAIL -- Sending 701B does not result in invalid. Poster out of range.\n");
        }
        MainActivity.sentMessage(KVLLoader.get701A());
        if (msgs.contains("Invalid701\n")) {
            testsConsole.append("PASS -- Sending 701A results in invalid. Duplicate vote.\n");
        } else {
            testsConsole.append("FAIL -- Sending 701A does not result in invalid. Duplicate vote.\n");
        }

        testsConsole.append("\nChecking KVL Message 702s...\n");
        try {
            MainActivity.sentMessage(KVLLoader.get702A());
            testsConsole.append("PASS -- Sending 702 with no votes does not crash component.\n");
        } catch (Exception e) {
            testsConsole.append("FAIL -- Sending 702 with no votes crashes component.\n");
        }

        MainActivity.sentMessage(KVLLoader.get702A());
        if (msgs.contains("712")) {
            testsConsole.append("PASS -- Sending 702A with correct password returns valid.\n");
        } else {
            testsConsole.append("FAIL -- Sending 702A with correct password does not return invalid.\n");
        }

        MainActivity.sentMessage(KVLLoader.get702B());
        if (msgs.contains("Invalid712")) {
            testsConsole.append("PASS -- Sending 702B with wrong password results in invalid.\n");
        } else {
            testsConsole.append("FAIL -- Sending 702B with wrong password does not result in invalid.\n");
        }

        testsConsole.append("\nChecking KVL Message 703...\n");
        MainActivity.sentMessage(KVLLoader.get703());
        if (msgs.contains("703Test")) {
            testsConsole.append("PASS -- Sending 703 results in test pair.");
        } else {
            testsConsole.append("FAIL -- Sending 703 does not result in test pairs.");
        }

    }
}
