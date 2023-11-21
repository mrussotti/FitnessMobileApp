package ast;

public class INT extends Q {

    public Long value;

    public INT(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString(){
        return value.toString();
    }

}
