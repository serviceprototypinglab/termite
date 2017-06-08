package app;

import ch.zhaw.splab.podilizerproc.annotations.*;

public class Test{
	public static void main(String[] args){
		System.out.println("Main method has been started.");
		for (int i = 0; i < 2; i++) {
			testMethod(i, 5, "String");
		}
		System.out.println("Main method has been finished");
	}

	@Lambda(timeOut = 60, memorySize = 512)
	public static void testMethod(int i, int j, String s){
		System.out.println("Lambda no." + i + "; Args sum = " + (i + j) + "; String param: " + s);
	}
}