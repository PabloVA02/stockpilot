package com.pabloverdejo.stockpilot.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

final class ApiProblems {

    private ApiProblems() {
    }

    static ProblemDetail create(
            HttpStatus status,
            String title,
            String detail,
            String type,
            HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://stockpilot.dev/problems/" + type));
        problem.setInstance(URI.create(request.getRequestURI()));
        var requestId = request.getAttribute(RequestTraceFilter.ATTRIBUTE);
        if (requestId != null) {
            problem.setProperty("requestId", requestId);
        }
        return problem;
    }
}
