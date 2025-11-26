package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String relativePath = uri.substring(context.length());

        // --- CAS 1 : FICHIER STATIC (HTML / JSP) ---------------------------------
        if (handleStaticFile(relativePath, req, resp)) {
            return;
        }


        // --- CAS 2 : URL MAPPeE PAR ANNOTATION (CONTROLLER SCANNER) --------------
        String path = relativePath.isEmpty() ? "/" : relativePath;


        // SPRINT 6 et 6 BIS --------------------------------------------
        // Map<String, Method> mappings = ControllerScanner.getUrlMappings();
        // Method method = mappings.get(path);

        // if (method != null) 
        // {
        //     try {
        //         Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
        //         // Object result = method.invoke(controller);

        //         String controllerName = method.getDeclaringClass().getSimpleName();
        //         String methodName = method.getName();

        //         //  Injection automatique des paramètres (Méthode 1)
        //         Object[] args = bindParameters(req, method);

        //         //  Appel de la méthode avec les arguments injectés
        //         Object result = method.invoke(controller, args);

        //         // --- Si le resultat est un texte simple ---------------------------
        //         if (result instanceof String) 
        //         {
        //             resp.setContentType("text/plain;charset=UTF-8");
        //             resp.getWriter().println("Resultat retourne : " + result);
        //         }

        //         // --- Si le resultat est un ModelView ------------------------------
        //         else if (result instanceof ModelView) 
        //         {
        //             ModelView mv = (ModelView) result;
        //             // String view = mv.getView();

        //             // // Injecte les donnees dans la requete
        //             // for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
        //             //     req.setAttribute(entry.getKey(), entry.getValue());
        //             // }

        //             // RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + view);
        //             // dispatcher.forward(req, resp);
        //             // return;

        //             // -Sprint 6 - Injecte les données dans la requête
        //             for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
        //                 req.setAttribute(entry.getKey(), entry.getValue());
        //             }

        //             RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
        //             dispatcher.forward(req, resp);
        //             return;
        //         }

        //         // --- Affichage --------------------------------------------------------
        //         resp.setContentType("text/plain;charset=UTF-8");
        //         resp.getWriter().println("\n URL trouvee : " + path);
        //         resp.getWriter().println("-> Controleur : " + controllerName);
        //         resp.getWriter().println("-> Methode : " + methodName);

        //         return;

        //     } catch (Exception e) {
        //         e.printStackTrace();
        //         resp.sendError(500, "Erreur interne du serveur : " + e.getMessage());
        //         return;
        //     }
        // }

        // // --- CAS 3 : Aucune URL trouvee 
        // resp.setContentType("text/plain;charset=UTF-8");
        // resp.getWriter().println("404 - Aucune methode trouvee pour " + path);

        // -------------------------------------------------------------------


        // SPRINT 6 TER ---------------------

        Map<String, UrlDefinition> defs = ControllerScanner.getUrlMappings();
        boolean matched = false;

        for (UrlDefinition def : defs.values()) {

            String regex = "^" + def.getRegex() + "$";

            if (path.matches(regex)) {
                matched = true;

                try {
                    Method method = def.getMethod();
                    Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                    String controllerName = method.getDeclaringClass().getSimpleName();
                    String methodName = method.getName();

                    // --- Extraire valeurs dynamiques {id} ---
                    Map<String, String> extracted = new HashMap<>();

                    String[] pathParts = path.split("/");
                    String[] patternParts = def.getPattern().split("/");

                    for (int i = 0; i < patternParts.length; i++) {
                        if (patternParts[i].startsWith("{")) {
                            String var = patternParts[i].substring(1, patternParts[i].length() - 1);
                            extracted.put(var, pathParts[i]);
                        }
                    }

                    // --- binder les paramètres ---

                    Parameter[] params = method.getParameters();
                    Object[] args = new Object[params.length];

                    for (int i = 0; i < params.length; i++) {
                        String name = params[i].getName();
                        String strValue = extracted.get(name);

                        if (params[i].getType() == int.class || params[i].getType() == Integer.class) {
                            args[i] = Integer.parseInt(strValue);
                        } 
                        else {
                            args[i] = strValue; // String par défaut
                        }
                    }

                    // --- Appeler la méthode ---
                    Object result = method.invoke(controller, args);

                    // --- Gérer le retour ---
                    // if (result instanceof String) {
                    //     resp.setContentType("text/plain;charset=UTF-8");
                    //     resp.getWriter().println(result);
                    //     return;
                    // }

                    // if (result instanceof ModelView) {
                    //     ModelView mv = (ModelView) result;

                    //     for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    //         req.setAttribute(entry.getKey(), entry.getValue());
                    //     }

                    //     RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
                    //     dispatcher.forward(req, resp);
                    //     return;
                    // }

                    // --- Affichage --------------------------------------------------------
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.getWriter().println("\n URL trouvee : " + path);
                    resp.getWriter().println("-> Controleur : " + controllerName);
                    resp.getWriter().println("-> Methode : " + methodName);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(500, "Erreur URL dynamique : " + e.getMessage());
                    return;
                }

            }
        }

        // Si aucun motif ne correspond
        if (!matched) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("404 - Aucune méthode trouvée pour " + path);
        }

    }



}

