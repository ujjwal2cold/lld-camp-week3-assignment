import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class Main {
   public static void main(String[] args) {
      ThreadSafeQueue<Integer> tsq = new ThreadSafeQueue<>(10);
      AtomicInteger activeProducers = new AtomicInteger(2);
      Producer p1 = new Producer(tsq, activeProducers), p2 = new Producer(tsq, activeProducers);
      Consumer c1 = new Consumer(tsq), c2 = new Consumer(tsq);
      Thread t1 = new Thread(p1), t2 = new Thread(p2);
      Thread t3 = new Thread(c1), t4 = new Thread(c2);
      t1.start();
      t2.start();
      t3.start();
      t4.start();
   }
}

class Producer implements Runnable {
   private AtomicInteger activeProducers;
   private ThreadSafeQueue<Integer> tsq;

   Producer(ThreadSafeQueue<Integer> tsq, AtomicInteger activeProducers) {
      this.tsq = tsq;
      this.activeProducers = activeProducers;
   }

   @Override
   public void run() {
      for (int i = 0; i < 20; ++i) {
         tsq.add(i);
      }
      dec();
   }

   public synchronized void dec() {
      if (activeProducers.decrementAndGet() == 0)
         tsq.shutdown();
   }
}

class Consumer implements Runnable {
   private final ThreadSafeQueue<Integer> tsq;

   Consumer(ThreadSafeQueue<Integer> tsq) {
      this.tsq = tsq;
   }

   @Override
   public void run() {
      while (true) {
         Optional<Integer> item = tsq.poll();
         if (item.isEmpty()) {
            break;
         }
         System.out.println(
                 "Consumed " + item.get() +
                         " by " + Thread.currentThread().getName()
         );
      }
   }
}