package com.bigdata;


import com.bigdata.utils.PathUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.avro.ipc.Transceiver;
import org.apache.spark.streaming.flume.sink.EventBatch;

public class WordGenerateUtils {
    public static final String[] wordList = new String[]{"java", "php", "go", "python", "c", "js"};
    public static List<String> wordArray = Arrays.asList(wordList);

    public String generateWordStrs(int count)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0;i < count;i++)
        {
            Random random = new Random();
            int randWordIndex = random.nextInt(wordList.length);
            sb.append(wordList[randWordIndex] + " ");
            if (i%10 == 0 && i != 0)
            {
                sb.append(" \n");
            }
        }
        return  sb.toString();
    }

    public boolean saveToLocalFile(String filePath,String fileData)
    {
        boolean bRet = false;
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path))
            {
                Files.write(path,fileData.getBytes(), StandardOpenOption.CREATE);
            }
            else{
                Files.write(path,fileData.getBytes(), StandardOpenOption.APPEND);
            }

            bRet = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  bRet;
    }

    public static class GeneWordTask implements Runnable
    {
       private WordGenerateUtils wordGenerateUtils;

        public GeneWordTask(WordGenerateUtils wordGene) {
            this.wordGenerateUtils = wordGene;
        }

        @Override
        public void run() {
            int i  = 0;
            String savePath = "/usr/local/flume/exec-log";
//            savePath = PathUtils.getCurPath() + "/exec-log";
            System.out.println(" run start. savePath:" + savePath);
            while (i < 1000)
            {
                this.wordGenerateUtils.saveToLocalFile(savePath,wordGenerateUtils.generateWordStrs(100));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i%10 == 0)
                {
                    System.out.println(" run loop i:"+i);
                }
                i++;

            }
            System.out.println(" run end.");
        }
    }


    public static void main(String[] args) throws InterruptedException {
       WordGenerateUtils wordGenerateUtils = new WordGenerateUtils();
       int count = 100000;
//       wordGenerateUtils.saveToLocalFile("/usr/local/flume/exec-log",wordGenerateUtils.generateWordStrs(count));
        GeneWordTask geneWordTask = new GeneWordTask(wordGenerateUtils);
        Thread t1 = new Thread(geneWordTask);
        t1.start();
        t1.join();
    }

}
