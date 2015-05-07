package net.javacoding.xsearch.utility;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.bsf.BSFManager;

/**
 * ScriptingUtil s = new ScriptingUtil();
 * s.set("variableName", someObject);
 * ...
 * s.setBaseDirectoryFile(...); //optional, if empty, use absolute path
 * s.evalFile("adfs.rb");
 *
 *
 */
public class ScriptingUtil {
	Map<String, Object> m;
	File baseDirectoryFile = null;
	public ScriptingUtil(){
		m = new HashMap<String, Object>();
	}
	public void setBaseDirectoryFile(File basedir){
		baseDirectoryFile = basedir;
	}
	public void set(String n, Object v){
		m.put(n,v);
	}
	public Object evalFile(File file) throws Exception {
		return evalFile(m,file);
	}
	public Object evalFile(String filename) throws Exception {
		return evalFile(m,(baseDirectoryFile==null? new File(filename): new File(baseDirectoryFile, filename)));
	}

	static{
        // JRuby must be registered in BSF; jruby.jar and bsf.jar must be on classpath.
        BSFManager.registerScriptingEngine("ruby",  "org.jruby.javasupport.bsf.JRubyEngine",  new String[]{"rb"});
	}
	public static Object eval(Map<String, Object> map, String code) throws Exception {
        BSFManager manager = new BSFManager();
        // Make the variable myUrl available from Ruby
        Set<String> keys = map.keySet();
        Iterator<String> keyIter = keys.iterator();
        while (keyIter.hasNext()) {
           String key = keyIter.next();
           Object value = map.get(key);
           manager.declareBean(key, value, value.getClass());
        }
        return manager.eval("ruby", "(java)", 1, 1, code);
	}
	public static Object evalFile(Map<String, Object> map, File file) throws Exception {
		return eval(map, FileUtil.readFile(file));
	}
	public static Object evalFile(Map<String, Object> map, String filename) throws Exception {
		return evalFile(map, new File(filename));
	}
  	public static void main(String[] args) throws Exception {
  		Map<String, Object> m = new HashMap<String, Object>();
  		m.put("myUrl", new URL("http://www/jruby.org"));
  		String result = (String) ScriptingUtil.eval(m,"if $myUrl.defaultPort< 1024 then " +
  	    		"'System port' else 'User port' end");
        System.out.println(result);
        String[] s = {"this is great", "abc"};
  		m.put("s", s);
  		result = (String) ScriptingUtil.eval(m,
  				"$s[0] += 'ly improved'");
        System.out.println("returned:"+result);

        System.out.println("actual:");
        for(int i=0;i<s.length;i++){
            System.out.println(s[i]);
        }
  	}
}
