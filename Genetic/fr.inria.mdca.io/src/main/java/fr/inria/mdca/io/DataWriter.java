package fr.inria.mdca.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.path.Path;

public class DataWriter {
	public boolean writeModel(BaseModel model,File file) throws IOException{
		FileOutputStream f0=new FileOutputStream(file,true);
		f0.write(model.toString().getBytes());
		f0.close();
		return true;
	}
	public boolean writeInstances(ArrayList<BaseInstance> instances,File file) throws IOException{
		FileOutputStream f0=new FileOutputStream(file,true);
		StringBuffer buffer=new StringBuffer();
		int j=0;
		int lim=instances.size();
		for(BaseInstance i:instances){
			buffer.append(i.toString());
			if(j<lim-1){
				buffer.append("\n");
			}
			j++;
		}
		f0.write(buffer.toString().getBytes());
		f0.close();
		return true;
	}
	public boolean writePaths(ArrayList<Path> paths,File file) throws IOException{
		FileOutputStream f0=new FileOutputStream(file,true);
		StringBuffer buffer=new StringBuffer();
		int j=0;
		int lim=paths.size();
		for(Path i:paths){
			buffer.append(i.toString());
			if(j<lim-1){
				buffer.append("\n+\n");
			}
			j++;
		}
		f0.write(buffer.toString().getBytes());
		f0.close();
		return true;
	}
	public boolean write(BaseModel model,File file) throws IOException{
		return this.writeModel(model, file);
	}

	public boolean write(ArrayList<BaseInstance> instance,File file) throws IOException{
		return this.writeInstances(instance, file);
	}

}
