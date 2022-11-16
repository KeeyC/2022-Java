package com.focustech.fpc.algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.hash;

/**
 * 差异分析器实现类示例
 * @author KeCheng
 **/
public class DemoDifferenceAnalyser implements IDifferenceAnalyser{


    @Override
    public int[] diff() {
        /**
         * 将大文件分割成多份小文件
         * 使用hashSet进行查看是否含有
         * 用流来读取文件
         * 难点在于在规定时间内使用规定内存
         * 用hashSet可以节省时间
         */
        int fileCutNum = 100;
           try {
               ArrayList<Integer> res = new ArrayList<>();
               int cnt = 0;

               File file=new File("src/main/resources/dest");
              String fileDestName=file.getName();
              long fileDestSize=file.length();

              File file1 = new File("src/main/resources/origin");
              String fileOriginName = file1.getName();
              long fileOriginSize = file1.length();


               //缓冲流 =================================================
               InputStream inputDest = this.getClass().getClassLoader().getResourceAsStream("dest");
               InputStreamReader destReader= new InputStreamReader(inputDest);
               BufferedReader dest = new BufferedReader(destReader);

               InputStream inputOrigin = this.getClass().getClassLoader().getResourceAsStream("origin");
               InputStreamReader originReader= new InputStreamReader(inputOrigin);
               BufferedReader origin = new BufferedReader(originReader);

               //========================================================
               /**
                * 分割文件 两个大文件分割成400份小文件
                * 本次操作为创建400个文件
                */
               for(int i=0;i<fileCutNum;i++){
                   File file2 = new File("src/main/resources/file/"+i+".txt");
                   if (file2.exists()) file2.delete();
                   if (!file2.exists()) file2.createNewFile();
               }
               String destLine,originLie;
               /*
               根据哈希值将文件中的行数放到400个文件中，
                哈希值相同的会被放到一个文件里
               */
               long timeStart1 = System.currentTimeMillis();
                int count = 0;
                
               while ((destLine=dest.readLine())!=null){
                   int code = hash(destLine)%fileCutNum;
                   if (code<0)code=-code;//取绝对值
                   /**
                    * 追加文件需要在FileWriter第二个参数选择true
                    */
                   BufferedWriter out = new BufferedWriter(new FileWriter("src/main/resources/file/"+code+".txt",true));
                   out.write(destLine);
                   out.write(","+String.valueOf(count));
                   out.write("\n");
                   out.flush();
                    count++;
               }
               System.out.println("第一次写文件需要花费：");
               System.out.println(System.currentTimeMillis()-timeStart1);

               long timeStart2 = System.currentTimeMillis();
            count = 0;
                while ((originLie=origin.readLine())!=null){

                    int code = hash(originLie)%fileCutNum;
                    if (code<0)code=-code;//取绝对值
                    /**
                     * 追加文件需要在FileWriter第二个参数选择true
                     */
                    BufferedWriter out = new BufferedWriter(new FileWriter("src/main/resources/file/"+code+".txt",true));
                    out.write(originLie);
                    out.write(","+count);
                    out.write("\n");
                    out.flush();
                    count++;
                }
               /**
                * 读写操作约耗时5min
                */
               System.out.println("第二次写文件需要花费");
               System.out.println(System.currentTimeMillis()-timeStart2);

               /**
                * 接下来遍历多个文件
                */
                long startTime = System.currentTimeMillis();
               String line=null;
               List<String> list = new ArrayList<>();
//               HashMap<String,String> numMap = new HashMap<>();
               for (int i=0;i<fileCutNum;i++){
                   HashMap<String,String> hashMap = new HashMap<>();
                   BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/file/"+i+".txt"));
                   while( (line = bufferedReader.readLine())!=null){
                    String[] a = line.split(",");
                    line = a[0];
                    String num = a[1];
                      if (hashMap.containsKey(line)){
                          hashMap.replace(line,"true");
                      }
                      else {
                          hashMap.put(line,num);
                      }

                  }
                   Set<String> Key = hashMap.keySet();
                   for (String key:Key){
                       if (!hashMap.get(key).equals("true")){
                           list.add(hashMap.get(key));
                       }
                   }
                   hashMap=null; //置空 等GC

               }
               dest.close();
               origin.close();
               int [] result = new int[list.size()];
               int index =0;
               for (int i=0;i<list.size();i++){
                   String num = list.get(i);
                   result[index++] = Integer.parseInt(num);
               }
             Arrays.sort(result);
               System.out.println("处理数据所需时间");
               System.out.println(System.currentTimeMillis()-startTime);
               System.out.println("======");
               for (int i=0;i< result.length;i++){
                   System.out.println("处理得到的数据：");
                   System.out.println(result[i]);
               }
               System.out.println("======");
              return  result;

           } catch (IOException ex) {
               ex.printStackTrace();
           }
        return new int[0];
    }
    }


