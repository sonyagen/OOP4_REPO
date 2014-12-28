package provided;

/**
 * You should throw this exception when there is an assertion error
 * when invoking a story-test-class's method 
 * (method annotated by the Then annotation).
 */
public abstract class StoryTestException extends Exception {
	private static final long serialVersionUID = 95576353840828036L;
	
	/**
	 * Returns a string representing the step. For example:
	 * "Then the person feels Tired".
	 */
	public abstract String getStep();
	
	/**
	 * Returns a string representing the expected value from the story.
	 * For example: "Tired".
	 */
	public abstract String getStoryExpected();
	
	/**
	 * Returns a string representing the actual value.
	 * For example: "Fine".
	 */
	public abstract String getTestResult();
}
