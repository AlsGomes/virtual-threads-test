package br.com.agdev;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadsTest {

    public static final int TOTAL_FILES = 1_000_000;
    public static final Path FOLDER = Paths.get(System.getProperty("java.io.tmpdir"), "test-virtual-thread");
    public static final int N_THREADS = 1000;

    public static void main(String[] args) throws IOException {
        Files.createDirectories(FOLDER);

        VirtualThreadsTest app = new VirtualThreadsTest();

        AtomicInteger counter = new AtomicInteger(0);

//        ExecutorService executorService = Executors.newFixedThreadPool(N_THREADS);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        app.execute(counter, executorService, TOTAL_FILES);
    }

    private void execute(AtomicInteger counter, ExecutorService executorService, int totalFiles) {
        Instant start = Instant.now();
        try (ExecutorService threadPool = executorService) {
            for (int i = 0; i < totalFiles; i++) {
                threadPool.execute(createFile(counter));
            }
        }
        Instant end = Instant.now();

        long elapsedTime = ChronoUnit.SECONDS.between(start, end);
        System.out.printf("A criação dos arquivos levou %02d segundos\n", elapsedTime);
    }

    private Runnable createFile(AtomicInteger counter) {
        return () -> {
            try {
                String fileName = String.format("file%02d.txt", counter.incrementAndGet());
                System.out.printf("Gerando arquivo %s\n", fileName);

                Thread.sleep(new Random().nextLong(1000, 10000));

                List<String> lines = new ArrayList<>();
                int totalLines = new Random().nextInt(1, 101);
                for (int i = 0; i < totalLines; i++) {
                    lines.add(String.format("%02d - Texto de quantidade aleatória de linhas", i + 1));
                }

                Files.write(FOLDER.resolve(fileName), lines);
                System.out.printf("Arquivo %s gerado\n", fileName);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }
}