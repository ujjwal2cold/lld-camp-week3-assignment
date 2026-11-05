import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Main {
   public static void main(String[] args) {
      Queue<Integer> q = new ArrayDeque<>();
      ReentrantLock lock = new ReentrantLock();
      Condition notFull = lock.newCondition();
      Condition notEmpty = lock.newCondition();
      int capacity = 10;
      AtomicInteger activeProducers = new AtomicInteger(2);
      Thread p0 = new Thread(new Producer(q, lock, notFull, notEmpty, activeProducers, capacity)), p1 = new Thread(new Producer(q, lock, notFull, notEmpty, activeProducers, capacity));
      Thread c0 = new Thread(new Consumer(q, lock, notFull, notEmpty, activeProducers)), c1 = new Thread(new Consumer(q, lock, notFull, notEmpty, activeProducers));
      p0.start();
      p1.start();
      c0.start();
      c1.start();
   }
}

class Producer implements Runnable {
   private final Queue<Integer> q;
   private final ReentrantLock lock;
   private final Condition notFull;
   private final Condition notEmpty;
   private AtomicInteger activeProducers;
   private int cap;

   Producer(Queue<Integer> q, ReentrantLock lock, Condition notFull, Condition notEmpty, AtomicInteger activeProducers, int cap) {
      this.q = q;
      this.lock = lock;
      this.notFull = notFull;
      this.notEmpty = notEmpty;
      this.activeProducers = activeProducers;
      this.cap = cap;
   }

   @Override
   public void run() {
      for (int i = 0; i < 20; ++i) {
         lock.lock();
         try {
            while (q.size() == cap)
               notFull.await();
            q.add(i);
            System.out.println("Produced item " + i + " by Thread " + Thread.currentThread().getName());
            notEmpty.signal();

         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         } finally {
            lock.unlock();
         }
      }
      lock.lock();
      try {
         if (activeProducers.decrementAndGet() == 0)
            notEmpty.signalAll();
      } finally {
         lock.unlock();
      }
   }
}

class Consumer implements Runnable {
   private final Queue<Integer> q;
   private final ReentrantLock lock;
   private final Condition notFull;
   private final Condition notEmpty;
   private AtomicInteger activeProducers;

   Consumer(Queue<Integer> q, ReentrantLock lock, Condition notFull, Condition notEmpty, AtomicInteger activeProducers) {
      this.q = q;
      this.lock = lock;
      this.notFull = notFull;
      this.notEmpty = notEmpty;
      this.activeProducers = activeProducers;
   }

   @Override
   public void run() {
      while (true) {
         lock.lock();
         try {
            while (q.isEmpty() && activeProducers.get() > 0) {
               notEmpty.await();
            }
            if (q.isEmpty()) return;
            int top = q.poll();
            System.out.println("Consumed item= " + top + " by thread= " + Thread.currentThread().getName());
            notFull.signal();
         } catch (InterruptedException e) {
            throw new RuntimeException(e);
         } finally {
            lock.unlock();
         }
      }
   }
}