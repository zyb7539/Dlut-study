package com.xuecheng.media;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.*;

public class BigFileTest {


    // 测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("C:\\Users\\zyb01\\Desktop\\test1.mp4");
        //分块文件存储
        String chunkFilePath = "F:\\chunk\\";
        //分块大小
        int chunkSize = 1024 * 1024 *5;
        //分块文件个数
        int chunkNum =(int) Math.ceil( sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读数据，向分块文件写数据
        RandomAccessFile r = new RandomAccessFile(sourceFile, "r");
        byte[] bytes = new byte[1024];
        for (int i =0;i < chunkNum;i++){
            File chunkFile = new File(chunkFilePath + i);
            // boolean newFile = chunkFile.createNewFile();

            //分块文件写入流
            RandomAccessFile rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }
            rw.close();
        }
        r.close();

    }
    @Test
    public void testMerge() throws IOException {
        File chunkFolder= new File("F:/chunk/");
        //源文件
        File sourceFile = new File("C:\\Users\\zyb01\\Desktop\\test1.mp4");
        //合并后的文件
        File mergeFile = new File("F:\\1.mp4");
        boolean newFile = mergeFile.createNewFile();
        // ..取出所有分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转为list
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //写的流
        RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");
        //缓存区
        byte[] bytes= new byte[1024];
        //遍历分块文件
        for (File file : list) {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
            }
            r.close();
        }
        rw.close();
    }


   

}
