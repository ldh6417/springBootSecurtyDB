package com.zeus.common.securty;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		//접근이 금지된 페이지를 요청하는 회원의 정보를 추적한다.
		log.info("AccessDeniedHandler");
		log.info("AccessDeniedHandler request" + request);
		log.info("AccessDeniedHandler response" + response);
		log.info("AccessDeniedHandler response" + accessDeniedException);
		
		response.sendRedirect("/accessError");
	}

}
