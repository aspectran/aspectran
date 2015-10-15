package com.aspectran.core.util.apon;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import com.aspectran.core.context.loader.config.AspectranConfig;

public class AponReaderTest {

	public static void main(String argv[]) {
		try {
			Reader fileReader = new FileReader(new File(argv[0]));
			AponReader reader = new AponReader(fileReader);
			
			try {
				Parameters aspectranConfig = new AspectranConfig();  
				
				reader.read(aspectranConfig);
				
				System.out.println(aspectranConfig);
			} finally {
				reader.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
