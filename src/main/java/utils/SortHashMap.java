package utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SortHashMap {
	
	//按值降序排列hashmap
	public static List<Map.Entry<Integer,Double>> sortDescHM(HashMap map, final boolean bAsc){
		//这里将map.entrySet()转换成list
	    List<Map.Entry<Integer,Double>> list = new ArrayList<Map.Entry<Integer,Double>>(map.entrySet());
	    //然后通过比较器来实现排序
	    Collections.sort(list,new Comparator<Map.Entry<Integer,Double>>() {
	      public int compare(Entry<Integer,Double> o1,
	              Entry<Integer,Double> o2) {
	    	  if(!bAsc){//降序排序
	    		  return o2.getValue().compareTo(o1.getValue());
	    	  }
	    	  return o1.getValue().compareTo(o2.getValue());//升序排序
	      }
	    });
	    return list;
	}
	
	//按值降序排列hashmap
	public static List<Map.Entry<Integer,Float>> sortDescHMFloat(HashMap map, final boolean bAsc){
		//这里将map.entrySet()转换成list
	    List<Map.Entry<Integer,Float>> list = new ArrayList<Map.Entry<Integer,Float>>(map.entrySet());
	    //然后通过比较器来实现排序
	    Collections.sort(list,new Comparator<Map.Entry<Integer,Float>>() {
	      public int compare(Entry<Integer,Float> o1,
	              Entry<Integer,Float> o2) {
	    	  if(!bAsc){//降序排序
	    		  return o2.getValue().compareTo(o1.getValue());
	    	  }
	    	  return o1.getValue().compareTo(o2.getValue());//升序排序
	      }
	    });
	    return list;
	}
	
	/**
	 * 按值排列hashmap
	 * @param map
	 * @param bAsc 是否升序
	 * @return
	 */
	public static List<Map.Entry<String,Integer>> sortHM(HashMap<String,Integer> map, final boolean bAsc){
		//这里将map.entrySet()转换成list
	    List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
	    //然后通过比较器来实现排序
	    Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
	      public int compare(Entry<String,Integer> o1,
	              Entry<String,Integer> o2) {
	    	  if(!bAsc){//降序排序
	    		  return o2.getValue().compareTo(o1.getValue());
	    	  }
	    	  return o1.getValue().compareTo(o2.getValue());//升序排序
	      }
	    });
	    return list;
	}
		
	//按值降序排列hashmap
	public static List<Map.Entry<Integer,Integer>> sortDescIntHM(HashMap<Integer, Integer> map, final boolean bAsc){
		//这里将map.entrySet()转换成list
	    List<Map.Entry<Integer,Integer>> list = new ArrayList<Map.Entry<Integer,Integer>>(map.entrySet());
	    //然后通过比较器来实现排序
	    Collections.sort(list,new Comparator<Map.Entry<Integer,Integer>>() {
	      public int compare(Entry<Integer,Integer> o1,
	              Entry<Integer,Integer> o2) {
	    	  if(!bAsc){//降序排序
	    		  return o2.getValue().compareTo(o1.getValue());
	    	  }
	    	  return o1.getValue().compareTo(o2.getValue());//升序排序
	      }
	    });
	    return list;
	}
}
