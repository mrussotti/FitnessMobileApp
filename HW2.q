/* Quesiton 1 */
int isList(Q x){
    if(isNil(x) != 0){ 
        return 1;
    }
    if(isAtom(x) != 1){
        return isList(right((Ref)x));
    }
    return 0;
}

/* Question 2 */
Ref append(Ref x, Ref y){
    if (isNil(x) != 0) {
        return y;
    }
    Q left = left(x);
    Q right = right(x);
    if (isNil(right) != 0) {
        return left . y;
    }
    return left . append((Ref)right, y);
}

/* Question 3 */
Ref reverse(Ref x) {
    if (isNil(x) != 0) {
        return x;
    }
    if(isNil(right(x))==0){
        return append(reverse((Ref)right(x)), (left(x) . nil));
    }
    return x;
}

/* Question 4 */
int length(Ref x){
    if(isNil(x) != 0){
        return 0;
    }
    return 1 + length((Ref)right(x));
}

int isSorted(Ref x){
    if (isNil(x) != 0) {
        return 1;
    }
    if (isList(left(x)) == 0) {
        return 0;
    }
    if (isNil(right(x)) != 0) {
        return 1;
    }
    return checkOrder(x, 0); 
}

int checkOrder(Ref x, int prev){
    if(isNil(x) != 0){
        return 0;
    }
    int curLen = length((Ref)left(x));
    if(isNil(right(x)) != 0){
        /* here there no more non-nil elements to check so we terminate*/
        if(curLen >= prev){
            /* check that current length is greater than previous
               if it's not we return prev */
            return curLen;
        } else { 
            return prev;
        }
    }
    /* there are more elements to check so we recur */
    if(curLen >= prev){
        /* check that current length is greater than previous
           if it's not we return 0 as the list of lists is not
           sorted by length */
        return checkOrder((Ref)right(x), curLen);
    }
    return 0;
}

/* Question 5 */
/* If statements are necessary because they help to create a flow alongside the recursion
   No complex methods would be possible without If statements. Without them it would not be 
   a Turing complete language.

   Recursion is necessary because it allows iteration where loops are unavailable. Loops are
   necessary because how else would it be possible to address a set of values without hard
   coding to indeces. */