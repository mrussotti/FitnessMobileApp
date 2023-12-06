mutable int main(int arg){
    if(arg == 1){
        return q1(17);
    }
    if(arg == 2){
        return q2(17);
    }
    if(arg == 3){
        return q3(17);
    }
    if(arg == 4){
        return q4(17);
    }
    return 0;
}

int q1(int arg){
    if(arg == 0){
        return 0;
    }
    Ref r = 1 . 1;
    return q1(arg - 1);
}

mutable int q2(int arg){
    mutable int i = arg;
    while(i > 0){
        Ref a = nil . nil;
        Ref b = nil . nil;
        setRight(a, (Ref) b);
        setRight(b, (Ref) a);
        i = i - 1;
    }
    return 0;
}

int q3(int arg){
    if(arg > 0){
        Ref a = nil . nil;
        free(a);
        return q1(arg-1);
    }
    return 0;
}

int q4(int arg){
    mutable int i = arg;
    while(i>0){
        Ref x = nil . nil;
        i = i - 1;
    }
    return 0;
}
