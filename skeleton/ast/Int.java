package ast;

public class INT extends Q {

    public Long value;

    public INT(Long value) {
        this.value = value;
    }

    public int getInt() {
        return value;
    }

}
