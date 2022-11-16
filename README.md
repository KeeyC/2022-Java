# 焦点科技编程挑战赛2022题解

## 比赛说明：

比赛在四个学校开展，南理南航南农和矿大。

### 题目

查找文本差异

### 要求

- `origin`和`dest`中分别包含`1000w+`条数据
- `dest`对数据进行了打乱操作，即`origin`和`dest`中相同数据行的顺序不相同
- 程序运行的总内存消耗不能超过`30MB`
- 程序运行的总时间消耗不能超过`10`分钟
- `origin`中存在但`dest`中不存在的数据，取`origin`中的行号；`dest`中存在但`origin`中不存在的数据，取`dest`中的行号
- 输出的行号数组按照升序排列

### 判定规则

- 总内存消耗超过`30MB`，判定为不合格
- 总时间消耗超过`10`分钟，判定为不合格

### 示例

假设 origin 文件内容为：

```
e630f353-01b3-4b2c-989c-6236b476140a
cc8626ef-c45b-4b9a-96b6-3206d8adc518
8c622fb7-8150-4487-91f2-6a860eb14feb
78c0fe67-b0ea-4fe5-9ef0-4157d956e3d2
```

假设 dest 文件内容为：

```
78c0fe67-b0ea-4fe5-9ef0-4157d956e3d2
14fbf539-a614-402c-8ab9-8e7438f3bd72
e630f353-01b3-4b2c-989c-6236b476140a
cc8626ef-c45b-4b9a-96b6-3206d8adc518
```

输出结果为：

```
[1, 2]
```



## 解决方案：

先分析本题的数据量。本题文件数据量两个文件大约在3000万条左右，比较符合海量数据的处理。同时对时间和内存的限定也十分的苛刻。

所以本题采用的方法是将大文件每行按hash值放入对应的小文件中，然后依次处理每个小文件。

#### 步骤：

首先将两个大文件按hash值分别放入切割后的小文件,两个文件一共800mb，2800w行左右

```java
  while ((destLine=dest.readLine())!=null){
                   int code = hash(destLine)%200;
                   if (code<0)code=-code;//取绝对值
                   /**
                    * 追加文件需要在FileWriter第二个参数选择true
                    */
                   BufferedWriter out = new BufferedWriter(new FileWriter("src/main/resources/file/"+code+".txt",true));
                   out.write(destLine);
                   out.write(","+String.valueOf(count));  //添加逗号是为了将行号加入，减少后面再去寻找行号的时间
                   out.write("\n");
                   out.flush();
                    count++;
               }
   while ((originLie=origin.readLine())!=null){

                    int code = hash(originLie)%200;
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
//写入第一个文件花费 16000ms
//写入两个大概33000ms
```

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h85kh0zyjdj309g05074a.jpg)

然后分别遍历这些小文件中的数据

```java
  							 String line=null;
               List<String> list = new ArrayList<>();
               for (int i=0;i<200;i++){
                   HashMap<String,String> hashMap = new HashMap<>();
                   BufferedReader bufferedReader = new BufferedReader(new FileReader("src/main/resources/file/"+i+".txt"));
                  while( (line = bufferedReader.readLine())!=null){
                    String[] a = line.split(",");
                    line = a[0];
                    String num = a[1];
                    //若HashMap中没有字符串，则将字符串和行号放入
                    //若后续出现重复，则将行号改为true
                      if (hashMap.containsKey(line)){     
                          hashMap.replace(line,"true");
                      }
                      else {   
                          hashMap.put(line,num);
                      }

                  }
                   Set<String> Key = hashMap.keySet();
                 //向list中添加行号
                   for (String key:Key){
                       if (!hashMap.get(key).equals("true")){
                           list.add(hashMap.get(key));
                       }
                   }
                   hashMap=null; //置空 等GC

               }
```



然后便是将list转化为int类型数组输出

```JAVA
  int [] result = new int[list.size()];
               int index =0;
               for (int i=0;i<list.size();i++){
                   String num = list.get(i);
                   result[index++] = Integer.parseInt(num);
               }
```

#### 处理结果：

时间花费6分钟

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h85wy3zuekj30fa0k2q3p.jpg)

至此内存与时间的限制都满足了。

#### 注意问题

hashMap内存的释放

### 全部代码:

```java
package com.focustech.fpc.algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.hash;

/**
 * 差异分析器实现类示例
 * @author KeC
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
               for(int i=0;i<200;i++){
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
                   int code = hash(destLine)%200;
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

                    int code = hash(originLie)%200;
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
                * 接下来遍历200个文件
                */

               String line=null;
               List<String> list = new ArrayList<>();
//               HashMap<String,String> numMap = new HashMap<>();
               for (int i=0;i<200;i++){
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
               System.out.println(result);
              return  result;
              
           } catch (IOException ex) {
               ex.printStackTrace();
           }
        return new int[0];
    }
    }



```

## 改进

### 使用单线程读文件，多线程写文件。

创建一个ToFile类 继承Thread类

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h86so589rej30py09igma.jpg)

#### 多线程写文件时碰到的问题

```
java.io.IOException: Stream closed
```

这是因为在线程启动后，主线程没有等子线程结束并接着运行,主线程关闭了子线程的文件流。

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h86s9zdhqgj30pi02eq2x.jpg)

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h86saunmjxj30xs0a83zq.jpg)

需要在子线程启动后加![](https://tva1.sinaimg.cn/large/008vxvgGgy1h86sc4aey6j30q602it8o.jpg)

测试结果：

![](https://tva1.sinaimg.cn/large/008vxvgGgy1h86si0um2sj30h0086q39.jpg)

多线程处理后时间减少了两分钟左右。

#### 多线程全部代码:

```java
package com.focustech.fpc.algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.hash;

/**
 * 差异分析器实现类示例
 * @author KeC
 **/
public class DemoDifferenceAnalyser implements IDifferenceAnalyser{
    final static int fileCutNum = 100;


    @Override
    public int[] diff() {
        /**
         * 将大文件分割成多份小文件
         * 使用hashSet进行查看是否含有
         * 用流来读取文件
         * 难点在于在规定时间内使用规定内存
         * 用hashSet可以节省时间
         */
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
//               String destLine,originLie;
               /*
               根据哈希值将文件中的行数放到400个文件中，
                哈希值相同的会被放到一个文件里
               */
               long timeStart1 = System.currentTimeMillis();

               ToFile threadOne = new ToFile(dest);
               ToFile threadTwo = new ToFile(origin);
               threadOne.start();
               threadTwo.start();
               threadOne.join();
               threadTwo.join();
               Thread.sleep(100);
               System.out.println("多线程写文件共需要花费：");
               System.out.println(System.currentTimeMillis()-timeStart1);



               /**
                * 接下来遍历多个文件
                */
                long startTime = System.currentTimeMillis();
               String line=null;
               List<String> list = new ArrayList<>();

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
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
        return new int[0];
    }
    }


    class ToFile extends Thread{

     private BufferedReader dest;

      public   ToFile(BufferedReader bufferedReader){
            dest = bufferedReader;
        }

        @Override
        public  synchronized void run(){

              final  int fileCutNum = 100;
              try {
                  String destLine;
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
              }catch (Exception e){
                  System.out.println(e.getMessage());
                  e.printStackTrace();
              }
          }



        }




```



