package com.topology.phytopo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 拓扑服务主启动类
 *
 * @author topology
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.topology.phytopo.mapper")
public class PhyTopoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhyTopoApplication.class, args);
    }
}
