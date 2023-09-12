/* To run: ./ref/quandary test.q 5 */

Q main (int x){
    Ref list = rigoberto(x);
    Ref list2 = squareList(list);
    return list2;
}

Ref rigoberto (int x){
    if (x == 0) return nil;
    return x . rigoberto (x -1 );
}

Ref badReverse (Ref list){
    if(isNil(list) == 1) return nil;
    return right(list) . left(list);
}

Ref prepend (int x, Ref r){
    return (x . r);
}

int fib (int i){
    if (i <= 2) return i;
    return fib(i-1) + fib(i-2);
}

int double(int rigoberto){
    return rigoberto + rigoberto;
}

Ref doubleList(Ref list){
    if(isNil(list) == 1) return nil;
    return double((int) left(list)) . doubleList((Ref) right(list));
}

int square(int rigoberto){
    return rigoberto * rigoberto;
}

Ref squareList(Ref list){
    if(isNil(list) == 1) return nil;
    return square((int) left(list)) . squareList((Ref)right(list));
}

Ref map( Q func, Ref list){
    if(isNil(list) == 1) return nil;
    return func((int) left(list)) . map(func, (Ref) right(list));
}