package com.suyos.authservice.service;

import java.net.InetAddress;

import org.springframework.stereotype.Service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service for resolving geolocation information from IP addresses.
 *
 * <p>Handles IP validation and maps public IPs to city/country using a GeoIP
 * database.</p>
 */
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    /** GeoIP database reader */
    private final DatabaseReader geoIpReader;

    /**
     * Checks whether the given IP address is private or local.
     *
     * @param ip IP address to check
     * @return True if IP is private or local, false otherwise
     */
    private boolean isPrivateIp(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isSiteLocalAddress();
        } catch (Exception e) {
            // Treat invalid IPs as private
            return true;
        }
    }

    /**
     * Resolves the location of an IP address.
     *
     * @param ip IP address to resolve
     * @return Location in "city, country" format or fallback values
     */
    public String resolveLocation(String ip) {
        // Return early for private/local IPs
        if (isPrivateIp(ip)) {
            return "Internal network";
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            CityResponse response = geoIpReader.city(address);

            String city = response.getCity().getName();
            String country = response.getCountry().getIsoCode();

            // Handle missing location data
            if (city == null && country == null) return "Unknown";
            if (city == null) return country;

            return city + ", " + country;
        } catch (Exception e) {
            // Fallback if lookup fails
            return "Unknown";
        }
    }
    
}