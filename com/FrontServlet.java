package com;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FrontServlet extends HttpServlet 
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException 
    {
        String url = req.getRequestURI();
        resp.getWriter().println("Requete URL : " + url);
    }
}

// javac -cp "C:\apache-tomcat-10.1.28\lib\servlet-api.jar" -d build\classes com\FrontServlet.java
// jar -cvf test_framework.jar -C build\classes  .
// copy test_framework.jar "..\test-framework\lib"