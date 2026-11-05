import java.util.Arrays;
import java.util.concurrent.*;


class Sum implements Callable<Integer> {
   private final int a;
   private final int b;
   Sum(int a, int b) {
      this.a = a;
      this.b = b;
   }

   @Override
   public Integer call() throws Exception {
      int res = 0;
      for (int i = a; i <= b; ++i)
         res = res + i;
      return res;
   }
}

class Prime implements Runnable {
   private final int a;
   private final int b;

   Prime(int a, int b) {
      this.a = a;
      this.b = b;
   }

   @Override
   public void run() {
      boolean[] spf = new boolean[b + 1];
      Arrays.fill(spf, true);
      spf[0] = spf[1] = false;
      int res = 0;
      for (int i = 0; i <= b; ++i) {
         if (spf[i]) {
            if (i >= a)
               res += 1;
            for (int j = 2 * i; j <= b; j += i)
               spf[j] = false;
         }
      }
      System.out.println("Total number of Primes between " + a + " and " + b + " = " + res);
   }
}

class Solution {
   public static void main(String[] args) throws Exception {
      Runnable POISON_PILL = new Runnable() {
         @Override
         public void run() {

         }
      };
      ThreadPool pool = new ThreadPool(3, POISON_PILL);
      for (int i = 0; i < 3; ++i) {
         Future<Integer> ft = pool.submit(new Sum(i, i + 10));
         System.out.println(ft.get()); // ft.get() is blocking
      }
      for (int i = 0; i < 3; ++i) {
         pool.execute(new Prime(10, 10 + i)); // This will fire parallel threads
      }
      pool.shutDown();
   }
}


class ThreadPool {
   private final BlockingQueue<Runnable> q;
   private final Runnable POISON_PILL;
   private int workersCount;

   ThreadPool(int workersCount, Runnable POISON_PILL) {
      this.workersCount = workersCount;
      this.POISON_PILL = POISON_PILL;
      this.q = new ArrayBlockingQueue<>(10);
      for (int i = 0; i < workersCount; ++i) {
         Thread t = new Thread(new Worker(this.q, POISON_PILL));
         t.start();
      }
   }

   public <T> Future<T> submit(Callable<T> r) throws Exception {
      FutureTask<T> ft = new FutureTask<>(r);
      q.put(ft);
      return ft;
   }

   public void execute(Runnable r) throws Exception {
      q.put(r);
   }

   public void shutDown() throws InterruptedException {
      for (int i = 0; i < workersCount; ++i)
         q.put(POISON_PILL);
   }
}

class Worker implements Runnable {
   private final BlockingQueue<Runnable> q;
   private final Runnable POISON_PILL;

   Worker(BlockingQueue<Runnable> q, Runnable POISON_PILL) {
      this.q = q;
      this.POISON_PILL = POISON_PILL;
   }

   @Override
   public void run() {
      while (true) {
         try {
            Runnable task = q.take();
            if (task == POISON_PILL)
               break;
            task.run();
         } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            break;
         }
      }
   }
}

