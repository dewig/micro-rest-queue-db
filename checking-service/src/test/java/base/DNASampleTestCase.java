package base;

import java.util.List;

public class DNASampleTestCase {

    private List<String> dna;
    private boolean expected;

    public List<String> getDna() {
        return dna;
    }

    public void setDna(List<String> dna) {
        this.dna = dna;
    }

    public boolean isExpected() {
        return expected;
    }

    public void setExpected(boolean expected) {
        this.expected = expected;
    }
}
