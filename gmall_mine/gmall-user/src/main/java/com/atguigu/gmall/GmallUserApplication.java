package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan(basePackages = "com.atguigu.gmall.user.mapper.UserInfoMapper")
@tk.mybatis.spring.annotation.MapperScan(basePackages = "com.atguigu.gmall.user.mapper")
public class GmallUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUserApplication.class, args);
	}
}
