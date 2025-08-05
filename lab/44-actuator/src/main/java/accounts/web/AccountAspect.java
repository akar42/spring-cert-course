package accounts.web;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/*
 * TODO-26 (Optional): Use AOP for counting logic
 * - Add `spring-boot-starter-aop` starter to the `pom.xml` or the
 *   `build.gradle`. (You might want to refresh your IDE so that
 *   it picks up the change in the `pom.xml` or the `build.gradle` file.)
 * - Make this class an Aspect, through which `account.fetch` counter,
 *   which has a tag of `type`/`fromAspect` key/value pair, gets incremented
 *   every time `accountSummary` method of the `AccountController` class
 *   is invoked
 * - Make this a component by using a proper annotation
 * - Access `/accounts` several times and verify the metrics of
 *   `/actuator/metrics/account.fetch?tag=type:fromAspect
 */
@Aspect
@Component
public class AccountAspect {

    private Counter counter;

    @Autowired
    public AccountAspect(MeterRegistry registry) {
        this.counter = registry.counter("account.fetch", "type", "fromAspect");
    }

    @Before("execution(* accounts.web.AccountController.accountSummary(..))")
    public void aspectCounter(JoinPoint joinPoint) {
        counter.increment();
    }
}
