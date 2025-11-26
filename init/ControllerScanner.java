// package init;

// import annotation.ControllerAnnotation;
// import annotation.UrlAnnotation;
// import java.io.File;
// import java.lang.reflect.Method;
// import java.util.HashMap;
// import java.util.Map;

// public class ControllerScanner 
// {
//     private static final Map<String, Method> urlMappings = new HashMap<>();
//     private static final Map<String, Method> dynamicUrlMappings = new HashMap<>();



//     public static void scanControllers(String basePackage) 
//     {
//         String path = basePackage.replace('.', '/');
//         File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());

//         scanDirectory(directory, basePackage);
//     }

//     private static void scanDirectory(File dir, String packageName) 
//     {
//         for (File file : dir.listFiles()) 
//         {
//             if (file.isDirectory()) 
//             {
//                 scanDirectory(file, packageName + "." + file.getName());
//             } 
//             else if (file.getName().endsWith(".class")) 
//             {
//                 String className = packageName + "." + file.getName().replace(".class", "");
//                 try {
//                     Class<?> clazz = Class.forName(className);
//                     if (clazz.isAnnotationPresent(ControllerAnnotation.class)) 
//                     {
//                         for (Method m : clazz.getDeclaredMethods()) 
//                         {
//                             if (m.isAnnotationPresent(UrlAnnotation.class)) 
//                             {
//                                 String url = m.getAnnotation(UrlAnnotation.class).value();
//                                 if (url.contains("{")) {
//                                     dynamicUrlMappings.put(url, m); // URL dynamique
//                                 } else {
//                                     urlMappings.put(url, m); // URL fixe
//                                 }

//                                 urlMappings.put(url, m);
//                                 System.out.println("→ " + url + " → " + clazz.getName() + "." + m.getName());
//                             }
//                         }
//                     }
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }
//         }
//     }

//     public static Map<String, Method> getUrlMappings() {
//         return urlMappings;
//     }

//     public static Map<String, Method> getDynamicUrlMappings() {
//         return dynamicUrlMappings;
//     }

// }



// -- SPRINT 6 TER
package init;

import annotation.ControllerAnnotation;
import annotation.UrlAnnotation;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class ControllerScanner {

    private static final Map<String, UrlDefinition> urlMappings = new HashMap<>();

    public static void scanControllers(String basePackage) {
        String path = basePackage.replace('.', '/');
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());
        scanDirectory(directory, basePackage);
    }

    private static void scanDirectory(File dir, String packageName) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");

                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(ControllerAnnotation.class)) {

                        for (Method m : clazz.getDeclaredMethods()) {

                            if (m.isAnnotationPresent(UrlAnnotation.class)) {
                                String pattern = m.getAnnotation(UrlAnnotation.class).value();

                                // extraire les variables {id}
                                List<String> vars = new ArrayList<>();
                                String regex = pattern;

                                for (String part : pattern.split("/")) {
                                    if (part.startsWith("{") && part.endsWith("}")) {
                                        String var = part.substring(1, part.length() - 1);
                                        vars.add(var);
                                        regex = regex.replace(part, "([^/]+)");
                                    }
                                }

                                UrlDefinition def = new UrlDefinition(pattern, regex, vars, m);
                                urlMappings.put(pattern, def);

                                System.out.println("→ " + pattern 
                                    + " (regex=" + regex 
                                    + ") → " + clazz.getName() + "." + m.getName());
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String, UrlDefinition> getUrlMappings() {
        return urlMappings;
    }

}
