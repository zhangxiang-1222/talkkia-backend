// AppleCallbackController.java
package com.talkkia.api.controller;

//import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/api/auth")
@Controller
public class AppleCallbackController {


    @PostMapping("/apple/callback")
    public void appleCallback(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        final String androidPackageName = "com.talkkia.android";
        try {
            // 可选：校验 state（建议接入 Redis/session 做一次性校验）
            String query = buildQuery(params);

            String location = "intent://callback?" + query
                    + "#Intent;package=" + androidPackageName
                    + ";scheme=signinwithapple;end";

            response.setStatus(HttpServletResponse.SC_FOUND); // 302
            response.setHeader("Location", location);
        } catch (Exception e) {
            log.error("Apple callback redirect failed", e);
            String err = URLEncoder.encode("server_error", StandardCharsets.UTF_8);
            String location = "intent://callback?error=" + err
                    + "#Intent;package=" + androidPackageName
                    + ";scheme=signinwithapple;end";
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", location);
        }
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
