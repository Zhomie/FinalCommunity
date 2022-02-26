package com.homie.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init(){
		//解决Netty启动冲突的问题
		//Netty4Utils :NettyRuntime.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}
	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
