package fr.inria.mdca.core.model;

public class BaseInstance {
	
	private boolean changed=false;
	
	private int[] values;
	
	private int changedElement;
	
	private int index;
	
	private int age=0;
	
	public BaseInstance(BaseModel model){
		values=new int[model.getElements().size()];
	}
	public BaseInstance(int size){
		values=new int[size];
	}
	
	public void setValues(int[] values) {
		this.values = values;
	}
	/**
	 * get the values for this instance
	 * @return
	 */
	public int[] getValues() {
		return values;
	}

	/**
	 * in case of change in the solution set, this property must be set to trues
	 * @param changed
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	/**
	 * Can ask this question only once.
	 * @return
	 */
	public boolean isChanged() {
		if(this.changed){
			this.changed=false;
			return true;
		}
		return changed;
	}
	/**
	 * returns a new instance copy of the current instance
	 */
	public BaseInstance clone(){
		BaseInstance instance=new BaseInstance(this.values.length);
		for(int i=0;i<this.values.length;i++){
			instance.values[i]=this.values[i];
		}
		return instance;
	}
	/**
	 * change the element i by the value 
	 * @param i position of the bucket
	 * @param value new instance bucket value
	 */
	public void change(int i, int value){
		if(i>this.values.length-1){
			throw new IndexOutOfBoundsException("The selected element does not exist in this instance");
		}
		this.values[i]=value;
		this.setChanged(true);
		this.setChangedElement(i);	
	}
	
	
	public void setChangedElement(int changedElement) {
		this.changedElement = changedElement;
	}
	
	public int getChangedElement() {
		return changedElement;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getIndex() {
		return index;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getAge() {
		return age;
	}
	
	
	public String toString(){
		StringBuffer buff=new StringBuffer();
		int j=0;
		int lim=values.length;
		for(int i:this.values){
			buff.append(i);
			if(j<lim-1){
				buff.append(" ");
			}
			j++;
		}
		return buff.toString();
	}
}
