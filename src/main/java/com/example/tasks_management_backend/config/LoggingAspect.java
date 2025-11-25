package com.example.tasks_management_backend.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Pointcut to match all methods in TaskService
    @Pointcut("execution(* com.example.tasks_management_backend.service.TaskService.*(..))")
    public void taskServiceMethods() {}

    @Before("taskServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Entering method: {} with args {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(value = "taskServiceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Method {} returned: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(value = "taskServiceMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        logger.error("Method {} thrown exception: {}", joinPoint.getSignature(), ex.getMessage());
    }
}