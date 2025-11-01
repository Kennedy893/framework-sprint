package init;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import annotation.ControllerAnnotation;
import annotation.UrlAnnotation;

public class ControllerScanner 
{
    private static final Map<String, Method> urlMappings = new HashMap<>();

    public static void scanControllers(String basePackage) 
    {
        String path = basePackage.replace('.', '/');
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());

        scanDirectory(directory, basePackage);
    }

    private static void scanDirectory(File dir, String packageName) 
    {
        for (File file : dir.listFiles()) 
        {
            if (file.isDirectory()) 
            {
                scanDirectory(file, packageName + "." + file.getName());
            } 
            else if (file.getName().endsWith(".class")) 
            {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(ControllerAnnotation.class)) 
                    {
                        for (Method m : clazz.getDeclaredMethods()) 
                        {
                            if (m.isAnnotationPresent(UrlAnnotation.class)) 
                            {
                                String url = m.getAnnotation(UrlAnnotation.class).value();
                                urlMappings.put(url, m);
                                System.out.println("→ " + url + " → " + clazz.getName() + "." + m.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String, Method> getUrlMappings() {
        return urlMappings;
    }
}
