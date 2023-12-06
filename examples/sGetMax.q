mutable Q main(int arg) {
  mutable Ref list = nil;
  list = add(list, 4);
  list = add(list, 3);
  list = add(list, 5);
  list = add(list, 40);
  list = add(list, 11);
  list = add(list, 32);
  list = add(list, 27);
  list = add(list, 1);
  print(list);
  return getMax(list);
}

mutable Ref add(Ref list, Q elem) {
  if (isNil(list) != 0) {
    print(78);
    print (elem);
    print(list);
    print(elem . nil);
    return elem . nil;
  }
  mutable Ref curr = list;
  while (isNil(right(curr)) == 0) {
    curr = (Ref)right(curr);
  }
  setRight(curr, elem . nil);
  return list;
}

int length(Ref c) {
  print(c);
  print(isAtom(c));
  if (isAtom(c) != 0){ 
    print(55);
    return 0;
  }
  return 1 + length((Ref)right(c));
}

int getMax(Ref list){
	mutable Ref dumList=list;
  print(list);
	int n = length(list);
  print(n);
	mutable int i=1;
	mutable int currmax=0;
	while(i<=n){
    print(123);
		if(currmax<(int)left(dumList))
			currmax=(int)left(dumList);
		dumList=(Ref)right(dumList);
		i=i+1;
	}
return currmax;

}
