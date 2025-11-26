package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;

import init.*;
import view.*;

public class FrontServlet extends HttpServlet 
{
    private boolean handleStaticFile(String relativePath, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    { 
        String realPath = getServletContext().getRealPath(relativePath); 
        File file = new File(realPath); 
        if (!file.exists() || !file.isFile()) 
        { 
            return false; 
        } 
        // JSP → doit être traité par Tomcat 
        if (relativePath.endsWith(".jsp")) 
        { 
            RequestDispatcher dispatcher = req.getRequestDispatcher(relativePath); 
            dispatcher.forward(req, resp); 
            return true; 
        } 
        // HTML statique 
        if (relativePath.endsWith(".html")) 
        { 
            resp.setContentType("text/html"); 
            try (FileInputStream fis = new FileInputStream(file); 
                OutputStream os = resp.getOutputStream()) { 
                    fis.transferTo(os); 
            } 
            return true; 
        } 
        return false; 
    }

    // --Sprint 6 : tsotra
    // public Object[] bindParameters(HttpServletRequest req, Method method) throws Exception 
    // {
    //     Class<?>[] paramTypes = method.getParameterTypes();
    //     Object[] args = new Object[paramTypes.length];

    //     java.lang.reflect.Parameter[] params = method.getParameters();

    //     for (int i = 0; i < paramTypes.length; i++) {
    //         String paramName = params[i].getName(); // nom du paramètre
    //         String value = req.getParameter(paramName);

    //         if (paramTypes[i] == int.class) {
    //             args[i] = value != null ? Integer.parseInt(value) : 0;
    //         } else if (paramTypes[i] == double.class) {
    //             args[i] = value != null ? Double.parseDouble(value) : 0.0;
    //         } else if (paramTypes[i] == boolean.class) {
    //             args[i] = value != null ? Boolean.parseBoolean(value) : false;
    //         } else {
    //             args[i] = value; // String ou autres objets
    //         }
    //     }

    //     return args;
    // }

    // --Sprint 6-bis : @RequestParam
    private Object[] bindParameters(HttpServletRequest req, Method method) 
    {
        Class<?>[] paramTypes = method.getParameterTypes();
        java.lang.reflect.Parameter[] params = method.getParameters();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(annotation.RequestParam.class)) {
                String paramName = params[i].getAnnotation(annotation.RequestParam.class).value();
                String value = req.getParameter(paramName);

                if (paramTypes[i] == String.class) {
                    args[i] = value;
                } else if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                    args[i] = value != null ? Integer.parseInt(value) : 0;
                } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                    args[i] = value != null ? Double.parseDouble(value) : 0.0;
                } else {
                    args[i] = value; // pour d’autres types, on peut améliorer plus tard
                }
            } else {
                args[i] = null; // valeur par défaut si pas annoté
            }
        }

        return args;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
    {
        String uri = req.getRequestURI();
        String context = req.getContextPath();

        String path = uri.startsWith(context) ? uri.substring(context.length()) : uri;

        if (path.isEmpty() || path.equals("/")) {
            path = "/"; // valeur par defaut
        }

        Map<String, Method> mappings = ControllerScanner.getUrlMappings();

        Method method = mappings.get(path);
        if (method != null) 
        {
            try {
                Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                Object result =  method.invoke(controller);
                
                String controllerName = method.getDeclaringClass().getSimpleName();
                String methodName = method.getName();

                if (result instanceof String) 
                {
                    resp.getWriter().println("Resultat retourne : " + result);
                }
                else if (result instanceof ModelView) 
                {
                    ModelView mv = (ModelView) result;
                    String view = mv.getView();

                    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) 
                    {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

                    // req.setAttribute("view", view);
                    RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + view);
                    dispatcher.forward(req, resp);
                } 
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().println("\n URL trouvee : " + path);
                resp.getWriter().println("-> Controleur : " + controllerName);
                resp.getWriter().println("-> Methode : " + methodName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            resp.getWriter().println("404 - Aucune methode trouvee pour " + path);
        }
    }


}

