package utils.corpus;

/**
 * 词的状态
 * @author hp
 *
 */
public class WordState {
	public int termIndex;//词索引
	public int tableAssignment;//词所赋予的桌子
	
	public WordState(int wordIndex, int tableAssignment){
		this.termIndex = wordIndex;
		this.tableAssignment = tableAssignment;
	}
}
