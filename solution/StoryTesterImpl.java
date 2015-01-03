package solution;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import provided.GivenNotFoundException;
import provided.StoryTester;
import provided.ThenNotFoundException;
import provided.WhenNotFoundException;
import provided.WordNotFoundException;

public class StoryTesterImpl implements StoryTester {
	
	private Object mTestClassInstance;
	private String mStory;
	private String mLastGivenStep;
	private String mLastStep;
	
	// THE 2 PUBLIC testOnHirarchy AND testOnNested
	/////////////////////////////////////////////////////////////////////////
	
	@Override
	public void testOnHirarchy(String story, Class<?> testClass) throws Exception {
		initLocalStory(story);
		MakeTestClassInstance(testClass);
		run(testClass);
	}
	
	private void run(Class<?> testClass) throws StoryTestExceptionImpl, WordNotFoundException{
		while(true){
			String step = getNexStoryStep();
			if(step.equals("EOF")) break;
			runStep(step, testClass);
		}
	}

	@Override
	public void testOnNested(String story, Class<?> testClass) throws Exception {

		initLocalStory(story);
		String firstGivenStep = getFirstStoryStep(story);
		Class<?> nestedClass = findNestedClassWithFirstGiven(firstGivenStep,testClass);
		run(nestedClass);
	}
	
	// CORE FUNCTIONS 
	/////////////////////////////////////////////////////////////////////////
	
	/**
	 * this method looks for a the annotated method in the 
	 * current class and its super classes
	 * if not found throws exception
	 * @param step - the string describing the annotation "When..."
	 * @param testClass - the class to look for the method in
	 * @return the method to run
	 * @throws WordNotFoundException
	 */
	private Method findAnnotatedMethodInHirarchy(String step, Class<?> testClass) 
			throws WordNotFoundException{
		
		String arr[] = step.split(" ", 2);
		String stepKeyWord = arr[0];
		String stepStrValue = arr[1];
		stepStrValue = stepStrValue.substring(0, stepStrValue.lastIndexOf(" "));
		
		// go over all method and look for the annotation 
		for (Method m : testClass.getMethods() ){
			String methodAnnotationValue = "";
			
			if(stepKeyWord.equals("Given") && m.isAnnotationPresent(Given.class)){
				Given a = m.getAnnotation(Given.class);
				methodAnnotationValue = a.value();
			}
			else if(stepKeyWord.equals("When") && m.isAnnotationPresent(When.class)){
				When a = m.getAnnotation(When.class);
				methodAnnotationValue = a.value();
			}
			else if(stepKeyWord.equals("Then") && m.isAnnotationPresent(Then.class)){
				Then a = m.getAnnotation(Then.class);
				methodAnnotationValue = a.value();
			}
			else continue;
	
			// if the annotation is the same as step return this method
			methodAnnotationValue = methodAnnotationValue.substring(0, methodAnnotationValue.lastIndexOf(" "));
			
			if(stepStrValue.equals(methodAnnotationValue)) return m;
			else continue;
		}
		
		// if the method was not found then we throw the exception
		if(stepKeyWord.equals("Given"))
			throw new GivenNotFoundException();
		else if(stepKeyWord.equals("When"))
			throw new WhenNotFoundException();
		else if(stepKeyWord.equals("Then") )
			throw new ThenNotFoundException();
		else 
			throw new WordNotFoundException();		
	}

	private Class<?> findNestedClassWithFirstGiven(String givenStep, Class<?> testClass) throws WordNotFoundException {
		Class<?> currClass = testClass;
		MakeTestClassInstance(testClass);
		while(true){
			try{
				findAnnotatedMethodInHirarchy(givenStep,currClass);
				return currClass;
			}catch (WordNotFoundException e){
				Class<?>[] currClassInnerClass = currClass.getDeclaredClasses();
				if(currClassInnerClass.length < 1)
					throw e;
				else{
					Class<?> outer = currClass;
					currClass = currClassInnerClass[0];
					
					Constructor<?> ctor = null;
					try {
						ctor = currClass.getDeclaredConstructor(outer);
						Object innerInstance = ctor.newInstance(mTestClassInstance);
						mTestClassInstance = innerInstance;
					
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException | 
							NoSuchMethodException | SecurityException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	private void manageCurrStep(String step, Class<?> testClass) throws StoryTestExceptionImpl, WordNotFoundException{
		if(getAnnotationNameFromStep(step).equals("Given"))
			resetLastGivenStep(step);
		if(getAnnotationNameFromStep(step).equals("When")
				&& getAnnotationNameFromStep(mLastStep).equals("Then"))	
			runStep(mLastGivenStep,testClass);
		mLastStep = step;
	}
	
	private void runStep(String step, Class<?> testClass) throws WordNotFoundException, StoryTestExceptionImpl{
		
		manageCurrStep(step,testClass);
		
		// if this method fails to find method the correct exception will be thrown
		Method m = findAnnotatedMethodInHirarchy(step,testClass);
		
		try{
			String s = getParamFromStep(step);
			
			Class<?>[] paramTypes = m.getParameterTypes();
			if(paramTypes[0].equals(String.class)){
				m.invoke(mTestClassInstance, s);
			}
			else{
				Integer i = intParamfromString(s);
				if(i!=null) m.invoke(mTestClassInstance, i);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new StoryTestExceptionImpl();
		}
	}
	
	// SMALL AUXILARY FUNCTIONS
	/////////////////////////////////////////////////////////////////////////
		
	private void initLocalStory(String story){
		mStory = story;
	}
	
	/** 
	 * responsible for creating an instance of the testClass
	 * because you need an instance to invoke the methods
	 * @param testClass 
	 */
	private void MakeTestClassInstance(Class<?> testClass){
		 try {
			 mTestClassInstance = testClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * save the last seen string "Given ..."
	 * used to reset the testClass instance between Then and When statments.
	 * @param step
	 */
	private void resetLastGivenStep(String step) {
		mLastGivenStep = step;
	}

	/**
	 * very much like Perl :)
	 * returns the next line of the story.
	 * when done returns the string "EOF"
	 * @return story line
	 */
	private String getNexStoryStep(){
		if (mStory.equals("")) return "EOF";
		String arr[] = mStory.split("\n", 2);
		try{mStory = arr[1];}
		catch(Exception e){mStory="";}
		return arr[0];
	}
	
	private String getFirstStoryStep(String story){
		String arr[] = story.split("\n", 2);
		return arr[0];
	}
	
	/**
	 * extracts the string parameter from the string of the story
	 * @param step
	 * @return
	 */
	private String getParamFromStep(String step){
		return step.substring(step.lastIndexOf(" ")+1);
	}
	
	/**
	 * translates string parameter "13" into integer.
	 * returns null if the string is illegal: "13a"
	 * @param param
	 * @return
	 */
	private Integer intParamfromString(String param){
		try{
			return Integer.parseInt(param);
		} catch ( NumberFormatException e ){
			return null;
		}
	}
	
	private String getAnnotationNameFromStep(String step){
		String arr[] = step.split(" ", 2);
		return arr[0];
	}
	/////////////////////////////////////////////////////////////////////////
	// END OF AUX FUNCS

}
