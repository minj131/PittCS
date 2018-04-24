package input;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

//handle sms messages
public class SmsReceiver extends BroadcastReceiver {


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String sender = smsMessage.getOriginatingAddress();
                String message = smsMessage.getMessageBody();

                if (message.length() >= 1) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    try {
                        Integer.parseInt(message);
                    } catch (Exception e) {
                        return;
                    }
                    KeyValueList temp = new KeyValueList();
                    temp.putPair("MsgID", "701");
                    temp.putPair("VoterID", sender);
                    temp.putPair("CandidateID", message);
                    Log.d("Sms Received", temp.toString());

                    MainActivity.parseVote(temp, sender, context);
                }
            }
        }
    }
}