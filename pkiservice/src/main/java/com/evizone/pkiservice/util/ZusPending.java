package com.evizone.pkiservice.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import net.samcik.java.utils.Parser;

public class ZusPending {
	
	public String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    return stringBuilder.toString();
	}
	
	public static void main(String[] args) throws IOException {
		String file = new ZusPending().readFile("C:\\Users\\Tomek Samcik\\Desktop\\zus-pending.txt");
		float pln = 0;
		for (String line : file.split("\n")) {
			String val = Parser.extract(1, "(\\d+,\\d{2})pln", line);
			pln += Float.parseFloat(val.replace(',', '.'));
		}
		System.out.println(pln);

	}

}
