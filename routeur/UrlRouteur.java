package routeur;

import annotation.UrlAnnotation;
import java.lang.reflect.Method;

public class UrlRouteur 
{
    public static void handleRequest(String url, Object controller) throws Exception 
    {
        Class<?> clazz = controller.getClass();

        for (Method method : clazz.getDeclaredMethods()) 
        {
            if (method.isAnnotationPresent(UrlAnnotation.class)) 
            {
                UrlAnnotation annotation = method.getAnnotation(UrlAnnotation.class);
                if (annotation.value().equals(url)) {
                    System.out.println("-> URL trouvee : " + url);
                    method.invoke(controller);
                    return;
                }
            }
        }

        System.out.println("404 NOT FOUND : " + url);
    }
}
