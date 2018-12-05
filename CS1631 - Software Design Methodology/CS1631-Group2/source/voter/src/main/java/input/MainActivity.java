package input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tdr.R;

import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    public static final String TAG = "Mobile Voting Component";

    private static Button connectToServerButton;
    private static Button registerToServerButton;
    private static Button enableVoting;
    private static Button showResults;
    private static Button setPosters;
    private static Button runTests;

    private EditText serverIp;
    private EditText serverPort;

    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static final String SENDER = "VoterComponent";
    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED = "Disconnect";
    private static final String SCOPE = "SIS.Scope1";
    private static final String VOTE = "Enable Voting";

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    private static TallyTable tallyTable;

    private static boolean isVotingEnabled;

    public static HashMap<Integer, Integer> results;

    private static String passcode = "1631";
    private String posterList = "";

    private static SmsManager smsMan = SmsManager.getDefault();

    String data = null;//"EMG:333ECG:111V";

    private static Context context;

    static Handler callbacks = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str;
            String[] strs;
            switch (msg.what) {
                case CONNECTED:
                    registerToServerButton.setText(REGISTERED);
                    Log.e(TAG, "===============================================================CONNECTED" );
                    break;
                case DISCONNECTED:
                    connectToServerButton.setText("Connect");
                    Log.e(TAG, "===============================================================DISCONNECTED" );
                    break;
                case MESSAGE_RECEIVED:
                    System.out.println("RECEIVING MESSAGE");
                    KeyValueList recv = (KeyValueList)msg.obj;
                    str = recv.toString();
                    System.out.println(str);
                    parseKVL(recv, getAppContext());
                    messageReceivedListText.append(str+"********************\n");
                    final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                    if (scrollAmount > 0)
                        messageReceivedListText.scrollTo(0, scrollAmount);
                    else
                        messageReceivedListText.scrollTo(0, 0);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client!=null){
            client.killThread();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        data = this.getIntent().getStringExtra("data");
        if(data!=null){
            Log.e(TAG, "Received an intent data: " + data );
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        MainActivity.context = getApplicationContext();

        SmsReceiver rec = new SmsReceiver();
        IntentFilter intentFilter = new IntentFilter("MainActivity.intent.MAIN");

        registerReceiver(rec, intentFilter);

        isVotingEnabled = false;
        connectToServerButton = (Button) findViewById(R.id.connectToServer);

        registerToServerButton = (Button) findViewById(R.id.registerToServerButton);
        enableVoting = (Button) findViewById(R.id.toggleVotingButton);
        showResults = (Button) findViewById(R.id.viewResultsButton);

        serverIp = (EditText) findViewById(R.id.serverIp);
        serverPort = (EditText) findViewById(R.id.serverPort);

        setPosters = (Button) findViewById(R.id.setPosters);

        runTests = (Button) findViewById(R.id.runTestsButton);

        messageReceivedListText = (TextView) findViewById(R.id.messageReceivedListText);
        messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());

        registerToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client!=null && client.isSocketAlive() && registerToServerButton.getText().toString().equalsIgnoreCase(REGISTERED)){
                    Toast.makeText(MainActivity.this,"Already registered.",Toast.LENGTH_SHORT).show();
                }else{
                    client = new ComponentSocket(serverIp.getText().toString(), Integer.parseInt(serverPort.getText().toString()),callbacks);
                    client.start();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            KeyValueList list = generateRegisterMessage();
                            client.setMessage(list);
                        }
                    }, 500);
                }
            }
        });
        connectToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(MainActivity.TAG, "Sending connectToServerButton.1" );
                if(connectToServerButton.getText().toString().equalsIgnoreCase(DISCOONECTED)){
                    Log.e(MainActivity.TAG, "Sending connectToServerButton.2" );
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            client.killThread();
                        }
                    }, 100);
                    connectToServerButton.setText("Connect");
                }else{
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            KeyValueList list = generateConnectMessage();
                            client.setMessage(list);
                        }
                    }, 100);
                    connectToServerButton.setText(DISCOONECTED);
                }
            }
        });
        setPosters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Input Poster Numbers (ex. '1,2,5,6,7,8')");

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (tallyTable == null) {
                            tallyTable = new TallyTable();
                        }
                        posterList = input.getText().toString();
                        String[] posterTokens = posterList.split(",");
                        for (String s : posterTokens) {
                            tallyTable.setCandidateList(Integer.parseInt(s));
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        enableVoting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isVotingEnabled) {
                    isVotingEnabled = true;
                    enableVoting.setText("Close Voting");
                } else {
                    isVotingEnabled = false;
                    enableVoting.setText("Enable Voting");
                    if (tallyTable != null) {
                        results = tallyTable.getCandidateTable();
                        results = tallyTable.sort(results, false);

                        StringBuilder sb = new StringBuilder();
                        int count = 0;

                        for (Map.Entry e : results.entrySet()) {
                            if (count == 3) {
                                System.out.println(sb.toString());
                                String phone = "+12674757720";
                                smsMan.sendTextMessage(phone, null, sb.toString(), null, null);
                                return;
                            }
                            int candidate = (int) e.getKey();
                            int votes = (int) e.getValue();
                            sb.append("Candidate: ").append(candidate).append(" | Votes: ").append(votes).append("\n");
                            count++;
                        }
                    }
                }
            }
        });
        showResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tallyTable != null) {
                    results = tallyTable.getCandidateTable();
                    results = tallyTable.sort(results, false);
                    showResults();
                } else {
                    Toast.makeText(MainActivity.this,"Candidate table is empty! Set poster numbers first!",Toast.LENGTH_LONG).show();
                }
            }
        });
        runTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTests();
            }
        });
    }
    KeyValueList generateRegisterMessage()
    {
        KeyValueList list = new KeyValueList();

        list.putPair("Scope", "SIS.Scope1");

        list.putPair("MessageType", "Register");

        list.putPair("Sender", "VoterComponent");

        list.putPair("Role", "Basic");

        list.putPair("Name", "VoterComponent");
        return list;
    }
    KeyValueList generateConnectMessage() {
        KeyValueList list = new KeyValueList();

        list.putPair("Scope", "SIS.Scope1");

        list.putPair("MessageType", "Connect");

        list.putPair("Sender", "VoterComponent");

        list.putPair("Role", "Basic");

        list.putPair("Name", "VoterComponent");
        return list;
    }
    KeyValueList generateReadingMessage() {
        KeyValueList list = new KeyValueList();

        list.putPair("Scope", "SIS.Scope1");

        list.putPair("MessageType", "Reading");

        list.putPair("Sender", "VoterComponent");

        list.putPair("Role", "Basic");


        list.putPair("Broadcast", "True");
        list.putPair("Direction", "Up");
        list.putPair("Receiver", "Uploader");

        list.putPair("Data_BP", "unavailable");

        if ((data.contains("EMG:")) && (data.contains("ECG:"))) {
            int index = data.indexOf("EMG:");
            int ecgindex = data.indexOf("ECG:");
            int vindex = data.indexOf("V");
            System.out.println("index:" + index);
            System.out.println("ecgindex:" + ecgindex);
            String emg = data.substring(index + 4, ecgindex);
            String ecg = data.substring(ecgindex + 4, vindex);
            System.out.println("emg:" + emg);
            System.out.println("ecg:" + ecg);

            list.putPair("Data_EMG", emg);
            list.putPair("Data_ECG", ecg);
        }

        list.putPair("Data_Pulse", "unavailable");

        long curr_time = System.currentTimeMillis();
        list.putPair("Data_Date", String.valueOf(curr_time));
        return list;
    }

    public static void parseVote(KeyValueList recv, String phone, Context context) {
        scrollText(recv);
        if (isVotingEnabled) {
            if (tallyTable == null) {
                tallyTable = new TallyTable();
            }

            if ((recv.getValue("VoterID") == null) || (recv.getValue("CandidateID") == null) || (recv.getValue("CandidateID").equals(""))) {
                KeyValueList initSend = new KeyValueList();

                initSend.putPair("Scope", "SIS.Scope1");
                initSend.putPair("MessageType", "Reading");
                initSend.putPair("Sender", "InputProcessor");
                initSend.putPair("MsgID", "711");
                initSend.putPair("Status", "Invalid");

                smsMan.sendTextMessage(phone, null, "Invalid Vote", null, null);
            }


            String voterID = recv.getValue("VoterID");
            int candidateID;

            try {
                candidateID = Integer.parseInt(recv.getValue("CandidateID"));
            } catch(Exception e) {
                return;
            }
            int voteStatus = tallyTable.addVote(voterID, candidateID, context);
            KeyValueList initSend = new KeyValueList();

            initSend.putPair("Scope", "SIS.Scope1");
            initSend.putPair("MessageType", "Reading");
            initSend.putPair("Sender", "InputProcessor");
            initSend.putPair("MsgID", "711");

            if (voteStatus == 1) {
                initSend.putPair("Status", "Valid");
                smsMan.sendTextMessage(phone, null, "Vote Accepted", null, null);
            } else if (voteStatus == 0) {
                initSend.putPair("Status", "Duplicate");
                smsMan.sendTextMessage(phone, null, "Duplicate Vote", null, null);
            } else if (voteStatus == -1) {
                initSend.putPair("Status", "Invalid");
                smsMan.sendTextMessage(phone, null, "Poster number not found. Vote rejected.", null, null);
            }
            Log.e("Mobile Voting Component", "Sending 711...");
        }
    }

    // When receiving 701 KVL
    public static void parseVoteRemote(KeyValueList recv, Context context) {
        scrollText(recv);
        if (isVotingEnabled) {
            if (tallyTable == null) {
                tallyTable = new TallyTable();
            }

            if (recv.getValue("VoterID") == null || recv.getValue("CandidateID") == null
                    || recv.getValue("CandidateID").equals("")) {
                KeyValueList initSend = new KeyValueList();

                initSend.putPair("Scope", SCOPE);
                initSend.putPair("MessageType", "Reading");
                initSend.putPair("Sender", "InputProcessor");
                initSend.putPair("MsgID", "711");
                initSend.putPair("Status", "Invalid");
                if (TestingActivity.msgs != null)
                    TestingActivity.msgs.add("Invalid711");
                client.setMessage(initSend);
            }

            String voterID = recv.getValue("VoterID");
            int candidateID = Integer.parseInt(recv.getValue("CandidateID"));

            int accepted = tallyTable.addVote(voterID, candidateID, context);
            KeyValueList send = new KeyValueList();

            send.putPair("Scope", SCOPE);
            send.putPair("MessageType", "Reading");
            send.putPair("Sender", "InputProcessor");
            send.putPair("MsgID", "711");
            if (accepted == 1) {
                send.putPair("Status", "Valid");
                if (TestingActivity.msgs != null)
                    TestingActivity.msgs.add("711");
            }
            else if (accepted == -1){
                if (TestingActivity.msgs != null)
                    TestingActivity.msgs.add("Invalid711");
                send.putPair("Status", "New Candidate. Valid.");
            }
            else if (accepted == 0) {
                if (TestingActivity.msgs != null)
                    TestingActivity.msgs.add("Invalid711");
                send.putPair("Status", "Duplicate");
            }
            Log.e(TAG, "Sending 711");
            client.setMessage(send);
        }
    }

    public static void parseKVL(KeyValueList recv, Context context) {
        if (recv.getValue("MsgID") != null) {
            if (recv.getValue("MsgID").equals("701")) {
                Log.e(TAG, "Received 701");
                parseVoteRemote(recv, context);
            } else if (recv.getValue("MsgID").equals("702")) {
                scrollText(recv);
                Log.e(TAG, "Received 702");
                if (!recv.getValue("Passcode").equals(passcode)) {
                    KeyValueList initSend = new KeyValueList();
                    initSend.putPair("Scope", SCOPE);
                    initSend.putPair("MessageType", "Reading");
                    initSend.putPair("Sender", "InputProcessor");
                    initSend.putPair("MsgID", "712");
                    initSend.putPair("RankedReport", "null");
                    Log.e(TAG, "Sending 712 Error");
                    if (TestingActivity.msgs != null)
                        TestingActivity.msgs.add("Invalid712");
                    client.setMessage(initSend);
                } else {
                    HashMap<Integer, Integer> results;
                    StringBuilder stringResults = new StringBuilder();

                    results = tallyTable.getCandidateTable();
                    results = tallyTable.sort(results, false);

                    int n = Integer.parseInt(recv.getValue("N"));
                    int count = 0;

                    for (Map.Entry entry : results.entrySet()) {
                        int candidate = (int) entry.getKey();
                        int votes = (int) entry.getValue();
                        stringResults.append(String.valueOf(candidate)).append("\t | \t").append(String.valueOf(votes)).append("\n");
                        count++;
                        if (count == n) break;
                    }

                    KeyValueList initSend = new KeyValueList();
                    initSend.putPair("Scope", SCOPE);
                    initSend.putPair("Sender", "InputProcessor");
                    initSend.putPair("MessageType", "Reading");
                    initSend.putPair("MsgID", "712");
                    initSend.putPair("RankedReport", String.valueOf(stringResults));

                    if (TestingActivity.msgs != null)
                        TestingActivity.msgs.add("712");
                    client.setMessage(initSend);
                }
            } else if (recv.getValue("MsgID").equals("703")) {
                scrollText(recv);
                Log.e(TAG, "Received 703");
                tallyTable = new TallyTable();

                String posters = recv.getValue("CandidateList");
                String passcode = recv.getValue("Passcode");
                String[] list = posters.split(",");

                for (String s : list) {
                    tallyTable.setCandidateList(Integer.parseInt(s));
                }

                KeyValueList send = new KeyValueList();
                send.putPair("Scope", SCOPE);
                send.putPair("MessageType", "Reading");
                send.putPair("MsgID", "26");
                send.putPair("Test", "Test");
                if (TestingActivity.msgs != null)
                    TestingActivity.msgs.add("Test");
                client.setMessage(send);
            }
        }
    }

    public void runTests() {
        Intent intent = new Intent(this, TestingActivity.class);
        startActivity(intent);
    }

    public void showResults() {
        Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

    private static void scrollText(KeyValueList kv) {
        if (messageReceivedListText != null) {
            messageReceivedListText.append(kv + "********************\n");
            int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
            if (scrollAmount > 0) {
                messageReceivedListText.scrollTo(0, scrollAmount);
            } else {
                messageReceivedListText.scrollTo(0, 0);
            }
        }
    }

    public static void sendMessage(KeyValueList kvl) {
        if (client.isSocketAlive())
            parseKVL(kvl, getAppContext());
        client.setMessage(kvl);
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }
}
