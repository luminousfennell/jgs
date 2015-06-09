package main;

import tests.testClasses.TestSubClass;
import analyzer.level2.HandleStmt;


public class Test {
	int field = 1;
	
	static int sField = 2;
   
    public static void main(String[] args) {
    	
    	int a1 = 22;
    	m();
    }
    
    public static int m() {
    	
    	TestSubClass ts = new TestSubClass();
    	
    	TestSubClass.sField = 2;
    	ts.sField = 3;
    	ts.pField = 1;

    	sField = 2;
    	
    	sField = ts.methodWithConstReturn();
    	
    	int a = 2;
    	int b = a;
    	int c = a + b;
    	return c;
    }
    
    // TODO note that every instanceMethod has a this reference at the beginning
    public void d(int x, int y , int z) {
    	
    }
    
    
    public void m2() {
    	
    	this.field = 2;
    	this.field = 3;
    	main.Test a = this;
    	int local = 5;

    	d(2, field, local);
    	
    	local = m();
    	System.out.println(local);
    }
    
    
}
