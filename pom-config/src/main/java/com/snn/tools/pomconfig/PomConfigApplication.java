package com.snn.tools.pomconfig;

import com.snn.tools.pomconfig.panel.MainPanel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author anan
 */
@SpringBootApplication
public class PomConfigApplication {

    public static void main(String[] args) {
        MainPanel mainPanel=new MainPanel();
        mainPanel.showModel();
        SpringApplication.run(PomConfigApplication.class, args);
    }

}
