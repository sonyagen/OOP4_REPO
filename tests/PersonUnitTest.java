package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tests.PersonStoryTest;
import solution.StoryTesterImpl;
import provided.StoryTestException;
import provided.StoryTester;

/**
 * This is a minimal test. Write your own tests!
 * Please don't submit your tests!
 */
public class PersonUnitTest {
	private StoryTester tester;
	private String goodStory;
	private String badStory;
	private String derivedStory;
	private String nestedStory;
	private Class<?> testClass;
	private Class<?> derivedTestClass;
	
	@Before
	public void setUp() throws Exception {
		goodStory = "Given a person of age 13\n"
				+ "When the person celebrates, and the number of hours is 5\n"
				+ "Then the person feels Fine";
		
		badStory = "Given a person of age 13\n"
				+ "When the person celebrates, and the number of hours is 5\n"
				+ "Then the person feels Tired";
		
		derivedStory = "Given a person of age 22\n"
				+ "When the person celebrates, and the number of hours is 5\n"
				+ "When the person rests, and the number of hours is 13\n"
				+ "Then the person feels Fine";
		
		nestedStory = "Given a person that his age is 13\n"
				+ "When the person celebrates, and the number of hours is 5\n"
				+ "Then the person feels Fine";
		
		testClass = PersonStoryTest.class;
		derivedTestClass = PersonStoryDerivedTest.class;
		tester = new StoryTesterImpl();
	}

	@Test
	public void test() throws Exception {
		
		try {
			tester.testOnHirarchy(goodStory, testClass);
			Assert.assertTrue(true);
		} catch (StoryTestException e) {
			Assert.assertTrue(false);
		}
		
		try {
			tester.testOnNested(badStory, testClass);
			Assert.assertTrue(false);
		} catch (StoryTestException e) {
			Assert.assertTrue(true);
			Assert.assertEquals("Then the person feels Tired", e.getStep());
			Assert.assertEquals("Tired", e.getStoryExpected());
			Assert.assertEquals("Fine", e.getTestResult());
		}
	
		try {
			tester.testOnNested(derivedStory, derivedTestClass);
			Assert.assertTrue(true);
		} catch (StoryTestException e) {
			Assert.assertTrue(false);
		}
		
		try {
			tester.testOnNested(nestedStory, derivedTestClass);
			Assert.assertTrue(true);
		} catch (StoryTestException e) {
			Assert.assertTrue(false);
		}
	}
}
