package input;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.tdr.R;

import java.util.HashMap;
import java.util.Map;

public class ResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        HashMap<Integer, Integer> results = MainActivity.results;

        TableRow heading = new TableRow(this);

        TextView label_candidate = new TextView(this);
        label_candidate.setText("CANDIDATE");
        label_candidate.setPadding(5, 25, 300, 5);
        heading.addView(label_candidate);

        TextView label_votes = new TextView(this);
        label_votes.setText("VOTES");
        label_votes.setPadding(5, 25, 5, 5);
        heading.addView(label_votes); // add the column to the table row here

        TableLayout mTable = (TableLayout) findViewById(R.id.tally_table);
        mTable.addView(heading);

        for (Map.Entry e : results.entrySet()) {
            int candidate = (int) e.getKey();
            int votes = (int) e.getValue();

            TableRow row = new TableRow(this);
            TextView candView = new TextView(this);
            TextView voteView = new TextView(this);

            candView.setText(String.valueOf(candidate));
            candView.setPadding(5,0,0,0);
            voteView.setText(String.valueOf(votes));
            voteView.setPadding(5,0,0,0);

            row.addView(candView);
            row.addView(voteView);
            mTable.addView(row);
        }
    }
}
