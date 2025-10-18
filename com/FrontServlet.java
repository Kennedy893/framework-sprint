package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FrontServlet extends HttpServlet 
{

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String path = getServletContext().getRealPath(uri.substring(context.length()));

        File file = new File(path);
        resp.setContentType("text/html");

        if (file.exists() && file.isFile()) 
        {
            // Vérifie si c’est un fichier statique (html, jsp, etc.)
            if (uri.endsWith(".html") || uri.endsWith(".jsp")) 
            {
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = resp.getOutputStream()) {
                    fis.transferTo(os);
                }
            } 
            else {
                // Si c’est une ressource dynamique
                RequestDispatcher dispatcher = req.getRequestDispatcher(uri.substring(context.length()));
                dispatcher.forward(req, resp);
            }
        } 
        else {
            resp.getWriter().println("<h3>Requete URL : " + uri + "</h3>");
        }
    }


}

