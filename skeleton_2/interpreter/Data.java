package interpreter;

public class Data {

    final int rawType;
    final boolean isMutable;
    public final long value;

    public Data(int rawType, boolean isMutable, long value) {
        this.rawType = rawType;
        this.isMutable = isMutable;
        this.value = value;
    }

    @Override
    public String toString() {

        return "" + this.value;
    }
}
