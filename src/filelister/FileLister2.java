package filelister;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.filechooser.FileSystemView;

public class FileLister2 {


    private static final String SEPARATE = "\t";

    public static void main(String[] args) {
        listRoots();
    }

    static final int POOL_SIZE = 16;
    static final int MILLI_WAIT = 1000;

    public static void listRoots() {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);

        FileSystemView fsv = FileSystemView.getFileSystemView();

        File[] roots = File.listRoots();
        File csvFile = new File("D:\\1.csv");

        Writer writer;
        try {
            writer = new BufferedWriter(new FileWriter(csvFile));

            long start = System.currentTimeMillis();

            for (File root : roots) {
                if (fsv.isDrive(root)) {
                    createListJob(root, writer, executor);
                }
            }
            int active;

            while ((active = executor.getActiveCount()) > 0) {
                System.out.println("Active Thread :  " + active);
                Thread.sleep(MILLI_WAIT);
            }
            writer.flush();
            writer.close();
            System.out.println("Done:" + (System.currentTimeMillis() - start) + "ms");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return;
    }

    public static void createListJob(File dir, Writer writer, ThreadPoolExecutor executor) {

        String path = dir.getAbsolutePath().toLowerCase();

        if (path.contains("sdk")
                || path.contains("cygwin64")
                || path.contains("eclipse")
                || path.contains("widget")
                || path.contains("androidstudio")
                || path.contains("\\drawable")
                || path.contains("\\mipmap")
                || path.contains("\\system32")
                || path.contains("\\.gradle")
                || path.contains("\\opencv")
                || path.contains("\\microsoft")
                || path.contains("\\android-ndk")
                || path.contains("\\android-studio")
                || path.contains("resources")
                || path.contains("\\temp\\")
                || path.contains("\\temporary internet files\\")
                || path.contains("c:\\windows\\")
                || path.contains("\\program files")
                || path.contains("\\appdata\\")
                || path.contains("template")) {
            return;
        }

        Lister lister = new Lister(dir, writer, executor);
        executor.execute(lister);
    }

    public static void listFiles(File dir, Writer writer, ThreadPoolExecutor executor) throws IOException {

        int counted = 0;
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    createListJob(file, writer, executor);
                } else {
                    String name = file.getName().toLowerCase();

                    if (name.endsWith(".jpg")
                            || name.endsWith(".jpeg")
                            || name.endsWith(".png")
                            || name.endsWith(".gif")) {
                        synchronized(writer) {
                            writer.write(file.getName() + SEPARATE + file.getAbsolutePath() + SEPARATE + file.length() + "\n");
                        }
                        counted++;
                    }
                }
            }

            if (counted > 0) {
                synchronized(System.out) {
                    System.out.println("listFiles:" + dir.getAbsolutePath() + " -> total:" + counted);
                }
            }
        }
    }

    public static class Lister implements Runnable {

        protected File dir;
        protected Writer writer;
        protected ThreadPoolExecutor executor;

        protected Lister(File dir, Writer writer, ThreadPoolExecutor executor) {
            this.dir = dir;
            this.writer = writer;
            this.executor = executor;
        }

        @Override
        public void run() {
            try {
                listFiles(dir, writer, executor);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
