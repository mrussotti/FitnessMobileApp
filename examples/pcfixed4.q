mutable Q main(int arg) {
    Ref flag = 0 . nil; /* the left field of the object will represent a flag */
    Ref data = 0 . nil; /* the left field of the object will represent producer-consumer data */
    return [ consume(flag, data) . produce(flag, data) ];
}

mutable Q produce(Ref flag, Ref data) {
    acq(flag);
    setLeft(data, 42);
    setLeft(flag, 1);
    rel(flag);
    return nil;
}

mutable Q consume(Ref flag, Ref data) {
    while (getFlagSync(flag) == 0) { /* empty */ }
    print(1);
    int d = (int)left(data);
    print(2);
    return d;
}

mutable int getFlagSync(Ref flag) {
    acq(flag);
    int f = (int)left(flag);
    rel(flag);
    return f;
}
