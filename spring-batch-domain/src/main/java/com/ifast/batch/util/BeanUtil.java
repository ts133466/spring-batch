package com.ifast.batch.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.Modifier;

public class BeanUtil {

	private static final Logger logger = LoggerFactory.getLogger(BeanUtil.class);

	public static String convertColumnToProperty(String column) {

		if(column == null) {
			return "";
		}

		String[] splits = column.split("\\.");
		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();
		for(String split : splits) {
			i++;
			if (splits.length == i) {
				stringBuilder.append(".").append(convert(split)).toString();
			} else {
				stringBuilder.append(".").append(split);
			}
		}
		return stringBuilder.substring(1);
		
	}
	
	private static String convert(String column) {
		
		String result = column.toLowerCase();

		int i = result.indexOf('_');
		while(i >= 0) {
			result = result.substring(0, i) + result.substring(i+1, i+2).toUpperCase() + result.substring(i + 2);
			i = result.indexOf('_', i + 1);
		}

		return result;

	}

	public static String compareBetween(Object fromObj,Object toObj) throws Exception
	{
		List<String> auditList = compareBetweenAndGenerateList(fromObj,toObj);
		String auditString = "";
		for ( int i=0;i<auditList.size();i++ ){
			auditString+= " "+auditList.get(i);
		}

		return auditString;
	}

	public static String compareBetween(Object fromObj,Object toObj,String[] fieldsToCompare) throws Exception 
	{
		List<String> auditList = compareBetweenAndGenerateList(fromObj,toObj,fieldsToCompare);
		String auditString = "";
		for ( int i=0;i<auditList.size();i++ ){
			auditString+= " "+auditList.get(i);
		}

		return auditString;
	}

	public static List<String> compareBetweenAndGenerateList(Object fromObj,Object toObj) throws Exception
	{

		if ( !(Class.forName(fromObj.getClass().getName())).isInstance(toObj)  ) {
			throw new ClassCastException("Wrong Class for the comparsion!");
		}

		Field[] fieldsArray = getAllDeclaredFieldsFromClass(toObj.getClass().getName(),null);
		
		if(fieldsArray != null) {
			String[] fieldsToCompare = new String[fieldsArray.length];
	
			for ( int i=0;i<fieldsArray.length;i++){
				fieldsToCompare[i]= fieldsArray[i].getName();
			}
	
			return compareBetweenAndGenerateList(fromObj,toObj,fieldsToCompare);
		}
		return new ArrayList<String>();
	}

