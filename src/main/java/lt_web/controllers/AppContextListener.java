package lt_web.controllers;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lt_web.repository.JpaConfig;
import lt_web.utils.ValidationUtil;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaConfig.close();
        ValidationUtil.close();
    }
}
