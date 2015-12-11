package net.javacoding.xsearch.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


public class U {

    /**
     * If the pValue is empty( null or ""), return default value
     * Otherwise, return pValue
     *
     * @param pValue
     * @param defaultValue
     */
    public static String getText(String pValue, String defaultValue){
        String sValue = defaultValue;
        if(!U.isEmpty(pValue)){
            sValue = pValue;
        }
        return sValue;
    }

    /**
     * If the pValue is empty( null or ""), return default value
     * Otherwise, return if pValue==expectedValue
     *
     * @param pValue
     * @param expectedValue
     * @param defaultValue
     */
    public static boolean getBoolean(String pValue, String expectedValue, boolean defaultValue){
        boolean bValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                bValue = expectedValue.equals(pValue);
            }catch(Exception e){}
        }
        return bValue;
    }

    /**
     * If the pValue is empty( null or ""), return default value
     * Otherwise, return pValue's integer value
     *
     * @param pValue
     * @param defaultValue
     */
    public static int getInt(String pValue, int defaultValue){
        int iValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                iValue = Integer.parseInt(pValue);
            }catch(Exception e){}
        }
        return iValue;
    }

    public static Integer getInteger(String pValue){
        if(!U.isEmpty(pValue)){
            try{
                return new Integer(pValue);
            }catch(Exception e){
                return null;
            }
        }
        return null;
    }

    /**
     * If the pValue is empty( null or ""), return default value
     * Otherwise, return pValue's long value
     *
     * @param pValue
     * @param defaultValue
     */
    public static long getLong(String pValue, long defaultValue){
        long iValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                iValue = Long.parseLong(pValue);
            }catch(Exception e){}
        }
        return iValue;
    }

    public static double getDouble(String pValue, double defaultValue){
        double iValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                iValue = Float.parseFloat(pValue);
            }catch(Exception e){}
        }
        return iValue;
    }

    /**
     * If the pValue is empty( null or ""), return default value
     * Otherwise, return pValue's byte value
     *
     * @param pValue
     * @param defaultValue
     */
    public static byte getByte(String pValue, byte defaultValue){
        byte iValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                iValue = Byte.parseByte(pValue);
            }catch(Exception e){}
        }
        return iValue;
    }

    /**
     * return the cookie with the specific name.
     * If not found, return null
     *
     * @param req
     * @param cookieName
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName){
        javax.servlet.http.Cookie [] allcookies = req.getCookies();
        if (allcookies != null&&cookieName!=null) {
            for (int i = 0; i < allcookies.length ; i ++)
                if (allcookies[i].getName().equals(cookieName)){
                    return allcookies[i];
                }
        }
        return null;
    }
    public static boolean isEmpty(String x){
        return (x==null||x.trim().equals(""));
    }

    public static float getFloat(String pValue, float defaultValue) {
        float iValue = defaultValue;
        if(!U.isEmpty(pValue)){
            try{
                iValue = Float.parseFloat(pValue);
            }catch(Exception e){}
        }
        return iValue;
    }


    /**
     * Converts the pattern to a Perl 5 Regular Expression. This means that
     * every asterisk is replaced by a dot and an asterisk, every question mark
     * is replaced by a dot, an accent circunflex is prepended to the pattern
     * and a dollar sign is appended to the pattern.
     *
     * @param pattern
     *    the pattern to be converted, may not be <code>null</code>.
     *
     * @return
     *    the converted pattern, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>pattern == null</code>.
     *
     * @throws ParseException
     *    if provided simplePattern is invalid or could not be parsed.
     */
    public static String convertToPerl5RegularExpression(String pattern) {

       char[] contents = pattern.toCharArray();
       int size = contents.length;
       StringBuffer buffer = new StringBuffer(size * 2);
       char prevChar = (char) 0;

       for (int i= 0; i < size; i++) {
          char currChar = contents[i];

          if (currChar == '.') {
              buffer.append("\\.");
          } else if ((currChar == '*' || currChar == '?') && (prevChar == '*')) {
              // skip
          } else if ((currChar == '*') && (prevChar == '?')) {
              // skip
          } else if (currChar == '*') {
              buffer.append(".*");
          } else if (currChar == '?') {
              buffer.append('.');
          } else {
              buffer.append(currChar);
          }

          prevChar = currChar;
       }

       return buffer.toString();
    }
    
    public static String squeeze(String x){
    	if(x==null) return null;
    	return x.replaceAll(" ", "").replaceAll("\r","").replaceAll("\n","");
    }
    
    /**
     * Invokes a method containing no primitive types. If the method has primitive
     * types, see the {@link #invoke(Class, Object, String, Class[], Object[])
     * invoke(Class, Object, String, Class[], Object[])} method.
     * @param className the class of the object, for example 
     * <code>URLClassLoader.class</code>
     * @param obj the object containing the method to invoke
     * @param method the name of the method to invoke, for example
     * <code>"addURL"</code>
     * @param param the parameters, for example <code>new Object[]{url}</code>
     * @throws Exception the following exceptions can be thrown from this method: 
     * <code>NoSuchMethodException</code>,
     * <code>NullPointerException</code>,
     * <code>SecurityException</code>,
     * <code>IllegalAccessException</code>,
     * <code>IllegalArgumentException</code>,
     * <code>InvocationTargetException</code>,
     * <code>ExceptionInInitializerError</code>.
     * @see #invoke(Class, Object, String, Class[], Object[])
     */
    public static Object invoke(Class className, Object obj, String method, Object[] param) throws Exception {
      Class[] paramClass = new Class[param.length];
      for (int i = 0; i < param.length; i++) {
        paramClass[i] = param[i].getClass();
      }
      return invoke(className, obj, method, paramClass, param);
    }
    /**
     * Invokes a method containing primitive types. If the method has no primitive
     * types, see the {@link #invoke(Class, Object, String, Object[])
     * invoke(Class, Object, String, Object[])} method.
     * @param className the class of the object, for example 
     * <code>MyProgram.class</code>
     * @param obj the object containing the method to invoke
     * @param method the name of the method to invoke, for example
     * <code>"testMethod"</code>
     * @param paramClass the classes of the parameters, for exemple
     * <code>new Class[]{Integer.TYPE}</code>
     * @param param the parameters, for example <code>new Object[]{new
     * Integer(555)}</code>
     * @throws Exception the following exceptions can be thrown from this method: 
     * <code>NoSuchMethodException</code>,
     * <code>NullPointerException</code>,
     * <code>SecurityException</code>,
     * <code>IllegalAccessException</code>,
     * <code>IllegalArgumentException</code>,
     * <code>InvocationTargetException</code>,
     * <code>ExceptionInInitializerError</code>.
     * @see #invoke(Class, Object, String, Object[])
     */
    public static Object invoke(Class className, Object obj, String method, Class[] paramClass, Object[] param) throws Exception {
      Method m = className.getDeclaredMethod(method, paramClass);
      m.setAccessible(true);
      return m.invoke(obj, param);
    }
    
    /**
     * Save to string, so that it can be printed to log4j, instead of console
     */
    public static String getStatckTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return "------\r\n" + sw.toString() + "------\r\n";
    }

    public static Object newInstance(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
    	return Class.forName(className).newInstance();
    }
    public static Object newInstance(String className, Object... params) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException{
    	if(params==null){
    		return newInstance(className);
    	}

    	Class c = Class.forName(className);
        Class[] klasses = new Class[params.length];
        for(int i=0;i<klasses.length;i++){
        	klasses[i] = params.getClass();
        }
        Constructor constructor = c.getConstructor(klasses);
        return constructor.newInstance(params);
    }
    public static String toString(Object o){
    	if(o==null) return "";
    	if (o instanceof String) {
    		return (String)o;
		}
    	return o.toString();
    }
    public static String join(Collection s, String delimiter) {
        if(s==null)return "";
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    public static String toJson(Collection s){
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(join(s,","));
        sb.append("]");
        return sb.toString();
    }
    public static String nvl(String... values){
    	if (values == null) {
    		return null;
    	} else {
	        for(String v : values) {
	            if(!U.isEmpty(v)) {
	                return v;
	            }
	        }
    	}
        return null;
    }
    public static boolean equals(Object a, Object b) {
        if(a==null) {
            return b==null;
        }else {
            return a.equals(b);
        }
    }
}
