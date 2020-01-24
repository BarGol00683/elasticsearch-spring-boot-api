package pl.postgresql_indexer.indexer.domain;

public class EpochInstant {

    private Long epochSecond;

    private Long nano;

    public EpochInstant() {

    }

    public EpochInstant(Long epochSecond, Long nano) {
        this.epochSecond = epochSecond;
        this.nano = nano;
    }

    public Long getEpochSecond() {
        return epochSecond;
    }

    public void setEpochSecond(Long epochSecond) {
        this.epochSecond = epochSecond;
    }

    public Long getNano() {
        return nano;
    }

    public void setNano(Long nano) {
        this.nano = nano;
    }
}
