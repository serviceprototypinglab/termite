package app;

import ch.zhaw.splab.podilizerproc.annotations.*;

public class Test{
	public static void main(String[] args){
		System.out.println("Main method has been started.");
		for (int i = 0; i < 2; i++) {
			testMethod(i, 5, "String");
		}
		sum(4,5);
		System.out.println("Main method has been finished");
	}
	//endpoint defining is possible, format: endPoint = "http://<hostname>/"(or "https://<hostname>/")
	@Lambda(timeOut = 60, memorySize = 512)
	public static void testMethod(int i, int j, String s){
		System.out.println("Lambda no." + i + "; Args sum = " + (i + j) + "; String param: " + s);
	}
	@Lambda()
	public static int sum(int a1, int a2){
		System.out.println("a1 + a2 = " + (a1 + a2));
		return a1 + a2;
	}
}