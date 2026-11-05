import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeQueue<S> {
   private final Queue<S> q;
   private final ReentrantLock lock;
   private final int cap;
   private final Condition notFull;
   private final Condition notEmpty;
   public boolean closed;

   public ThreadSafeQueue(int cap) {
      if (cap <= 0)
         throw new IllegalArgumentException("Cap should be >= 1");
      q = new ArrayDeque<>();
      lock = new ReentrantLock();
      this.cap = cap;
      notFull = lock.newCondition();
      notEmpty = lock.newCondition();
      this.closed = false;
   }

   public void add(S e) {
      lock.lock();
      try {
         while (q.size() == cap) {
            notFull.await();
         }
         q.add(e);
         System.out.println("Item " + e + " Produced by Thread : " + Thread.currentThread().getName());
         notEmpty.signalAll();
      } catch (InterruptedException ex) {
         throw new RuntimeException(ex);
      } finally {
         lock.unlock();
      }
   }

   public Optional<S> poll() {
      lock.lock();
      try {
         while (q.isEmpty() && !closed)
            notEmpty.await();
         if (q.isEmpty())
            return Optional.empty();
         S res = q.poll();
         notFull.signalAll();
         return Optional.of(res);
      } catch (InterruptedException ex) {
         throw new RuntimeException(ex);
      } finally {
         lock.unlock();
      }
   }

   public boolean isEmpty() {
      lock.lock();
      try {
         return this.size() == 0;
      } finally {
         lock.unlock();
      }
   }

   public int size() {
      lock.lock();
      try {
         return q.size();
      } finally {
         lock.unlock();
      }
   }

   public void shutdown() {
      lock.lock();
      try {
         closed = true;
         notEmpty.signalAll();
         notFull.signalAll();
      } finally {
         lock.unlock();
      }
   }

}
