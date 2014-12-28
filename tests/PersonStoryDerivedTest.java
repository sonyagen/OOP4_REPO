package tests;

import solution.Given;
import solution.When;

public class PersonStoryDerivedTest extends PersonStoryTest {
	@When("the person rests, and the number of hours is &hours")
	private void thePersonRests(Integer hours) {
		person.rest(hours);
	}
	
	public class InnerClass extends PersonStoryTest {
		@Given("a person that his age is &age")
		public void aPerson(Integer age) {
			person = new Person(age);
		}
	}
}
