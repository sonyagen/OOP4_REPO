package solution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import provided.StoryTester;
import provided.WordNotFoundException;

public class StoryTesterImpl implements StoryTester {
	
	private Object mTestClassInstance;
	private String mLastGivenStep;
	private String mStory;
	
	private void MakeTestClassInstance(Class<?> testClass){
		 try {
			 mTestClassInstance = testClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void resetLastGivenStep(String step) {
		mLastGivenStep = step;
	}

	private String getNexStoryStep(){
		if (mStory.equals("")) return "";
		String arr[] = mStory.split("\n", 2);
		try{mStory = arr[1];}
		catch(Exception e){mStory="";}
		return arr[0];
	}
	
	private String getParamFromStep(String step){
		return step.substring(step.lastIndexOf(" ")+1);
	}
	
	private Integer intParamfromString(String param){
		try{
			return Integer.parseInt(param);
		} catch ( NumberFormatException e ){
			return null;
		}
	}
	
	//TODO throw correct exception
	private Method findAnnotatedMethodInClass(String step, Class<?> testClass) 
			throws WordNotFoundException{
		
		String arr[] = step.split(" ", 2);
		String stepKeyWord = arr[0];
		String stepStrValue = arr[1];
				
		for (Method m : testClass.getMethods() ){
			
			String methodAnnotationValue = "";
			
			if(stepKeyWord.equals("Given") && m.isAnnotationPresent(Given.class)){
				resetLastGivenStep(step);
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
	
			methodAnnotationValue = methodAnnotationValue.substring(0, methodAnnotationValue.lastIndexOf(" "));
			stepStrValue = stepStrValue.substring(0, stepStrValue.lastIndexOf(" "));
			if(stepStrValue.equals(methodAnnotationValue)) return m;

		}
		
		throw new WordNotFoundException();		
	}
	
	private void runStep(String step, Class<?> testClass) throws WordNotFoundException{
		
		Method m = findAnnotatedMethodInClass(step,testClass);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	private void initLocalVars(String story, Class<?> testClass){
		MakeTestClassInstance(testClass);
		mStory = story;
	}
	
	
	@Override
	public void testOnHirarchy(String story, Class<?> testClass) throws Exception {
		initLocalVars(story, testClass);
		
		while(true){
			String step = getNexStoryStep();
			if(step.equals("")) break;
			//TODO add if object needed to reset then reset object
			try{
				runStep(step, testClass);
			} catch (WordNotFoundException e){
				//runStep in higher class hierarchy
			}
		}
		
	}

	@Override
	public void testOnNested(String story, Class<?> testClass) throws Exception {
		initLocalVars(story, testClass);
	}
}