	public static List<String> compareBetweenAndGenerateList(Object fromObj,Object toObj,String[] fieldsToCompare) throws Exception 
	{
		List<String> auditList = new ArrayList<String>();

		boolean displayOnly = false; // If toObj is null, just display the contents of the Bean.

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:aa");

		if (toObj ==null)
			displayOnly = true;
		else {
			if ( !(Class.forName(fromObj.getClass().getName())).isInstance(toObj)  ) {
				throw new ClassCastException("Wrong Class for the comparsion!");
			}
		}	

		Hashtable<String, String> fieldsToCompareHash = new Hashtable<String, String>();

		for ( int i=0;i<fieldsToCompare.length;i++){
			fieldsToCompareHash.put(fieldsToCompare[i],fieldsToCompare[i]);
		}

		String logTo=" -to- ";

		Field[] fieldsArray = getAllDeclaredFieldsFromClass(fromObj.getClass().getName(),null);
		if(fieldsArray != null) {
			for ( int i=0;i<fieldsArray.length;i++){
	
				Method getMethod = null;
				//Method setMethod = null;
	
				String fieldName = fieldsArray[i].getName();
				Class<?> fieldType = fieldsArray[i].getType();
	
				if (fieldsToCompareHash.get(fieldName)!=null){
	
					logger.debug("[compareBetweenAndGenerateList()] ============================");
					logger.debug("[compareBetweenAndGenerateList()] Comparing -> ["+fieldName+"]");
					logger.debug("[compareBetweenAndGenerateList()] Comparing -> ["+fieldType.getName()+"]");
					logger.debug("[compareBetweenAndGenerateList()] ============================");
	
					if (fieldType.getName().startsWith("com.f")){
						logger.debug("[compareBetweenAndGenerateList()] Ignoring -> ["+fieldType.getName()+"]");
						continue;
					}
					try {
						String paramMethodName = (fieldName.toUpperCase()).substring(0,1)+fieldName.substring(1,fieldName.length());
						getMethod = fromObj.getClass().getMethod("get"+paramMethodName, new Class<?>[] {});
						//setMethod = fromObj.getClass().getMethod("set"+(fieldName.toUpperCase()).substring(0,1)+fieldName.substring(1,fieldName.length()),new Class[] {fieldType});
	
						Object fromValue = getMethod.invoke(fromObj, new Object[] {});
						if (fromValue==null)
							fromValue = "null"; 
	
						logger.debug("[compareBetweenAndGenerateList()] Value -> ["+fieldName+"] FROM: "+fromValue);
	
						Object toValue = "";		
						if (!displayOnly){
							toValue = getMethod.invoke(toObj, new Object[] {});
							if (toValue==null)
								toValue = "null";
	
							logger.debug("[compareBetweenAndGenerateList()] Value -> ["+ fieldName +"] TO  : "+toValue);	
	
							try {
								if ( fromValue instanceof java.util.Date ){
									boolean isNull = false;
	
									if ("".equals(fromValue))
										fromValue = "null"; 
									if ("".equals(toValue))
										toValue = "null";
	
									if ("null".equals(fromValue))
										isNull = true; 
									if ("null".equals(toValue))
										isNull = true; 
	
									if (isNull)
									{
										if (!fromValue.equals(toValue)){
											auditList.add("["+fieldName+": " + fromValue + logTo + toValue +" ]");	
										}
									}
									else {
										logger.debug("[compareBetweenAndGenerateList()] Equals? -> ["+ (sdf.format( fromValue )).equals( (sdf.format(toValue)) ) +"]");
										logger.debug("[compareBetweenAndGenerateList()] fromValue -> ["+ sdf.format( fromValue ) +"]");
										logger.debug("[compareBetweenAndGenerateList()] toValue -> ["+ sdf.format( toValue ) +"]");
	
										if ( !sdf.format( fromValue ).equals( sdf.format(toValue) ) ) {
											auditList.add("["+fieldName+": " + sdf.format( fromValue ) + logTo + sdf.format(toValue) +" ]");	
										}
									}		
								}
								else if ( fromValue instanceof java.lang.String ){
									if ("".equals(fromValue))
										fromValue = "null"; 
									if ("".equals(toValue))
										toValue = "null";
	
									logger.debug("[compareBetweenAndGenerateList()] Equals? -> ["+ fromValue.equals(toValue) +"]");
	
									if (!fromValue.equals(toValue)){
										auditList.add("["+fieldName+": " + fromValue + logTo + toValue +" ]");	
									}	
								}
								else {
									logger.debug("[compareBetweenAndGenerateList()] Equals? -> ["+ fromValue.equals(toValue) +"]");
	
									if (!fromValue.equals(toValue)){
										auditList.add("["+fieldName+": " + fromValue + logTo + toValue +" ]");	
									}
								}
							}
							catch (Exception ex){
								if (!fromValue.equals(toValue)){
									auditList.add("["+fieldName+": " + fromValue + logTo + toValue +" ]");	
								}
							}
						}
						else {
							auditList.add("["+fieldName+": " + fromValue + " ]");	
						}
	
						// Remove after comparsion
						fieldsToCompareHash.remove(fieldName);
	
					} catch (NoSuchMethodException ex) {  
						logger.debug("[compareBetweenAndGenerateList()] Ignoring -> ["+fieldType.getName()+"] NoSuchMethodException");
					}
				}
			}
		}

		if (fieldsToCompareHash.size()>0)
		{
			Iterator<Map.Entry<String, String>> it = fieldsToCompareHash.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = it.next();
				System.out.println("############################################## KEYKEYKEY ######################## " + pair.getKey() + " = " + pair.getValue());
				it.remove(); // avoids a ConcurrentModificationException
			}
			throw new Exception("Error, more than 1 of the fields specified was not found in class for comparison! > " + fieldsToCompareHash.size());

		}

		return auditList;
	}

	public static Field[] getAllDeclaredFieldsFromClass(String className,Field[] array) throws Exception {

		Class<?> _class = Class.forName(className);

		Field[] fieldsArray = _class.getDeclaredFields();
		Field[] newtotalArray = null;
		
		List<Field> fields = new ArrayList<Field>(Arrays.asList(fieldsArray));
		
		Iterator<Field> itr = fields.iterator();
		
		while(itr.hasNext()) {
			Field field = itr.next();
			
			if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())){
				itr.remove();
			}
		}
		
		fieldsArray = fields.toArray(new Field[0]);

		if ( (array == null || array.length == 0 ) && (fieldsArray != null && fieldsArray.length > 0 ))
			newtotalArray = fieldsArray;
		else if ( (array != null && array.length > 0 ) && (fieldsArray == null || fieldsArray.length == 0 ) )
			newtotalArray = array;
		else if ( (array != null && array.length > 0 ) && (fieldsArray != null && fieldsArray.length > 0 ) ){
			newtotalArray = new Field[fieldsArray.length+array.length];
			for (int i=0;i<array.length;i++){
				newtotalArray[i] = array[i];
			}

			for (int i=array.length,j=0;j<fieldsArray.length;i++,j++){
				newtotalArray[i] = fieldsArray[j];
			}
		}

		if ( _class.getSuperclass() != null ) 
			return getAllDeclaredFieldsFromClass(_class.getSuperclass().getName(), newtotalArray) ;
		else
			return newtotalArray;
	}

}
