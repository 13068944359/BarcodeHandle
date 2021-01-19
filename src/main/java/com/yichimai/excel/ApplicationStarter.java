package com.yichimai.excel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
该注解指定项目为springboot，由此类当作程序入口
自动装配 web 依赖的环境

**/
@SpringBootApplication
//@EnableJpaRepositories("cn.edu.eeagd.repository") // JPA扫描该包路径下的Repositorie
//@EntityScan("cn.edu.eeagd.entity") // 扫描Entity实体类
@ServletComponentScan
public class ApplicationStarter extends SpringBootServletInitializer{
	
	public static void main(String[] args) {
        SpringApplication.run(ApplicationStarter.class, args);
    }
    
    /**
     * tomcat需要一个入口来启动springboot，所以需要extends SpringBootServletInitializer
     * 和下面这个方法
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    	return builder.sources(ApplicationStarter.class);
    }
}
