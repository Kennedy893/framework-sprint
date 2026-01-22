package a;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import a.annotation.JsonAnnotation;
import a.annotation.SessionAnnotation;
import a.init.ControllerScanner;
import a.init.UrlDefinition;
import a.util.JsonResponse;
import a.view.ModelView;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,   // 1 MB
    maxFileSize = 1024 * 1024 * 10,    // 10 MB
    maxRequestSize = 1024 * 1024 * 50  // 50 MB
)
public class FrontServlet extends HttpServlet 
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException 
    {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException 
    {
        handleRequest(req, resp);
    }
    
    private boolean handleStaticFile(String relativePath,
                                 HttpServletRequest req,
                                 HttpServletResponse resp)
        throws IOException, ServletException {

    // Ne traiter que les fichiers statiques
    if (!relativePath.matches(".*\\.(jsp|html|css|js|png|jpg|jpeg|gif)$")) {
        return false;
    }

    String realPath = getServletContext().getRealPath(relativePath);
    if (realPath == null) {
        return false;
    }

    File file = new File(realPath);
    if (!file.exists() || !file.isFile()) {
        return false;
    }

    // JSP → Tomcat
    if (relativePath.endsWith(".jsp")) {
        RequestDispatcher dispatcher = req.getRequestDispatcher(relativePath);
        dispatcher.forward(req, resp);
        return true;
    }

    // HTML statique
    if (relativePath.endsWith(".html")) {
        resp.setContentType("text/html;charset=UTF-8");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            fis.transferTo(os);
        }
        return true;
    }

    return false;
}


    private Object convertValue(Class<?> type, String value) 
    {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);

        return null; // type non géré
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
    // private Object[] bindParameters(HttpServletRequest req, Method method) 
    // {
    //     Class<?>[] paramTypes = method.getParameterTypes();
    //     java.lang.reflect.Parameter[] params = method.getParameters();
    //     Object[] args = new Object[paramTypes.length];

    //     for (int i = 0; i < params.length; i++) {
    //         if (params[i].isAnnotationPresent(annotation.RequestParam.class)) {
    //             String paramName = params[i].getAnnotation(annotation.RequestParam.class).value();
    //             String value = req.getParameter(paramName);

    //             if (paramTypes[i] == String.class) {
    //                 args[i] = value;
    //             } else if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
    //                 args[i] = value != null ? Integer.parseInt(value) : 0;
    //             } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
    //                 args[i] = value != null ? Double.parseDouble(value) : 0.0;
    //             } else {
    //                 args[i] = value; // pour d’autres types, on peut ameliorer plus tard
    //             }
    //         } else {
    //             args[i] = null; // valeur par defaut si pas annote
    //         }
    //     }

    //     return args;
    // }
    

    // version unifiee
    // private Object[] bindParameters(
    //     HttpServletRequest req,
    //     Method method,
    //     Map<String, String> extractedVars
    // ) {
    //     Class<?>[] paramTypes = method.getParameterTypes();
    //     java.lang.reflect.Parameter[] params = method.getParameters();
    //     Object[] args = new Object[paramTypes.length];

    //     for (int i = 0; i < params.length; i++) {

    //         String name = params[i].getName();
    //         String value = null;

    //         // 1) priorite aux variables {id} dans l'URL
    //         if (extractedVars.containsKey(name)) {
    //             value = extractedVars.get(name);
    //         }
    //         // 2) sinon paramètres du formulaire GET/POST
    //         else if (req.getParameter(name) != null) {
    //             value = req.getParameter(name);
    //         }
    //         // 3) sinon @RequestParam
    //         else if (params[i].isAnnotationPresent(annotation.RequestParam.class)) {
    //             String paramName = params[i].getAnnotation(annotation.RequestParam.class).value();
    //             value = req.getParameter(paramName);
    //         }

    //         // conversion des types
    //         if (paramTypes[i] == String.class) {
    //             args[i] = value;
    //         }
    //         else if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
    //             try {
    //                 args[i] = value != null ? Integer.parseInt(value) : 0;
    //             } catch (NumberFormatException e) {
    //                 throw new RuntimeException("Impossible de convertir '" + value + "' en entier.");
    //             }
    //         }
    //         else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
    //             args[i] = value != null ? Double.parseDouble(value) : 0.0;
    //         }
    //         else if (Map.class.isAssignableFrom(paramTypes[i])) {
    //             args[i] = new HashMap<String, Object>();
    //         }

    //         else {
    //             args[i] = value;    
    //         }
    //     }

    //     return args;
    // }

    // SPRINT 8 : MAP
    private Object[] bindParameters(HttpServletRequest req, Method method) 
    {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        Map<String, String[]> requestParams = req.getParameterMap();

        for (int i = 0; i < params.length; i++) 
        {

            Parameter param = params[i];
            Class<?> paramType = param.getType();

            if (paramType == HttpServletRequest.class) {
                    args[i] = req;
                    continue;
                }

            // --------------------------------------------
            // 1) Gestion des Map<String, Object>
            // --------------------------------------------
            if (Map.class.isAssignableFrom(paramType)) 
            {
        
                // Vérifier les types génériques
                Type genericType = param.getParameterizedType();
                if (genericType instanceof ParameterizedType pt) 
                {
                    Type[] typeArgs = pt.getActualTypeArguments();
                    if (typeArgs.length != 2 ||
                            typeArgs[0] != String.class ||
                            typeArgs[1] != Object.class) {
                        throw new RuntimeException(
                                "Erreur : le paramètre Map doit être de type Map<String,Object> dans la méthode "
                                        + method.getName()
                        );
                    }
                } 
                else {
                    throw new RuntimeException(
                            "Erreur : le paramètre Map doit avoir des types génériques dans la méthode "
                                    + method.getName()
                    );
                }


                // Vérifier si la méthode est annotée @Session
                if (method.isAnnotationPresent(SessionAnnotation.class)) 
                {
                    Map<String, Object> sessionMap = new HashMap<>();
                    HttpSession httpSession = req.getSession(false);

                    if (httpSession != null) {
                        Enumeration<String> names = httpSession.getAttributeNames();
                        while (names.hasMoreElements()) {
                            String key = names.nextElement();
                            sessionMap.put(key, httpSession.getAttribute(key));
                        }
                    }

                    args[i] = sessionMap;
                    continue; // pour ne pas aller plus loin
                }
                    

                // Construire la Map
                Map<String, Object> map = new HashMap<>();
                // Champs texte depuis requestParams (peut être vide pour multipart)
                requestParams.forEach((key, values) -> {
                    if (values.length == 1) map.put(key, values[0]);
                    else map.put(key, Arrays.asList(values));
                });

                // Dossier de sauvegarde des uploads
                String uploadDir = "D:/CODE/Spring/test-framework/uploads";
                // String uploadDir = getServletContext().getRealPath("/uploads");
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // Si multipart, extraire aussi les champs depuis les Part
                if (req.getContentType() != null && req.getContentType().startsWith("multipart/form-data")) 
                {
                    try {
                        Collection<Part> parts = req.getParts();
                        for (Part part : parts) {

                            String partName = part.getName();
                            String submitted = part.getSubmittedFileName();

                            System.out.println("DEBUG part = " + partName +
                                    " submitted=" + submitted +
                                    " size=" + part.getSize() +
                                    " type=" + part.getContentType());

                            // ---------- CHAMP TEXTE ----------
                            if (submitted == null && part.getContentType() == null) {
                                String value = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                                map.put(partName, value);
                            }

                            // ---------- FICHIER ----------
                            else if (submitted != null && part.getSize() > 0) {

                                String fileName = Paths.get(submitted).getFileName().toString();

                                File file = new File(dir, fileName);
                                try (InputStream is = part.getInputStream();
                                    FileOutputStream fos = new FileOutputStream(file)) {

                                    is.transferTo(fos);
                                }

                                byte[] bytes = Files.readAllBytes(file.toPath());

                                map.put(partName, bytes);        //  CLE = name="file"
                                map.put("fileName", fileName);
                                map.put("fileType", part.getContentType());
                                map.put("filePath", file.getAbsolutePath());
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("DEBUG upload: exception getting parts: " + e);
                        e.printStackTrace();
                    }
                }

                // Debug : afficher le contenu du Map (taille pour les byte[])
                System.out.println("DEBUG upload: map entries:");
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    Object v = e.getValue();
                    if (v instanceof byte[]) {
                        System.out.println("DEBUG upload: map key=" + e.getKey() + " value=byte[" + ((byte[]) v).length + "]");
                    } else {
                        System.out.println("DEBUG upload: map key=" + e.getKey() + " value=" + v);
                    }
                }

                args[i] = map;
                continue;
            }

            // --------------------------------------------
            // 2) Gestion des objets complexes (POJO)
            // --------------------------------------------
            if (!paramType.isPrimitive()
                    && !paramType.getName().startsWith("java.lang")
                    && !paramType.isEnum()) 
            {
                try {
                    Object instance = paramType.getDeclaredConstructor().newInstance();

                    for (Field field : paramType.getDeclaredFields()) {
                        String fieldName = field.getName();

                        if (requestParams.containsKey(fieldName)) 
                        {
                            String value = requestParams.get(fieldName)[0];
                            field.setAccessible(true);
                            field.set(instance, convertValue(field.getType(), value));
                        }
                    }

                    args[i] = instance;
                    continue;

                } catch (Exception e) {
                    throw new RuntimeException(
                            "Impossible d'instancier l'objet " + paramType.getSimpleName(), e
                    );
                }
            }

            // --------------------------------------------
            // 3) Gestion des types simples (String, int, etc.)
            // --------------------------------------------
            if (requestParams.containsKey(param.getName())) {
                String value = requestParams.get(param.getName())[0];
                args[i] = convertValue(paramType, value);
            } else {
                args[i] = null;
            }
        }

        return args;
    }



    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException 
    {
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String relativePath = uri.substring(context.length());
        String methodHTTP = req.getMethod().toUpperCase();

        // --- CAS 1 : FICHIER STATIC (HTML / JSP) ---------------------------------
        if (handleStaticFile(relativePath, req, resp)) {
            return;
        }

        // --- CAS 2 : URL CONTROLLER SCANNER --------------------------------------
        String path = relativePath.isEmpty() ? "/" : relativePath;

        // Recuperer les URL definies pour la methode HTTP correspondante
        Map<String, UrlDefinition> defs =
                methodHTTP.equals("POST") ?
                        ControllerScanner.getPostmappings() :
                        ControllerScanner.getGetmappings();

        boolean matched = false;

        for (UrlDefinition def : defs.values()) 
        {
            String regex = "^" + def.getRegex() + "$";  // maka anle {}

            if (path.matches(regex)) 
            {
                matched = true;

                try {
                    Method method = def.getMethod();
                    Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                    String controllerName = method.getDeclaringClass().getSimpleName();
                    String methodName = method.getName();

                    // --- EXTRACTION {id} / {name} et on la stocke dans un Map --------------------------------
                    Map<String, String> extracted = new HashMap<>();

                    String[] pathParts = path.split("/");
                    String[] patternParts = def.getPattern().split("/");

                    for (int i = 0; i < patternParts.length; i++) 
                    {
                        if (patternParts[i].startsWith("{") && patternParts[i].endsWith("}")) 
                        {
                            String var = patternParts[i].substring(1, patternParts[i].length() - 1);
                            extracted.put(var, pathParts[i]);
                        }
                    }

                    // --- BINDING DES PARAMETRES ---------------------------------
                    Object[] args = bindParameters(req, method);

                    // --- INVOCATION ------------------------------------------------
                    Object result = method.invoke(controller, args); 
                    
                    // -------------- JSON -----------------
                    if (method.isAnnotationPresent(JsonAnnotation.class)) 
                    {
                        resp.setContentType("application/json;charset=UTF-8");

                        Gson gson = new Gson();

                        // Si la méthode retourne un ModelView, exposer directement sa map de données
                        Object data = result;
                        if (result instanceof ModelView mv) {
                            data = mv.getData();
                        }

                        JsonResponse api = new JsonResponse(
                                "success",
                                200,
                                "OK",
                                data
                        );

                        resp.getWriter().print(gson.toJson(api));
                        return;
                    }

                    // --- GESTION DES RESULTATS -----------------------------------
                    if (result instanceof String) 
                    {
                        resp.setContentType("text/plain;charset=UTF-8");
                        resp.getWriter().println(result);
                        return;
                    }
                    if (result instanceof ModelView mv) 
                    {
                        String view = mv.getView();

                        // REDIRECTION
                        if (view.startsWith("redirect:")) {
                            String target = view.substring("redirect:".length());
                            resp.sendRedirect(req.getContextPath() + target);
                            return;
                        }

                        for (Map.Entry<String, Object> entry : mv.getData().entrySet())  {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                        RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
                        dispatcher.forward(req, resp);
                        return;
                    }

                    // AFFICHAGE DEBUG ---------------------------------------------
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.getWriter().println("\n URL trouvee : " + path);
                    resp.getWriter().println("-> Methode HTTP : " + methodHTTP);
                    resp.getWriter().println("-> Controleur : " + controllerName);
                    resp.getWriter().println("-> Methode : " + methodName);
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(500, "Erreur URL dynamique : " + e.getMessage());
                    return;
                }
            }
        }

        // --- AUCUNE URL TROUVeE --------------------------------------------------
        if (!matched) {
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("404 - Aucune methode trouvee pour " + path);
        }
    }




    // @Override
    // protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException 
    // {
    //     String uri = req.getRequestURI();
    //     String context = req.getContextPath();
    //     String relativePath = uri.substring(context.length());

    //     // --- CAS 1 : FICHIER STATIC (HTML / JSP) ---------------------------------
    //     if (handleStaticFile(relativePath, req, resp)) {
    //         return;
    //     }


    //     // --- CAS 2 : URL MAPPeE PAR ANNOTATION (CONTROLLER SCANNER) --------------
    //     String path = relativePath.isEmpty() ? "/" : relativePath;


    //     // SPRINT 6 et 6 BIS --------------------------------------------
    //     // Map<String, Method> mappings = ControllerScanner.getUrlMappings();
    //     // Method method = mappings.get(path);

    //     // if (method != null) 
    //     // {
    //     //     try {
    //     //         Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
    //     //         // Object result = method.invoke(controller);

    //     //         String controllerName = method.getDeclaringClass().getSimpleName();
    //     //         String methodName = method.getName();

    //     //         //  Injection automatique des paramètres (Methode 1)
    //     //         Object[] args = bindParameters(req, method);

    //     //         //  Appel de la methode avec les arguments injectes
    //     //         Object result = method.invoke(controller, args);

    //     //         // --- Si le resultat est un texte simple ---------------------------
    //     //         if (result instanceof String) 
    //     //         {
    //     //             resp.setContentType("text/plain;charset=UTF-8");
    //     //             resp.getWriter().println("Resultat retourne : " + result);
    //     //         }

    //     //         // --- Si le resultat est un ModelView ------------------------------
    //     //         else if (result instanceof ModelView) 
    //     //         {
    //     //             ModelView mv = (ModelView) result;
    //     //             // String view = mv.getView();

    //     //             // // Injecte les donnees dans la requete
    //     //             // for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
    //     //             //     req.setAttribute(entry.getKey(), entry.getValue());
    //     //             // }

    //     //             // RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + view);
    //     //             // dispatcher.forward(req, resp);
    //     //             // return;

    //     //             // -Sprint 6 - Injecte les donnees dans la requête
    //     //             for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
    //     //                 req.setAttribute(entry.getKey(), entry.getValue());
    //     //             }

    //     //             RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
    //     //             dispatcher.forward(req, resp);
    //     //             return;
    //     //         }

    //     //         // --- Affichage --------------------------------------------------------
    //     //         resp.setContentType("text/plain;charset=UTF-8");
    //     //         resp.getWriter().println("\n URL trouvee : " + path);
    //     //         resp.getWriter().println("-> Controleur : " + controllerName);
    //     //         resp.getWriter().println("-> Methode : " + methodName);

    //     //         return;

    //     //     } catch (Exception e) {
    //     //         e.printStackTrace();
    //     //         resp.sendError(500, "Erreur interne du serveur : " + e.getMessage());
    //     //         return;
    //     //     }
    //     // }

    //     // // --- CAS 3 : Aucune URL trouvee 
    //     // resp.setContentType("text/plain;charset=UTF-8");
    //     // resp.getWriter().println("404 - Aucune methode trouvee pour " + path);

    //     // -------------------------------------------------------------------


    //     // SPRINT 6 TER ---------------------

    //     Map<String, UrlDefinition> defs = ControllerScanner.getUrlMappings();
    //     boolean matched = false;

    //     for (UrlDefinition def : defs.values()) 
    //     {

    //         String regex = "^" + def.getRegex() + "$";

    //         if (path.matches(regex)) 
    //         {
    //             matched = true;

    //             try {
    //                 Method method = def.getMethod();
    //                 Object controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();
    //                 String controllerName = method.getDeclaringClass().getSimpleName();
    //                 String methodName = method.getName();

    //                 // --- Extraire valeurs dynamiques {id} ---
    //                 Map<String, String> extracted = new HashMap<>();

    //                 String[] pathParts = path.split("/");
    //                 String[] patternParts = def.getPattern().split("/");

    //                 for (int i = 0; i < patternParts.length; i++) 
    //                 {
    //                     if (patternParts[i].startsWith("{")) {
    //                         String var = patternParts[i].substring(1, patternParts[i].length() - 1);
    //                         extracted.put(var, pathParts[i]);
    //                     }
    //                 }

    //                 // --- binder les paramètres ---

    //                 Parameter[] params = method.getParameters();
    //                 Object[] args = new Object[params.length];

    //                 for (int i = 0; i < params.length; i++) 
    //                 {
    //                     String name = params[i].getName();
    //                     String strValue = extracted.get(name);

    //                     if (params[i].getType() == int.class || params[i].getType() == Integer.class) {
    //                         args[i] = Integer.parseInt(strValue);
    //                     } 
    //                     else {
    //                         args[i] = strValue; // String par defaut
    //                     }
    //                 }

    //                 // --- Appeler la methode ---
    //                 Object result = method.invoke(controller, args);

    //                 // --- Gerer le retour ---
    //                 if (result instanceof String) {
    //                     resp.setContentType("text/plain;charset=UTF-8");
    //                     resp.getWriter().println(result);
    //                     return;
    //                 }

    //                 if (result instanceof ModelView) {
    //                     ModelView mv = (ModelView) result;

    //                     for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
    //                         req.setAttribute(entry.getKey(), entry.getValue());
    //                     }

    //                     RequestDispatcher dispatcher = req.getRequestDispatcher("/views/" + mv.getView());
    //                     dispatcher.forward(req, resp);
    //                     return;
    //                 }

    //                 // --- Affichage --------------------------------------------------------
    //                 resp.setContentType("text/plain;charset=UTF-8");
    //                 resp.getWriter().println("\n URL trouvee : " + path);
    //                 resp.getWriter().println("-> Controleur : " + controllerName);
    //                 resp.getWriter().println("-> Methode : " + methodName);
                    
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 resp.sendError(500, "Erreur URL dynamique : " + e.getMessage());
    //                 return;
    //             }

    //         }
    //     }

    //     // Si aucun motif ne correspond
    //     if (!matched) {
    //         resp.setContentType("text/plain;charset=UTF-8");
    //         resp.getWriter().println("404 - Aucune methode trouvee pour " + path);
    //     }

    // }




}

