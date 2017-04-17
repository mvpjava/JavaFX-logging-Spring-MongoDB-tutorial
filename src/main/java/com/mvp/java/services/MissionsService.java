package com.mvp.java.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MissionsService {

    @Value(("${specs.dir}"))
    private String specsPath;

    public String getMissionInfo(String missionName) throws IOException {
        final StringBuilder fileContents = new StringBuilder(2000);
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(specsPath + missionName);
        } catch (Exception e) {
            throw new FileNotFoundException("unknown mission specified");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContents.append(line).append("\n");
            }
        }
        return fileContents.toString();
    }
}
