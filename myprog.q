mutable int main (int args) {
    if (args == 1) {
        return prob1(17);
    } else if (args == 2) {
        return prob2Wrapper(17);
    } else if (args == 3) {
        return prob3(17);
    } else if (args == 4) {
       return prob4Wrapper(17); 
    }
    return 0;
}


/* Problem 1 --------------------------------------------- */
/* ref/quandary -gc MarkSweep -heapsize 408 myprog.q 1 || process returns 0*/
/* ref/quandary -gc MarkSweep -heapsize 384 myprog.q 1 || process returns 5 */

/* Incorrect: Refs declared through these two functions will not exceed heap memory
    each call to prob() results in a new stack frame and the vairable x is lost everytime 
    thus leaving no path to the data in the heap allowing them to be deleted using mark-sweep */
int loopWrapper(int i) {
    if (i > 0) {
        int a = prob(1);
        return loopWrapper(i-1);
    }
    return 0;
}
int prob(int i) {
    Ref x = (1 . 1);
    return 0;
}


/* Correct: Can declare 16 but not 17 refs using marksweep as
    stack frames are not cleared until the final call,
    this leaves all the refs declared "alive" 
    preventing them from being deleted using mark-sweep */
int prob1(int i) {
    if (i > 0) {
        Ref x = (1 . 1);
        return prob1(i-1); /* important note; some languages will recognize tail recursion and clean the stack before each recursive call */
    }
    return 1;
}

/* Problem 2 ------------------------------------------- */
/* ref/quandary -gc RefCount -heapsize 384 myprog.q 2 || process returns 5 */
/* ref/quandary -gc MarkSweep -heapsize 384 myprog.q 2 || process returns 0 */


/* Incorrect: For these two functions, the objects x and y point to will not persist past the function call even though they reference each other
    this is because when the stack frame is cleared whatever x and y are currently pointing at are deleted as part of the stack clean up process*/
mutable int loopWrapperAlt(int i) {
    if (i > 0) {
        int a = prob2Alt(1);
        return loopWrapper(i-1);
    }
    return 0;
}
mutable int prob2Alt(int a) {
    mutable Ref x = (1.nil);
    mutable Ref y = (1.nil);
    setRight(x, y);
    setRight(y, x);
    return 0;
}


/* Correct: this works because the objects lose their refs while the stack frame is active,
    so when the stack is being cleaned the objects cant be reached to be removed with the stack frame
    this is caught by mark and sweep but not reference counting; remember, gc is about cleaning unaccessible data
    ref counting works by deleting anything that is not referenced by anything */
mutable int prob2Wrapper(int i) {
    if (i <= 0) {
        return 0;
    }
    prob2();
    return prob2Wrapper(i-1);
}
mutable int prob2() {
    /* Using a while loop would not make a difference here; can you figure out why? */
    mutable Ref x = (1.nil);
    mutable Ref y = (1.nil);
    setRight(x, y);
    setRight(y, x);
    x = nil;
    y = nil;
    return 0;
}

/* Problem 3 ------------------------------------------ */
/* ref/quandary -gc MarkSweep -heapsize 384 myprog.q 3 || should return 5 */
/* ref/quandary -gc Explicit -heapsize 384 myprog.q 3 || should return 0 */

/* Correct: See problem 1; only difference is that we explicitly free x before making the next call */
int prob3(int i) {
    if (i > 0) {
        Ref x = (1 . 1);
        free(x);
        return prob1(i-1);
    }
    return 1;
}



/* Problem 4 ------------------------------------------ */
/* ref/quandary -gc MarkSweep -heapsize 384 myprog.q 4 || should return 0 */
/* ref/quandary -gc Explicit -heapsize 384 myprog.q 4 || should return 5 */


/* Correct: see incorrect answer for problem 1; can you figure out whats happening here? */
int prob4Wrapper(int i) {
    if (i > 0) {
        int a = prob4(1);
        return prob4Wrapper(i-1);
    }
    return 0;
}
int prob4(int i) {
    Ref x = (1 . 1);
    return 0;
}


/*
Some notes for the project:
    Quandary is 64bit/8byte words
    Refs are 24 bytes (4 words)
*/