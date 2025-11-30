package init;

import java.lang.reflect.Method;
import java.util.List;

public class UrlDefinition 
{

    private String pattern;
    private String regex;
    private List<String> variables;
    private Method method;
    private String httpMethod; // "GET" ou "POST"

    public UrlDefinition(String pattern, String regex, List<String> vars, Method method, String httpMethod) {
        this.pattern = pattern;
        this.regex = regex;
        this.variables = vars;
        this.method = method;
        this.httpMethod = httpMethod;
    }

    public String getPattern() { return pattern; }
    public String getRegex() { return regex; }
    public List<String> getVariables() { return variables; }
    public Method getMethod() { return method; }
    public String getHttpMethod() { return httpMethod; }
}


// "/user/{id}",          // pattern
// "/user/([^/]+)",       // regex pour matcher le {id}
//  vars,                 // liste des variables
//  getUserMethod,        // méthode Java à appeler
//  "GET"                 // type HTTP