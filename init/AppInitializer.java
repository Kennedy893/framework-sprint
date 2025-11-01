package init;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener 
{

    @Override
    public void contextInitialized(ServletContextEvent sce) 
    {
        System.out.println("Scan des controleurs au demarrage...");
        ControllerScanner.scanControllers("controller");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) 
    {
        System.out.println("Application stoppee.");
    }
}
