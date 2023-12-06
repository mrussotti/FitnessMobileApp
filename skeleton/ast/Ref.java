package ast;
import java.util.concurrent.locks.ReentrantLock;

public class Ref extends Q {
    public Q left;
    public Q right;
    //public volatile int lock;
    public ReentrantLock lock = new ReentrantLock();


    public static final Ref NIL = new Ref(null, null);

    public Ref(Q left, Q right) {
        this.left = left;
        this.right = right;
        //this.lock = 0;
    }

    public Q getLeft() {
        return left;
    }
    
    public Q getRight(){
        return right;
    }

    // public synchronized boolean isLocked(){
    //     return lock == 1;
    // }

    // public synchronized boolean tryLock(){
    //     if(isLocked()){
    //         return false;
    //     }else{
    //         lock = 1;
    //         return true;
    //     }
    // }

    // public synchronized void release(){
    //     lock = 0;
    // }

    public boolean isLocked(){
        return lock.isLocked();
    }

    public boolean tryLock(){
        return lock.tryLock();
    }

    public void release(){
        if(isLocked()){
            lock.unlock();
        }
    }

    public boolean isNil(){
        return this == NIL;
    }

    public void setLeft(Q newLeft){
        left = newLeft;
    }

    public void setRight(Q newRight){
        right = newRight;
    }

    @Override
    public String toString() {
        if (this == NIL){
            
            return "nil";
        }
        return "(" + left + " . " + right + ")";
    }
}
