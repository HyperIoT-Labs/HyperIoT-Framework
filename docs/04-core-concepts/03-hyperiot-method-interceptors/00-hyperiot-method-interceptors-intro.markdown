# Service Method Interceptors [](id=hyperiot-service-method-interceptors)

HyperIoT Framework provides the ability to insert interceptors via annotation to insert custom pre/post execution logic within the methods of classes representing a HyperIoTService.

## Why do we need method interceptors ?

Just think of all those common logics , also triggered by configuration, to trigger actions on the pre/post execution of a method. The following are some examples.

* Application logging on entry/exit from a method 
* Measurement of execution times 
* Automatic security management on method invocation.

Those mentioned are just the main ones, but it might happen that a developer still needs to develop his own custom interceptor logic.

## How do they works ?

HyperIoT registers, within the OSGi context, listeners by intercepting all registered components that extend the HyperIoTService interface. A proxy component is always registered for this type of service. The purpose of this proxy is precisely to enable pre/post invocation logic via annotations.

## How to implement custom annotation interceptor ?

The procedure is very simple: First you define the annotation and annotate it itself with @HyperIoTInterceptorExecutor giving as the value of the "interceptor" attribute the name of the class that will contain the implementation of the pre/post execution logic.

```
/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users with certain roles
 */
@Target({ ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowRolesInterceptor.class)
public @interface AllowRoles  {
    /**
     *
     * @return Role names array
     */
    String[] rolesNames() default {};
}
```
Having defined the annotation we go on to develop the implementation, here there are two possible alternatives:

1. Define an interceptor executor that implements the HyperIoTBeforeMethodInterceptor interface.
2. Define an interceptor executor that implements the HyperIoTAfterMethodInterceptor interface.

Depending on which one you choose your implementation will execute either before or after the method.

Below is the implementation referring to the example above:

```
/**
 * @Author Aristide Cittadino
 * This class is the implementation of @AllowRoles annotation.
 * It simply verifies that the user has the a specific role before execute the invoked method.
 */
@Component(service = {HyperIoTBeforeMethodInterceptor.class, AllowRolesInterceptor.class}, immediate = true)
public class AllowRolesInterceptor extends AbstractPermissionInterceptor implements HyperIoTBeforeMethodInterceptor<AllowRoles> {
    private static Logger log = LoggerFactory.getLogger(AllowRolesInterceptor.class.getName());

    @Override
    public void interceptMethod(HyperIoTService s, Method m, Object[] args, AllowRoles annotation) {
        ....
    }
}
```

In this case, "AllowRolesInterceptor" implements "HyperIoTBeforeMethodInterceptor."

With this choice the interceptor will be executed before every method of every HyperIoTService. With this in mind, it becomes critical to check the custom executors for the presence of precise annotations to verify that they are actually executing the desired logic.