# Entity Validation [](id=entity-validation)

One of the most tedious and most "time-consuming" moments of a developer is when you have to develop CRUD services and specifically you have to take information and perform validations on the entities involved.

With modern front-end technologies, we know that it is very easy to give graphical feedback to the user, but we also know that front-end validation alone is not enough, and it is therefore necessary to "replicate" the server-side validation logic.

Once again, HyperIoT Framework takes advantage of existing technologies by trying to set up a modus operandi to drastically reduce the code to be written, focusing solely and exclusively on defining its models.

In the Java world there is already the standard for validation provided by the javax.validation package where some simple annotations such as @NotNull, @Length are defined. These are also exploited and identified by other frameworks such as Swagger for generating documentation and identifying which fields are mandatory or not (in this case it goes to read @NotNull fields). The HyperIoT Framework also takes advantage of such annotations and defines new ones and automatically inserts them into code management and database storage.

Annotating a field as @NotNull will have the effect of having an actual check before the entity is saved to the database. This behavior may seem trivial but it is not. We all know about these annotations but few people actually use them, why? Simple, these annotations alone are not enough and many times they have no effect in the code unless you plan to include a validator on persistence. Many developers think that just inserting such annotations is enough to have the control implemented but that is not the case!

HyperIoT Framework already has a built-in hibernate validator that precisely checks and validates the annotated fields before each save.

## *SystemApi and Validation

As described above, the invocation path that the framework follows, for CRUD operations, is as follows :

RestApi -> Api → SystemApi → Repository → Perstitence.

The developer, should take care to always maintain this path when developing new functionality. This will ensure him not only proper separation of the various application layers but also proper management of the entity lifecycle in terms of permission checking, validation and saving.

Specifically, validation comes into play in the *SystemApi layer. Here, before the save or update method is invoked on the repository the validator is launched on the entity in question and if successful the update will be performed.

## Custom HyperIoT Tags

In addition to the tags available from javax.validation in the generated projects (in *-model) there will be additional annotations available which we will describe shortly.

The module that exports the HyperIoT validation annotations is called HyperIoTBase-validation just import it into the projects where it is needed and also add that information on the bnd file.

In the build.gradle:

compileOnly group: "en.acsoftware.hyperiot.base", name: "HyperIoTBase-validation", version:1.0.0

In the bnd : 

```
Import-Package: \
    ...
    it.acsoftware.hyperiot.base.validation;version='[1.0.0,2)'
    ...
```

The annotations currently implemented are as follows:

* @NoMalitiusCode : This annotation is critical for security issues. It in fact strips from the string field to which it refers. We recommend annotating any string field with this annotation so that both front-end (Javascript injection) and back-end (SQL-Injection) code injection attacks can be avoided.
* @NotNullOnPersist: This annotation differs from @NotNull in the simple fact that it relates only to persistence, whereas @NotNull is generic. For example, the swagger documentation uses @NotNull to identify mandatory fields.
* @PasswordMustMatch: applicable to all objects of type HyperIoTAuthenticable and checks for password matches when adding a new entity
* @PowOf2: Verifies that the associated integer is a power of 2
* @PatternValidator: Validates the specified pattern
* @ValidPassword: Implements verification logic for passwords that are not easily attacked and returns an error in case of a weak password

## Managing Validation Errors

n case of failed validation the exception raised is a HyperIoTValidationException. This class contains within it the information about which field failed the check with the specific error message, if any.

Finally, the exception is also automatically converted via Jackson for the REST response.

Below is the excerpt of the class:

```
/**
 * @author Aristide Cittadino Model class for HyperIoTValidationException. It is
 * used to describe any constraint violation that occurs during runtime
 * exceptions.
 */
public class HyperIoTValidationException extends HyperIoTRuntimeException {
    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * Collection that contains constraint violations
     */
    private Set<ConstraintViolation<HyperIoTBaseEntity>> violations;

    /**
     * Constructor for HyperIoTValidationException
     *
     * @param violations parameter that indicates constraint violations produced
     */
    public HyperIoTValidationException(Set<ConstraintViolation<HyperIoTBaseEntity>> violations) {
        this.violations = violations;
    }

    /**
     * Gets the constraint violations
     *
     * @return Collection of constraint violations
     */
    public Set<ConstraintViolation<HyperIoTBaseEntity>> getViolations() {
        return violations;
    }

}
```