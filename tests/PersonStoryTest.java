package tests;
import org.junit.Assert;

import tests.Person;
import solution.Given;
import solution.Then;
import solution.When;

public class PersonStoryTest {
	protected Person person;
	
	@Given("a person of age &age")
	public void aPerson(Integer age) {
		person = new Person(age);
	}
	
	@When("the person celebrates, and the number of hours is &hours")
	public void thePersonCelebrates(Integer hours) {
		person.celebrate(hours);
	}
	
	@Then("the person feels &feeling")
	public void thePersonFeels(String feeling) {
		Assert.assertEquals(feeling, person.howAreYou());
	}
}
