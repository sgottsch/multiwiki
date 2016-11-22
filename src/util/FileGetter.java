package util;

import java.io.File;

public class FileGetter {

	private String realpath;

	public FileGetter(String realpath) {
		this.realpath = realpath;
	}
	
	public FileGetter(){
		this.realpath= new File("").getAbsolutePath();
	}

	public String path(String filename) {
		// TODO Auto-generated method stub
		if (realpath == null) {
			return filename;
		}
		return realpath + "/" + filename;
	}

}
