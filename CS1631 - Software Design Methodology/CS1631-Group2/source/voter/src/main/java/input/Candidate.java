package input;

public class Candidate {

    private int candidateID;
    private int numVotes;

    public Candidate(int candidateID) {
        this.candidateID = candidateID;
        numVotes = 0;
    }

    public void addVote() {
        numVotes++;
    }

    public void resetVotes() {
        numVotes = 0;
    }

    public int getCandidateID() {
        return candidateID;
    }

    public int getNumVotes() {
        return numVotes;
    }
}
