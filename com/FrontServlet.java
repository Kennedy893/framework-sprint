package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;

import init.*;
import view.*;

public class FrontServlet extends HttpServlet 
{
    private boolean handleStaticFile(String relativePath, HttpServletRequest req, HttpServletResponse resp) 
        throws IOException, ServletException 
    {
        String realPath = getServletContext().getRealPath(relativePath);
        File file = new File(realPath);

        if (!file.exists() || !file.isFile()) {
            return false;
        }

        // JSP → doit être traité par Tomcat
        if (relativePath.endsWith(".jsp")) {
            RequestDispatcher dispatcher = req.getRequestDispatcher(relativePath);
            dispatcher.forward(req, resp);
            return true;
        }

        // HTML statique
        if (relativePath.endsWith(".html")) {
            resp.setContentType("text/html");
            try (FileInputStream fis = new FileInputStream(file);
                OutputStream os = resp.getOutputStream()) {
                fis.transferTo(os);
            }
            return true;
        }

        return false;
    }


    public Object[] bindParameters(HttpServletRequest req, Method method) throws Exception 
    {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        java.lang.reflect.Parameter[] params = method.getParameters();

        for (int i = 0; i < paramTypes.length; i++) {
            String paramName = params[i].getName(); // nom du paramètre
            String value = req.getParameter(paramName);

            if (paramTypes[i] == int.class) {
                args[i] = value != null ? Integer.parseInt(value) : 0;
            } else if (paramTypes[i] == double.class) {
                args[i] = value != null ? Double.parseDouble(value) : 0.0;
            } else if (paramTypes[i] == boolean.class) {
                args[i] = value != null ? Boolean.parseBoolean(value) : false;
            } else {
                args[i] = value; // String ou autres objets
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

        Map<String, Method> mappings = ControllerScanner.getUrlMappings();
        Method method = mappings.get(path);

        if (method != null) 
        {
            try {
                Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                // Object result = method.invoke(controller);

                String controllerName = method.getDeclaringClass().getSimpleName();
                String methodName = method.getName();

                //  Injection automatique des paramètres (Méthode 1)
                Object[] args = bindParameters(req, method);

                //  Appel de la méthode avec les arguments injectés
                Object result = method.invoke(controller, args);

                // --- Si le resultat est un texte simple ---------------------------
                if (result instanceof String) 
                {
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.getWriter().println("Resultat retourne : " + result);
                }

                // --- Si le resultat est un ModelView ------------------------------
                else if (result instanceof ModelView) 
                {
                    ModelView mv = (ModelView) result;
                    // String view = mv.getView();

                    // // Injecte les donnees dans la requete
                    // for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    //     req.setAttribute(entry.getKey(), entry.getValue());
                    // }

                    // RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + view);
                    // dispatcher.forward(req, resp);
                    // return;

                    // -Sprint 6 - Injecte les données dans la requête
                    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
                    dispatcher.forward(req, resp);
                    return;
                }

                // --- Debug --------------------------------------------------------
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().println("\n URL trouvee : " + path);
                resp.getWriter().println("-> Controleur : " + controllerName);
                resp.getWriter().println("-> Methode : " + methodName);

                return;

            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(500, "Erreur interne du serveur : " + e.getMessage());
                return;
            }
        }


        // --- CAS 3 : Aucune URL trouvee ------------------------------------------
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("404 - Aucune methode trouvee pour " + path);
    }



}

