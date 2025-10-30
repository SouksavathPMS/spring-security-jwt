package com.kyedev.spring_security_jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Log all controller methods
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}


    // Log all service methods
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}

    @Before("controllerMethods()")
    public void beforeControllerMethod(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            log.info("=========================== REQUEST START ===========================");
            log.info("HTTP Method: {}", request.getMethod());
            log.info("URI: {}", request.getRequestURI());
            log.info("Controller Method: {}.{}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            log.info("IP Address: {}", request.getRemoteAddr());

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Arrays.stream(args).forEach(arg -> {
                    if (arg != null) {
                        try {
                            // Don't log sensitive data like password
                            String argString = objectMapper.writeValueAsString(arg);
                            if (!argString.contains("password")) {
                               log.info("Request Body: [CONTAINS SENSITIVE DATA - HIDDEN]");
                            } else {
                                log.info("Request Body: {}",argString);
                            }

                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            log.info("Request Argument: {}", arg.getClass().getName());
                        }
                    }
                });
            }
        }
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        try {
            String resultString = objectMapper.writeValueAsString(result);
            // Don't log tokens in response
            if (resultString.contains("accessToken") || resultString.contains("refreshToken")) {
                log.info("Response: [CONTAINS TOKENS - HIDDEN]");
            } else {
                log.info("Response: {}.", resultString);
            }
        } catch (Exception e) {
            log.info("Response: {}", result != null ? result.getClass().getName() : e.getMessage());
        }
        log.info("=========================== REQUEST END =============================");
    }

    @Around("serviceMethods()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();

        log.debug(">>> Entering Service: {}.{}", className, methodName);

        long startTime = System.currentTimeMillis();
        Object result = null;

        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("<<< Exiting Service: {}.{} - Execution time: {}ms",
                    className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("<<< Exception in Service: {}.{} - Execution time: {}ms - Error: {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}.{} with message: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}
