
mutable int doLeftInc(Ref counters, int b) {
    mutable int i = 0;
    while (i < 10000) {
        acq(counters);
        int value = (int)left(counters);
        rel(counters);

        acq(counters);
        setLeft(counters, value + 1);
        rel(counters);
        i = i + 1;
    }
    return 0;
}

mutable Q main(int arg) {
    mutable Ref counters = 0 . 0;
    int dummy = [ doLeftInc(counters, 1) + doLeftInc(counters, 2) ];
    print(dummy);
    int value = (int)left(counters);
    print(value);
    if (value >= 20000) return value;
    return nil;
}
