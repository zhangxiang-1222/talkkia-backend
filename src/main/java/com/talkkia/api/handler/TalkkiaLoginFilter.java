package com.talkkia.api.handler;

import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: Ethan
 * @Date: 2024/9/17
 * @Description: verify the Authorization,user has to login in
 */
@Slf4j
public class TalkkiaLoginFilter extends OncePerRequestFilter {

    public TalkkiaLoginFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private JwtTokenUtil jwtTokenUtil;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final List<String> EXCLUDE_URL = Arrays.asList(
            "/api/user/register",
            "/api/user/signup",
            "/api/user/otp",
            "/api/user/social-login",
            "/api/auth/noPermission",
            "/websocket",
            "/api/auth/apple/callback");

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            // verify JWT
            String token = httpServletRequest.getHeader("token");
            String userStr = httpServletRequest.getHeader("userid");
            if (!StringUtils.isBlank(token) && !StringUtils.isBlank(userStr)) {
                if (token.equals(jwtTokenUtil.generateToken(Long.valueOf(userStr)))) {
                    filterChain.doFilter(httpServletRequest, httpServletResponse);
                    return;
                }
            }
            log.info("filtered url:{}", httpServletRequest.getServletPath());
            log.info("filtered time:{}", sdf.format(new Date()));
            if (!httpServletResponse.isCommitted()) {
                httpServletResponse.sendRedirect("/api/auth/noPermission");
            }
        } catch (Exception e) {
            log.error("error", e);
            if (!httpServletResponse.isCommitted()) {
                httpServletResponse.sendRedirect("/api/auth/noPermission");
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(request.getServletPath()));
    }
}
