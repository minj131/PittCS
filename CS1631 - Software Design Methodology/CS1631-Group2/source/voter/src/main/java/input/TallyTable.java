package input;

import android.content.Context;
import android.widget.Toast;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TallyTable {
    private HashMap<Integer, Integer> candidateTable = new HashMap();
    private HashSet<String> voterTable = new HashSet();

    public int addVote(String voterID, int candidateID, Context context) {

        if (candidateTable.get(candidateID) == null) {
            Toast.makeText(context, "Invalid candidate number attempt. Vote rejected. Please try again.", Toast.LENGTH_SHORT).show();
            return -1;
        }

        if (voterTable.contains(voterID)) {
            Toast.makeText(context, "This number has already voted.", Toast.LENGTH_SHORT).show();
            return 0;
        } else {
            voterTable.add(voterID);
        }

        Toast.makeText(context, "Vote Successful.", Toast.LENGTH_SHORT).show();
        for (Entry entry : candidateTable.entrySet()) {
            if (entry.getKey().equals(candidateID)) {
                this.candidateTable.put(candidateID, ((Integer) entry.getValue()) + 1);
            }
        }
        return 1;
    }

    public void setCandidateList(int posterNum) {
        if (candidateTable.get(posterNum) == null) {
            Candidate candidate = new Candidate(posterNum);
            candidateTable.put(candidate.getCandidateID(), candidate.getNumVotes());
        }
    }

    public HashMap<Integer, Integer> sort(Map<Integer, Integer> unsortMap, final boolean order) {

        List<Entry<Integer, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public HashMap<Integer, Integer> getCandidateTable() {
        return candidateTable;
    }
}