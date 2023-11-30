mutable Q main (mutable int arg) {
	if (arg == 0)
		return nil;
	mutable Ref list = randomList(arg);
	print(arg);
	while (arg > 0) {
		print(arg);
		int index = randomInt(arg);
		list = rmElement(list, index);
		list = randomInt(100000) . list;
		print(arg - 1);
		arg = arg - 1;
		print(arg);
	}
	return length(list);
}

int length(Ref list) {
	if (isNil(list) != 0) return 0;
	return 1 + length((Ref)right(list));
}

Ref randomList(int length) {
	if (length == 0) {
        return nil;
    }
    return randomInt(100000) . randomList(length - 1);
}

mutable Ref rmElement(Ref list, int index) {
	if (isNil(list) != 0)
		return nil;
	if (index == 0) {
		return (Ref)right(list);
	}
	setRight(list, rmElement((Ref)right(list), index-1));
	print(list);
	return list;
}