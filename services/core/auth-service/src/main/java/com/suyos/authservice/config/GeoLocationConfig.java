package com.suyos.authservice.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.maxmind.geoip2.DatabaseReader;

@Configuration
public class GeoLocationConfig {

    @Bean
    public DatabaseReader geoIpReader() throws IOException {
        InputStream db = new ClassPathResource("GeoLite2-City/GeoLite2-City.mmdb").getInputStream();
        return new DatabaseReader.Builder(db).build();
    }
    
}