import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100, true);
    public static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100, true);
    public static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100, true);

    public static AtomicInteger atomicCountA = new AtomicInteger(0);
    public static AtomicInteger atomicCountB = new AtomicInteger(0);
    public static AtomicInteger atomicCountC = new AtomicInteger(0);

    public static final int SIZESTR = 10_000;
    public static final int LENGTHSTR = 100_000;

    public static final String letters = "abc";

    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<>();

        Thread generateThread = new Thread(() -> {
            int count = 0;
            while (count != SIZESTR) {
                String text = generateText(letters, LENGTHSTR);
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                    count++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        threads.add(generateThread);
        threads.add(searchChar(queueA,letters.charAt(0),atomicCountA));
        threads.add(searchChar(queueB,letters.charAt(1),atomicCountB));
        threads.add(searchChar(queueC,letters.charAt(2),atomicCountC));

        for (Thread thread: threads) {
            thread.start();
        }

        for (Thread thread: threads) {
            thread.join();
        }

        System.out.println("символов " + letters.charAt(0) + " " + atomicCountA.get());
        System.out.println("символов " + letters.charAt(1) + " " + atomicCountB.get());
        System.out.println("символов " + letters.charAt(2) + " " + atomicCountC.get());
    }

    public static Thread searchChar(BlockingQueue<String> queue, char chr, AtomicInteger atomic) {
        Thread thread = new Thread(() -> {
            int count = 0;
            while (SIZESTR != count) {
                for (int i = 0; i < SIZESTR; i++) {
                    String str;
                    try {
                        str = queue.take();
                        count++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    for (int j = 0; j < LENGTHSTR; j++) {
                        if (str.charAt(j) == chr) {
                            atomic.incrementAndGet();
                        }
                    }
                }
            }
        });
        return thread;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}