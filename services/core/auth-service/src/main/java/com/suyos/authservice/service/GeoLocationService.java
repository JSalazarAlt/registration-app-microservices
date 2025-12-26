package com.suyos.authservice.service;

import java.net.InetAddress;

import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service for .
 *
 * <p>Handles </p>
 */
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private final DatabaseReader geoIpReader;

    private boolean isPrivateIp(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isSiteLocalAddress();
        } catch (Exception e) {
            return true;
        }
    }

    public String resolveLocation(String ip) {
        if (isPrivateIp(ip)) {
            return "Internal network";
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            CityResponse response = geoIpReader.city(address);

            String city = response.getCity().getName();
            String country = response.getCountry().getIsoCode();

            if (city == null && country == null) return "Unknown";
            if (city == null) return country;

            return city + ", " + country;
        } catch (Exception e) {
            return "Unknown";
        }
    }

}