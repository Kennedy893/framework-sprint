package init;

import java.lang.reflect.Method;
import java.util.List;

public class UrlDefinition {
    private String pattern;     // ex: /etudiant/delete/{id}
    private String regex;       // ex: /etudiant/delete/([^/]+)
    private List<String> vars;  // ex: ["id"]
    private Method method;

    public UrlDefinition(String pattern, String regex, List<String> vars, Method method) {
        this.pattern = pattern;
        this.regex = regex;
        this.vars = vars;
        this.method = method;
    }

    public String getPattern() { return pattern; }
    public String getRegex() { return regex; }
    public List<String> getVars() { return vars; }
    public Method getMethod() { return method; }
}
