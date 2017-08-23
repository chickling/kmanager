package com.chickling.kmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Hulva Luva.H
 *
 */
@SpringBootApplication
@ComponentScan("com.chickling.kmanager.**")
public class Kmanager {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Kmanager.class, args);
	}
}
