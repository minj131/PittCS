package tdr.sisprjremote;

public class KVLLoader {
    public static KeyValueList get701A() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "701");
        ret.putPair("MessageType", "Reading");
        ret.putPair("VoterID", "2674757720");
        ret.putPair("CandidateID", "3");
        ret.putPair("Test701A", "Test701A");
        if (TestingActivity.msgs != null)
            TestingActivity.msgs.add("Test701A");
        return ret;
    }

    public static KeyValueList get701B() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "701");
        ret.putPair("MessageType", "Reading");
        ret.putPair("VoterID", "2674757721");
        ret.putPair("CandidateID", "99");
        ret.putPair("Test701B", "Test701B");
        if (TestingActivity.msgs != null)
            TestingActivity.msgs.add("Test701B");
        return ret;
    }

    public static KeyValueList get702A() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "702");
        ret.putPair("MessageType", "Reading");
        ret.putPair("Passcode", "1631");
        ret.putPair("N", "2");
        ret.putPair("Test702", "Test702");
        if (TestingActivity.msgs != null)
            TestingActivity.msgs.add("Test702");
        return ret;
    }

    public static KeyValueList get702B() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "702");
        ret.putPair("MessageType", "Reading");
        ret.putPair("Passcode", "9999");
        ret.putPair("N", "2");
        ret.putPair("Test702", "Test702");
        if (TestingActivity.msgs != null)
            TestingActivity.msgs.add("Test702");
        return ret;
    }

    public static KeyValueList get703() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "703");
        ret.putPair("MessageType", "Reading");
        //ret.putPair("Sender", "VotingComponent");
        ret.putPair("CandidateList", "2;3;4");
        ret.putPair("Passcode", "1631");
        ret.putPair("Test703", "Test703");
        if (TestingActivity.msgs != null)
            TestingActivity.msgs.add("Test703");
        return ret;
    }

    public static KeyValueList get999() {
        KeyValueList ret = new KeyValueList();
        ret.putPair("Scope", "SIS");
        ret.putPair("MsgID", "999");
        ret.putPair("MessageType", "Reading");

        return ret;
    }
}
