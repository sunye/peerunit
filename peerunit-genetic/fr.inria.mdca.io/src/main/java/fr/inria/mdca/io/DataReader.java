package fr.inria.mdca.io;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.core.model.path.Path;

public class DataReader {
	


	public BaseModel readModel(File file) throws IOException{
		Scanner scanner = new Scanner(file);
		BaseModel m=new BaseModel();
		try {
			int i=0;
			while ( scanner.hasNextLine() ){
				String line=scanner.nextLine();
				if(i>1){
					m.addElement(this.parseModelElement(line));
				}
				else{
					this.parseModelInfo(line, m);
				}
				i++;
			}
		}
		finally {
			scanner.close();
		}
		return m;
	}
	private BaseModelElement parseModelElement(String line){
		String[] segments=line.split(":");
		String name=segments[0];
		String integer=segments[1].replace(" ", "");
		BaseModelElement m=new BaseModelElement();
		m.setElementsNum(Integer.parseInt(integer));
		m.setName(name);
		return m;
	}
	private void parseModelInfo(String line,BaseModel m){
		String value=line.split("=")[1];
		int v=Integer.parseInt(value);
		if(line.contains(BaseModel.ORDER)){
			m.setOrder(v);
		}
		if(line.contains(BaseModel.TWISE)){
			m.setTwise(v);
		}
	}
	
	
	public ArrayList<BaseInstance> readInstances(File file) throws IOException{
		Scanner scanner = new Scanner(file);
		ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
		try {
			int i=0;
			while ( scanner.hasNextLine() ){
				String line=scanner.nextLine();
				instances.add(this.parseInstance(line));
				i++;
			}
		}
		finally {
			scanner.close();
		}
		return instances;
	}
	
	private BaseInstance parseInstance(String line) {
		String[] values=line.split(" ");
		ArrayList<Integer> nums=new ArrayList<Integer>();
		for(String t:values){
			String s=t.replace(" ","");
			if(s.length()>0){
				nums.add(Integer.parseInt(s));
			}
		}
		BaseInstance instance=new BaseInstance(nums.size());
		for(int i=0;i<nums.size();i++){
			instance.getValues()[i]=nums.get(i);
		}
		return instance;
	}
	public ArrayList<Path> readPaths(File file) throws IOException{
		Scanner scanner = new Scanner(file);
		ArrayList<Path> paths=new ArrayList<Path>();
		ArrayList<String> pathGroup=new ArrayList<String>();
		try {
			int i=0;
			while ( scanner.hasNextLine() ){
				String line=scanner.nextLine();
				if(line.contains("+")){
					paths.add(this.parsePath(pathGroup));
					pathGroup.clear();
				}
				else{
					pathGroup.add(line);
				}
				i++;
			}
			paths.add(this.parsePath(pathGroup));
			pathGroup.clear();
		}
		finally {
			scanner.close();
		}
		return paths;
	}
	private Path parsePath(ArrayList<String> pathGroup) {
		Path p=new Path(pathGroup.size());
		int i=0;
		for(String t:pathGroup){
			this.parsePath(t,p,i);
		
			i++;
		}
		return p;
	}
	private void parsePath(String t, Path p,int index) {
		String[] info=t.split(":");
		String[] values=info[1].split(" ");
		p.getIndexes()[index]=Integer.parseInt(info[0].replace(" ", ""));
		for(String v:values){
			if(v.replace(" ", "").length()>0)
				p.getValues().get(index).add(Integer.parseInt(v.replace(" ", "")));
		}
	}
}
