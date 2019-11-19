package base;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Stat {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private long countMutantDna;
    private long countHumanDna;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getCountMutantDna() {
        return countMutantDna;
    }

    public void setCountMutantDna(long countMutantDna) {
        this.countMutantDna = countMutantDna;
    }

    public long getCountHumanDna() {
        return countHumanDna;
    }

    public void setCountHumanDna(long countHumanDna) {
        this.countHumanDna = countHumanDna;
    }
}
