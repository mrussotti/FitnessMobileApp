Ref get(Ref list, int pos){
    if(isNil(list)==1){
        return nil;
    }
    if(pos==0){
        return (Ref)left(list);
    }
    return get((Ref)right(list), pos-1);
}

Q main (int x){
    Ref in = ((3.(5.nil)).((4.nil).nil));

    Ref out = get( in, 3);
    return out;
}