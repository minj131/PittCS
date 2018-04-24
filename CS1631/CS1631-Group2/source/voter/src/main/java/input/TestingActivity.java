package input;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tdr.R;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestingActivity extends Activity {

    Button b701, b702, b703, bTests, loadXML;
    TextView tc;
    boolean canContinue = true;

    static Set<String> msgs = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        b701 = (Button) findViewById(R.id.button701);
        b702 = (Button) findViewById(R.id.button702);
        b703 = (Button) findViewById(R.id.button703);
        bTests = (Button) findViewById(R.id.buttonRunTests);
        loadXML = (Button) findViewById(R.id.loadXML);

        tc = (TextView) findViewById(R.id.testConsole);
        tc.setMovementMethod(new ScrollingMovementMethod());

        //set button handlers
        b701.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.sendMessage(KVLLoader.get701A());
            }
        });
        b702.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.sendMessage(KVLLoader.get702A());
            }
        });
        b703.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.sendMessage(KVLLoader.get703());
            }
        });
        bTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestingActivity.this,"Running Tests...",Toast.LENGTH_SHORT).show();
                tc.append("Test Cases Result\n\n");

                // CHECKING CONNECTION
                tc.append("Checking SIS Server connection...\n");
                if (MainActivity.client != null && MainActivity.client.isSocketAlive())
                    tc.append("PASS -- Successfully connected to SIS Server.\n");
                else {
                    tc.append("FAIL -- Component is not connected to SIS Server\n");
                    canContinue = false;
                }

                if (canContinue) {
                    // CHECKING 999 KVL Message
                    tc.append("\nSending KVL Message 999...\n");
                    MainActivity.sendMessage(KVLLoader.get999());
                    if (msgs.isEmpty())
                        tc.append("PASS -- Sending invalid KVL message returns no response.\n");
                    else
                        tc.append("FAIL -- Sending invalid KVL message returns response.\n");

                    // Sending 701 results in 711
                    tc.append("\nSending KVL Message 701A...\n");
                    MainActivity.sendMessage(KVLLoader.get701A());
                    if (msgs.contains("711"))
                        tc.append("PASS -- Sending 701A results in successful 711 response.\n");
                    else
                        tc.append("FAIL -- Sending 701A results does not result in successful 711 response.\n");

                    // Sending 701 for poster out of range results invalid
                    tc.append("\nSending KVL Message 701B for poster out of range...\n");
                    MainActivity.sendMessage(KVLLoader.get701B());
                    if (msgs.contains("Invalid711"))
                        tc.append("PASS -- Sending 701B returns invalid vote.\n");
                    else
                        tc.append("FAIL -- Sending 701B does not return invalid vote.\n");

                    // Sending 701 for duplicate vote results invalid
                    tc.append("\nSending KVL Message 701A again for duplicate vote...\n");
                    MainActivity.sendMessage(KVLLoader.get701A());
                    if (msgs.contains("Invalid711"))
                        tc.append("PASS -- Sending 701A returns invalid vote.\n");
                    else
                        tc.append("FAIL -- Sending 701A does not return invalid vote.\n");

                    // Sending 702 does not crash component
                    tc.append("\nTrying to send KVL Message 702...");
                    try {
                        MainActivity.sendMessage(KVLLoader.get702A());
                        tc.append("\nPASS -- KVL Message 702 does not crash component.\n");

                        // Sending 702 with incorrect password
                        tc.append("\nSending KVL Message 702B with incorrect password...\n");
                        MainActivity.sendMessage(KVLLoader.get702B());
                        if (msgs.contains("Invalid712"))
                            tc.append("PASS -- Sending 702B with incorrect password does not return results.\n");
                        else
                            tc.append("FAIL -- Sending 702B with correct password returns results.\n");

                    } catch (Exception e) {
                        tc.append("\nFAIL -- KVL Message 702 does not work as intended.\n");
                    }

                    // Sending 702 with correct password results in pass
                    tc.append("\nSending KVL Message 702A with correct password...\n");
                    MainActivity.sendMessage(KVLLoader.get702A());
                    for (String s : msgs)
                        System.out.println(s);
                    if (msgs.contains("712"))
                        tc.append("PASS -- Sending 702A with correct password returns results.\n");
                    else
                        tc.append("FAIL -- Sending 702A with correct password does not return results.\n");

                    // Sending 703 returns test pair
                    tc.append("\nSending KVL Message 703...\n");
                    MainActivity.sendMessage(KVLLoader.get703());
                    if (msgs.contains("Test"))
                        tc.append("PASS -- Sending 703 results in test pairs");
                    else
                        tc.append("FAIL -- Sending 703 does not result in test pairs");
                } else {
                    tc.append("Aborting test procedure. Please connect to server and try again.\n");
                }
            }
        });
        loadXML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.client!=null && MainActivity.client.isSocketAlive()){
                    openFolder();
                }else{
                    Toast.makeText(TestingActivity.this, "Please connect to the server first.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Call a system service that enables users to choose a XML file.
    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/");
        intent.setDataAndType(uri, "*/*");
        try {
            if (ContextCompat.checkSelfPermission(TestingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                Toast.makeText(TestingActivity.this, "Please grant read permissions",Toast.LENGTH_LONG).show();
            }
            startActivityForResult(Intent.createChooser(intent, "Open XML configuration file"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(TestingActivity.this, "Please install a File Manager.",Toast.LENGTH_LONG).show();
        }
    }

    //Called after the user chooses a XML file.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(TestingActivity.this, "Upload successful.",Toast.LENGTH_LONG).show();
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = uri.toString();
                    Log.e("", "kvList================uri=========="+ path);
                    path = path.replace("file:///","/");
                    File myFile = new File(path);
                    //file:///storage/emulated/0/AlertMonitor.xml

                    KeyValueList kvList = XMLParser.getMessagesFromXML(myFile);
                    Log.e("", "kvList================xml=========="+kvList );

                    MainActivity.sendMessage(kvList);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
