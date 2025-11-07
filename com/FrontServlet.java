package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;

import init.*;

public class FrontServlet extends HttpServlet 
{

    // @Override
    // protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    // {
    //     service(req, resp);
    // }

    // @Override
    // protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    // {
    //     String uri = req.getRequestURI();
    //     String context = req.getContextPath();
    //     String path = getServletContext().getRealPath(uri.substring(context.length()));

    //     File file = new File(path);
    //     resp.setContentType("text/html");

    //     if (file.exists() && file.isFile()) 
    //     {
    //         // Verifie si c’est un fichier statique (html, jsp, etc.)
    //         if (uri.endsWith(".html") || uri.endsWith(".jsp")) 
    //         {
    //             try (FileInputStream fis = new FileInputStream(file);
    //                  OutputStream os = resp.getOutputStream()) {
    //                 fis.transferTo(os);
    //             }
    //         } 
    //         else {
    //             // Si c’est une ressource dynamique
    //             RequestDispatcher dispatcher = req.getRequestDispatcher(uri.substring(context.length()));
    //             dispatcher.forward(req, resp);
    //         }
    //     } 
    //     else {
    //         resp.getWriter().println("<h3>Requete URL : " + uri + "</h3>");
    //     }
    // }

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
                Object result = method.invoke(controller);
                
                String controllerName = method.getDeclaringClass().getSimpleName();
                String methodName = method.getName();

                if (result instanceof String) 
                {
                    resp.getWriter().println("Resultat retourne : " + result);
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

