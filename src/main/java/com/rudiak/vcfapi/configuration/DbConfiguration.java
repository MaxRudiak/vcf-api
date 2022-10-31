package com.rudiak.vcfapi.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:dao/*.xml"})
public class DbConfiguration {
}
