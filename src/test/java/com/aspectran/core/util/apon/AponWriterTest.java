package com.aspectran.core.util.apon;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;

import com.aspectran.core.context.loader.config.AspectranConfig;

public class AponWriterTest {

	public static void main(String argv[]) {
		try {
			Reader fileReader = new FileReader(new File(argv[0]));
			
			AponReader reader = new AponReader(fileReader);
			
			Parameters aspectranConfig = new AspectranConfig();
			
			try {
				reader.read(aspectranConfig);
				
				System.out.println(aspectranConfig);
			} finally {
				reader.close();
			}
			
			StringWriter stringWriter = new StringWriter();
			
			AponWriter writer = new AponWriter(stringWriter);
			
			try {
				writer.write(aspectranConfig);
			} finally {
				writer.close();
			}
			
			System.out.println(stringWriter.toString());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
