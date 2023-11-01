int isList(Q val) {
    if (isAtom(val) != 0)
      return isNil(val);
    return isList(right((Ref)val));
}

Ref append(Ref list1, Ref list2) {
    if (isNil(list1) != 0) {
        return list2;
    }
    return (left(list1) . append((Ref)right(list1), list2));
}

Ref reverse(Ref list) {
    if (isNil(list) != 0) {
        return nil;
    }
    return append(reverse((Ref)right(list)), left(list) . nil);
}

int isSorted(Ref list) {
    if (isNil(list) != 0 || isNil(right(list)) != 0) {
        return 1;
    }
    if (length((Ref)left(list)) <= length((Ref)left((Ref)right(list)))) {
        return 1;
    }
    return 0;
}

int length(Ref list) {
    if (isNil(list) != 0) {
        return 0;
    }
    return length((Ref)right(list)) + 1;
}

/* Problem 5:

Immutable Quandary without if statements is NOT Turing complete. Without if statements, there's no way to affect the number of expression evaluations in an execution -- it'll be the same for every execution, yet it needs to be arbitrarily large (i.e., unbounded) to handle all possible inputs.

Caveat: An interesting question that I (Mike) haven't fully considered is whether, even without if statements, Quandary *with first-class functions* is Turing complete.

Immutable Quandary without calls is NOT Turing complete. Without calls, there's no way to have a static expression be evaluated multiple times, which is needed for unbounded computation.

*/
