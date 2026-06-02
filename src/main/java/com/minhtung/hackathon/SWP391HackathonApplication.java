package com.minhtung.hackathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
@EnableScheduling // dòng nay để xóa khi ko ấn xác nhận
public class SWP391HackathonApplication {


    public static void main(String[] args) {
        SpringApplication.run(SWP391HackathonApplication.class, args);
    }
}