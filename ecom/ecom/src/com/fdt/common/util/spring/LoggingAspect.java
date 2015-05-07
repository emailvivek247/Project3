package com.fdt.common.util.spring;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

@Aspect
@Component
public class LoggingAspect {

	private final Logger logger = LoggerFactory.getLogger("INPUT_LOG");

	@Before(value="within(com.fdt.ecom.service.rs.EComAdminFacadeServiceRSImpl)", argNames = "joinPoint")
	public void logEcomAdminBefore(JoinPoint joinPoint) {
		Object[] signatureArgs = replaceNullInArray(joinPoint.getArgs());
		logger.debug("Request: " + joinPoint.getTarget().getClass().getSimpleName()
			+ ":" + joinPoint.getSignature().getName()
				+ "(" + Joiner.on(",").join(Arrays.asList(signatureArgs)) + ")");
	}

	@Before(value="within(com.fdt.ecom.service.rs.EComFacadeServiceRSImpl)", argNames = "joinPoint")
	public void logSDLEcomBefore(JoinPoint joinPoint) {
		Object[] signatureArgs = replaceNullInArray(joinPoint.getArgs());
		logger.debug("Request: " + joinPoint.getTarget().getClass().getSimpleName()
			+ ":" + joinPoint.getSignature().getName()
				+ "(" + Joiner.on(",").join(Arrays.asList(signatureArgs)) + ")");
	}

	private Object[] replaceNullInArray(Object[] signatureArgs1) {
		if(signatureArgs1 != null && signatureArgs1.length > 0) {
			for(int i=0; i < signatureArgs1.length; i++) {
				if(signatureArgs1[i] == null) {
					signatureArgs1[i] = "null";
				}
			}
		}
		return signatureArgs1;
	}

	@AfterReturning(value="within(com.fdt.ecom.service.rs.EComAdminFacadeServiceRSImpl)", returning = "result")
	public void logEcomAdminAfterReturning(JoinPoint joinPoint, Object result){
		logger.debug("Response: " + joinPoint.getTarget().getClass().getSimpleName() + ":"
			+ joinPoint.getSignature().getName() + "(" + result + ")");
	}

	@AfterReturning(value="within(com.fdt.ecom.service.rs.EComFacadeServiceRSImpl)", returning = "result")
	public void logSDLEcomAfterReturning(JoinPoint joinPoint, Object result){
		logger.debug("Response: " + joinPoint.getTarget().getClass().getSimpleName() + ":"
			+ joinPoint.getSignature().getName() + "(" + result + ")");
	}

	/** This Code is Currentlt Not Being Used. It will be used in the Future **/
	/**public void logAfter(JoinPoint joinPoint) {
		System.out.println("logAfter() is running!");
		System.out.println("hijacked : " + joinPoint.getSignature().getName());
		System.out.println("******");
	}

	public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
		System.out.println("hijacked : " + joinPoint.getSignature().getName());
		System.out.println("Exception : " + error);
		System.out.println("******");
	}

	public void logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("logAround() is running!");
		System.out.println("hijacked method : "	+ joinPoint.getSignature().getName());
		System.out.println("hijacked arguments : " + Arrays.toString(joinPoint.getArgs()));
		System.out.println("Around before is running!");
		joinPoint.proceed();
		System.out.println("Around after is running!");
		System.out.println("******");
	} **/

}