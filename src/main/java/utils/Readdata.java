package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


public class Readdata {
    private static String path = (new File("")).getAbsolutePath();
//    private static String path_src = "D:/快盘1/dev/BurstDetection_MR_v1/data/out2/ldaCorpus/ldaCorpus-r-00000";
    private static String path_src = path + "\\data\\yahoo news.txt";
//    private static String path_src = path + "\\data\\ya.txt";
    private static String path_answer_src = "d:/yahoo_news(3)/answers_rss.csv";
    private static String path_result = path + "dev/BurstDetection_MR_v1/experimentData/chineseCorpus/out/ldaCorpus/part-00000.txt";
    private static String path_result_burst = "d:/yahoo_news(3)/yahoo_news_in_burst.csv";
    private static String path_result_topic = "d:/yahoo_news(3)/yahoo_news_out.txt";
    public static String path_result_hdp = path + "\\data\\temp2\\news.txt";
    public static String path_result_hdp_w2i = path + "\\data\\temp2\\newsW2I.txt";
    public static String path_result_hdp_vocb = path + "\\data\\temp2\\newsVocab.txt";//词汇表
    private static String path_result_hdp_2 = "d:/yahoo_news(3)/yahoo_news_out_lda_2.txt";
    private static BufferedReader br = null;
    private static BufferedWriter bw = null;
    private static String line_current = null;
    private static String[] words = null;
    private static List<String> word_list = new ArrayList<String>();
    static Map<String, Integer> wordToint = new HashMap<String, Integer>();
    static int day = 0; 
    public static int wordCount = 0;
    private static int lowFreq = 1;//低频词
    private static float highFreq = 0.9998f;//高频词
  
    /**
     * @param args
     * @param args[0] inputPath ldaCorpusResult
     * @param args[1] outputPath path_result_hdp
     * @param args[2] outputPath path_result_hdp_w2i
     */
  public static void main(String[] args) {
  	try {
//  	  path_src = args[0];
//  	  path_result_hdp = args[1];
//  	  path_result_hdp_w2i = args[2];
  		readHDPGibbsdata3("");
  		changeWord2Voc(path_result_hdp_w2i, path_result_hdp_vocb);
//  		getVocab(path_src, path_result_hdp_vocb);
//  		readHDPGibbsdata2("");
  	} catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
//  	System.out.println("读取结束");
  }

    public static void printMap(Map<String, Integer> map) throws IOException {

        bw = new BufferedWriter(new FileWriter(path_result));

        Set<String> keys = map.keySet();
        writeResult("[" + keys.size() +"]" );
        for (String s : keys) {
            System.out.println("word: " + s +
                    ", times: " + map.get(s));
           // [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count]
            writeResult(" [" + s +"]:"+
                    "[" + map.get(s)+"]");
        }
    }

