package tests;

public class Person {
	private int age;
	private boolean isTired;
	
	public Person(int age) {
		this.age = age;
		this.isTired = false;
	}
	
	public void celebrate(int hours) {
		isTired = hours > 3 && age > 20;
	}
	
	public void rest(int hours) {
		isTired = (hours < 10);
	}
	
	public String howAreYou() {
		return isTired ? "Tired" : "Fine";
	}
}
