package utils.corpus;

/**
 * 文档状态信息
 * @author hp
 *
 */
public class DOCState {
	public int docID;
	public int docLen, tablesNum;
	public int[] tableToTopic;//桌子的主题 赋值
	public int[] wordCountByTable;//每个桌子的词数
	public WordState[] words;

	public DOCState(int[] instance, int docID) {
		this.docID = docID;
	    tablesNum = 0;
	    docLen = instance.length;
	    words = new WordState[docLen];	
	    wordCountByTable = new int[2];
	    tableToTopic = new int[2];
		for (int position = 0; position < docLen; position++) 
			words[position] = new WordState(instance[position], -1);
	}


	public void defragment(int[] kOldToKNew) {
	    int[] tOldToTNew = new int[tablesNum];
	    int t, newtablesNum = 0;
	    for (t = 0; t < tablesNum; t++){
	        if (wordCountByTable[t] > 0){
	            tOldToTNew[t] = newtablesNum;
	            tableToTopic[newtablesNum] = kOldToKNew[tableToTopic[t]];
	            swap(wordCountByTable, newtablesNum, t);
	            newtablesNum ++;
	        } else 
	        	tableToTopic[t] = -1;
	    }
	    tablesNum = newtablesNum;
	    for (int i = 0; i < docLen; i++)
	        words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
	}
	
	public static void swap(int[] arr, int arg1, int arg2){
		   int t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	public static void swap(int[][] arr, int arg1, int arg2) {
		   int[] t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
}