    public static void writeResult(String line) throws IOException {

        try {
            if (bw != null) {
                bw.write(line);
//                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeOutputStream(bw);
        }
    }

    public static void closeOutputStream(BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeInputStream(BufferedReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getContent(String content){
    	String rs = "";
    	int idx = content.indexOf("title=\"");
    	if(idx > 0){
    		rs = content.substring(idx + 7);
    		idx = rs.indexOf("/></a>");
    		if(idx > 0){
    			rs = rs.substring(0,idx - 13) + ". " + rs.substring(idx + 6);
    			idx = rs.indexOf(".</p>");
    			if(idx > 0){
    				rs = rs.substring(0,idx+1);
    			}
    		}
    	}
    	else{
    		rs = content;
    	}
    	idx = rs.indexOf("&#039;s");	//去除&#039;s
    	while(idx > 0){
    		rs = rs.substring(0,idx) + " " + rs.substring(idx + 7);
    		idx = rs.indexOf("&#039;s");
    	}
    	idx = rs.indexOf("(AP");	//去除(AP Photo/Jeff Chiu, File)
    	if(idx > 0){
    		int idx2 = rs.substring(idx + 2).indexOf(")");
    		if(idx2 > 0){
    			rs = rs.substring(0,idx-1) + rs.substring(idx + 2).substring(idx2 + 1);
    		}
    	}
    	idx = rs.indexOf("鈥");	//去除鈥*
    	while(idx > 0){
    		rs = rs.substring(0,idx) + " " + rs.substring(idx + 4);
    		idx = rs.indexOf("鈥");
    	}
    	
    	idx = rs.indexOf("锟斤拷");	//去除锟斤拷
    	while(idx > 0){
    		rs = rs.substring(0,idx) + " " + rs.substring(idx + 4);
    		idx = rs.indexOf("锟斤拷");
    	}
    	
    	return rs;
    }
    
    
    
    /**
     * 去除个别数字如200,5中的逗号
     */
    public static String removeComma(String lema){
    	int idx;
    	idx = lema.indexOf(",");
    	String result = lema;
    	if(idx > 0){
    		result = lema.substring(0,idx) + lema.substring(idx+1);
    	}
    	return result;
    }
    
    /**输出word-int映射表
     * @throws IOException **/
    public static void printWord2Int(Map<String, Integer> map) throws IOException{
      bw = new BufferedWriter(new FileWriter(new File(path_result_hdp_w2i)));
      for (Map.Entry<String, Integer> entry : map.entrySet()) {
        try {
          bw.write(entry.getKey() + "\t" + entry.getValue()+"\r\n");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    /**输出word-int映射表
     * @throws IOException **/
    public static int printfWord2Int(Map<String, Integer> map) throws IOException{

    	System.out.println("value: " + map.get("south"));
    	//这里将map.entrySet()转换成list
    	List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
    	//然后通过比较器来实现排序
    	Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
	        //降序排序
	        public int compare(Entry<String, Integer> o1,
	                Entry<String, Integer> o2) {
	            return o2.getValue().compareTo(o1.getValue());
	        }
    	});
    	System.out.println("总词数	:" + list.size());
    	System.out.println("高频词：	" + list.size() * highFreq);
    	int highThreds = (int) (list.size() - list.size() * highFreq);//高频词个数
    	int highThredsVal = 0;//高频词值
    	int count = 0;
		for(Map.Entry<String,Integer> mapping:list){
		  count++;
//		  System.out.println(mapping.getKey()+"	:"+mapping.getValue());
		  if(count == highThreds){
			  highThredsVal = mapping.getValue();
		  }
		}
		if(highThreds < 1){
			highThredsVal = Integer.MAX_VALUE;
		}
		return highThredsVal;
    }
    
    /**输出word-int映射表
     * @throws IOException **/
    public static Map<String, String> readWord2Int(String filePath) throws IOException{
      filePath = path_result_hdp_w2i;
      Map<String, String> map = new HashMap();
      BufferedReader br = new BufferedReader(new FileReader(filePath));
      String line = null;
      String[] str;
      while((line = br.readLine()) != null){
        str = line.split("\t");
        if(str.length > 1){
          map.put(str[1], str[0]);
        }
      }
      br.close();
      return map;
    }
    
    /**输入word-int映射表
     * 输出int-word映射表
     * @throws IOException **/
    public static Map<String, String> readWord(String filePath) throws IOException{
      Map<String, String> map = new HashMap();
      BufferedReader br = new BufferedReader(new FileReader(filePath));
      String line = null;
      String[] str;
//      int count = 0;
      while((line = br.readLine()) != null){
        str = line.split("\t");
        if(str.length > 1){
          map.put(str[1], str[0]);
        }
      }
      br.close();
      return map;
    }
    
    /**输入
     * 输出
     * @throws IOException **/
    public static int getVocabSize(String filePath) throws IOException{
      int vocSize = 0;
      BufferedReader br = new BufferedReader(new InputStreamReader((new FileInputStream(filePath)),
		"UTF-8"));
      String line = null;
      String[] str;
//      int count = 0;
      while((line = br.readLine()) != null){
//        str = line.split("\t");
//        if(line.length() > 1){
        	vocSize++;
//        }
      }
      br.close();
      return vocSize;
    }
    
    /**输出word-int映射表
     * @throws IOException **/
    public static List<String> readWord2(String filePath) throws IOException{
      List<String> vocab = new ArrayList<String>();
      BufferedReader br = new BufferedReader(new FileReader(filePath));
      String line = null;
      String[] str;
      int count = 0;
      while((line = br.readLine()) != null){
        if(line.length() > 1){
        	vocab.add(line);
        }
      }
      br.close();
      return vocab;
    }
    
    /**
     * 读取数据，用于HDPGibbsSampler
     * @throws IOException 
     */
    public static void readHDPGibbsdata(String plainText) throws IOException{
      File file = new File(path_src);
        if (!file.exists()) {
            System.out.println("file " + file + " is not existed, exit");
            return;
        }
        Map<String, Integer> map = new HashMap();
        Map<String, Integer> cipinMap = new HashMap();//词频
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str = null;
        int wordIndex = 0;
        br.readLine();
        int totalCipin = 0;//总词频
        while((str = br.readLine()) != null && !str.trim().equals("")){
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          String[] es = (es1[2] +". " + es1[3]).split(" ");
//          String[] es = str.split(" ");
          for(int i = 0; i< es.length; i++){
        	  String e = es[i].toLowerCase();
        	  if(e.endsWith(".") || e.endsWith(",")){
        		  e = e.substring(0, e.length() - 1);
        	  }
            if(!RemoveStopwords.isStopword(e) && !map.containsKey(e)
            		&& es[i].indexOf("锟斤拷") < 0){
//              System.out.println(wordIndex + "  "+es[i].toLowerCase());
              map.put(e, ++wordIndex);
              cipinMap.put(e, 1);
              totalCipin++;
            }
            else if(map.containsKey(e)){
            	cipinMap.put(e, cipinMap.get(e) + 1);
            	totalCipin++;
            }
          }
        }
        br.close();
      
        
        printWord2Int(map);
        int highThreds = printfWord2Int(cipinMap);
        bw.close();
        br = new BufferedReader(new FileReader(file));
        bw = new BufferedWriter(new FileWriter(new File(path_result_hdp)));
        br.readLine();
        Map<String, Integer> wordMap = new HashMap();
        int count = 0;
        /***
         * 输出的数据格式：
         * [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count]
			where [M] is the number of unique terms in the document, and the
			[count] associated with each term is how many times that term appeared
			in the document. 
         */
        while((str = br.readLine()) != null && !str.trim().equals("")){
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          String[] es = (es1[2]+ ". " + es1[3]).split(" ");
          HashMap<Integer, Integer> hashMp = new HashMap();
          for(int i = 0; i< es.length; i++){
        	  String e = es[i].toLowerCase();
        	  if(e.endsWith(".") || e.endsWith(",")){
        		  e = e.substring(0, e.length() - 1);
        	  }
            if(!RemoveStopwords.isStopword(e) && map.containsKey(e) && !e.trim().equals("")){
            	if(cipinMap.get(e) > lowFreq && (float)cipinMap.get(e) < highThreds){//去掉高频低频
	            	if(!wordMap.containsKey(e)){
	                    wordMap.put(e, 1);
		            }
		            else{
		                wordMap.put(e, wordMap.get(e) + 1);
		            }
            	}
            }
          }
          count++;
          bw.write(wordMap.size()+ printMapResult(wordMap, map));
          bw.write("\r\n");
          
          wordMap.clear();
        }
        br.close();
        bw.close();
        System.out.println("文档数：" + count);
        System.out.println("词数：" + map.size());
        wordCount = map.size();
    }
    
    /**
     * 读取数据，用于HDPGibbsSampler
     * @throws IOException 
     */
    public static void readHDPGibbsdata3(String plainText) throws IOException{
      File file = new File(path_src);
        if (!file.exists()) {
            System.out.println("file " + file + " is not existed, exit");
            return;
        }
        Map<String, Integer> map = new HashMap();
        Map<String, Integer> cipinMap = new HashMap();//词频
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str = null;
        int wordIndex = 0;
        br.readLine();
        int totalCipin = 0;//总词频
        int count = 0;
        while((str = br.readLine()) != null && !str.trim().equals("")){
          count++;
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          String[] es = (es1[2] +". " + es1[3]).split(" ");
//          String[] es = str.split(" ");
          for(int i = 0; i< es.length; i++){
        	  String e = es[i].toLowerCase();
        	  if(e.endsWith(".") || e.endsWith(",")){
        		  e = e.substring(0, e.length() - 1);
        	  }
            if(!RemoveStopwords.isStopword(e) && !map.containsKey(e)
            		&& es[i].indexOf("锟斤拷") < 0){
//              System.out.println(wordIndex + "  "+es[i].toLowerCase());
              map.put(e, ++wordIndex);
              cipinMap.put(e, 1);
              totalCipin++;
            }
            else if(map.containsKey(e)){
            	cipinMap.put(e, cipinMap.get(e) + 1);
            	totalCipin++;
            }
          }
        }
        br.close();
      
        
        printWord2Int(map);
        int highThreds = printfWord2Int(cipinMap);
        bw.close();
        br = new BufferedReader(new FileReader(file));
        bw = new BufferedWriter(new FileWriter(new File(path_result_hdp)));
        
        bw.write(count + "\r\n");//文档数
        bw.write(map.size() + "\r\n");//词汇数
        bw.write(totalCipin + "\r\n");//总词数
        br.readLine();
        Map<String, Integer> wordMap = new HashMap();
        count = 0;
        /***
         * 输出的数据格式：
         * [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count]
			where [M] is the number of unique terms in the document, and the
			[count] associated with each term is how many times that term appeared
			in the document. 
         */
        
        while((str = br.readLine()) != null && !str.trim().equals("")){
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          String[] es = (es1[2]+ ". " + es1[3]).split(" ");
          HashMap<Integer, Integer> hashMp = new HashMap();
          for(int i = 0; i< es.length; i++){
        	  String e = es[i].toLowerCase();
        	  if(e.endsWith(".") || e.endsWith(",")){
        		  e = e.substring(0, e.length() - 1);
        	  }
            if(!RemoveStopwords.isStopword(e) && map.containsKey(e) && !e.trim().equals("")){
            	if(cipinMap.get(e) > lowFreq && (float)cipinMap.get(e) < highThreds){//去掉高频低频
	            	if(!wordMap.containsKey(e)){
	                    wordMap.put(e, 1);
		            }
		            else{
		                wordMap.put(e, wordMap.get(e) + 1);
		            }
            	}
            }
          }
          count++;
          bw.write(printMapResult2(wordMap, map, count));          
          wordMap.clear();
        }
        br.close();
        bw.close();
        System.out.println("文档数：" + count);
        System.out.println("词数：" + map.size());
        wordCount = map.size();
    }
    
    /**输出map结果**/
    public static String printMapResult(Map<String, Integer> wordMap, Map<String, Integer> map) throws IOException {
      StringBuffer sb = new StringBuffer();
      for (Map.Entry<String,  Integer> entry : wordMap.entrySet()) {
    	  sb.append(" " + map.get(entry.getKey())+":"+entry.getValue());
      }
      return sb.toString();
    }
    
    /**输出map结果**/
    public static String printMapResult2(Map<String, Integer> wordMap, 
    		Map<String, Integer> map, int docID) 
    	throws IOException {
      StringBuffer sb = new StringBuffer();
      for (Map.Entry<String,  Integer> entry : wordMap.entrySet()) {
    	  sb.append(docID + " " + map.get(entry.getKey())+" "+entry.getValue() + "\r\n");
      }
      return sb.toString();
    }
    
    /**
     * 读取数据，用于HDPGibbsSampler
     * @throws IOException 
     */
    public static void readHDPGibbsdata2(String plainText) throws IOException{
      File file = new File(path_src);
        if (!file.exists()) {
            System.out.println("file " + file + " is not existed, exit");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(file));        
        br = new BufferedReader(new FileReader(file));
        bw = new BufferedWriter(new FileWriter(new File(path_result_hdp)));
        br.readLine();
        Map<String, Integer> wordMap = new HashMap();
        int count = 0;
        /***
         * 输出的数据格式：
         * docID docContent. 
         */
        
        String str = null;
        while((str = br.readLine()) != null && str.trim().length() > 0){
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          bw.write(count +"\t");
          bw.write( getContent(es1[2]+ ". " + es1[3]) + "\r\n");  
          count++;
        }
        br.close();
        bw.close();
        System.out.println("文档数：" + count);
    }
    
    /**读取LdaGibbsSampler用的文档矩阵**/
    public static int[][] readDocumentMetrix(String path) throws IOException{
    	BufferedReader br = new BufferedReader(new FileReader(new File(path_result_hdp)));
        int docCount = 0;
        while(br.readLine() != null){
        	docCount++;
        }
        br.close();
        
    	int[][] documents = new int[docCount][];
    	
    	br = new BufferedReader(new FileReader(new File(path_result_hdp)));
        String str = null;
        docCount = 0;
        while((str = br.readLine()) != null){
        	String[] es = str.split(" ");
        	documents[docCount] = new int[es.length];
        	for(int i = 0; i< es.length; i++){
        		documents[docCount][i] = Integer.parseInt(es[i]);
        	}
        	docCount ++;
        }
        br.close();
        
        return documents;
    }
    
    /**将W2I词索引文件，转换为只有词文件，且按词的ID升序排列**/
    public static void changeWord2Voc(String srcFile, String outFile) throws IOException{
    	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
    			new File(srcFile)),"UTF-8"));
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
    			new File(outFile)),"UTF-8"));
        int docCount = 0;
        HashMap<String, Integer> hsmap = new HashMap<String, Integer>();
        String line = null;
        while((line = br.readLine()) != null){
        	String[] es = line.split("\t");
        	if(es.length > 1){
        		hsmap.put(es[0], Integer.parseInt(es[1]));
        	}
        	docCount++;
        }
        br.close();
        
        List<Map.Entry<String,Integer>> list = SortHashMap.sortHM(hsmap, true);
        for(int i = 0, len = list.size(); i < len; i++){
        	bw.write(list.get(i).getKey() + "\r\n");
        }
        bw.close();
    }
    
    /**
     * 获得词汇，去掉高频低频
     * @throws IOException 
     */
    public static void getVocab(String srcFile, String outFile) throws IOException{
      File file = new File(srcFile);
        if (!file.exists()) {
            System.out.println("file " + file + " is not existed, exit");
            return;
        }
        Map<String, Integer> map = new HashMap();
        Map<String, Integer> cipinMap = new HashMap();//词频
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str = null;
        int wordIndex = 0;
        br.readLine();
        int totalCipin = 0;//总词频
        while((str = br.readLine()) != null && !str.trim().equals("")){
          String[] es1 = str.split("\t");
          if(es1.length < 3){
            continue;
          }
          String[] es = (es1[2] +". " + es1[3]).split(" ");
//          String[] es = str.split(" ");
          for(int i = 0; i< es.length; i++){
        	  String e = es[i].toLowerCase();
        	  if(e.endsWith(".") || e.endsWith(",")){
        		  e = e.substring(0, e.length() - 1);
        	  }
            if(!RemoveStopwords.isStopword(e) && !map.containsKey(e)
            		&& es[i].indexOf("锟斤拷") < 0){
//              System.out.println(wordIndex + "  "+es[i].toLowerCase());
              map.put(e, wordIndex++);
              cipinMap.put(e, 1);
              totalCipin++;
            }
            else if(map.containsKey(e)){
            	cipinMap.put(e, cipinMap.get(e) + 1);
            	totalCipin++;
            }
          }
        }
        br.close();
      
        int highThreds = printfWord2Int(cipinMap);
        bw = new BufferedWriter(new FileWriter(new File(outFile)));
        System.out.println("总词数： " + cipinMap.size());
        int count = 0;
        for(Map.Entry<String, Integer> entry : cipinMap.entrySet()){
        	if(entry.getValue() > lowFreq && entry.getValue() < highThreds){//去掉高频低频
        		bw.write(entry.getKey());
                bw.write("\r\n");
                count++;
        	}
        }
        System.out.println("过滤的高低频后剩余词数： " + count);
        bw.close();
        wordCount = map.size();
    }
}