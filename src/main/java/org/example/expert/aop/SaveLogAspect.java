package org.example.expert.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.annotation.SaveLog;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.service.LogService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SaveLogAspect {
    private final LogService logService;

    @Around("@annotation(saveLog)")
    public Object saveLog(ProceedingJoinPoint joinPoint, SaveLog saveLog) throws Throwable {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String message = saveLog.value();

        try {
            Object result = joinPoint.proceed();

            logService.saveLog(authUser.getId(), message, false);
            return result;
        } catch (Throwable e) {
            logService.saveLog(authUser.getId(), message, true);

            throw e;
        }

    }
}
