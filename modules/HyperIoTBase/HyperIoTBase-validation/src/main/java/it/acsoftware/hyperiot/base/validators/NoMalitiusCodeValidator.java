package it.acsoftware.hyperiot.base.validators;

import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;

public class NoMalitiusCodeValidator implements ConstraintValidator<NoMalitiusCode, String> {
    private Logger log = LoggerFactory.getLogger(NoMalitiusCodeValidator.class.getName());

    public static final String SQL_TYPES =
            "TABLE, TABLESPACE, PROCEDURE, FUNCTION, TRIGGER, KEY, VIEW, MATERIALIZED VIEW, LIBRARY" +
                    "DATABASE LINK, DBLINK, INDEX, CONSTRAINT, TRIGGER, USER, SCHEMA, DATABASE, PLUGGABLE DATABASE, BUCKET, " +
                    "CLUSTER, COMMENT, SYNONYM, TYPE, JAVA, SESSION, ROLE, PACKAGE, PACKAGE BODY, OPERATOR" +
                    "SEQUENCE, RESTORE POINT, PFILE, CLASS, CURSOR, OBJECT, RULE, USER, DATASET, DATASTORE, " +
                    "COLUMN, FIELD, OPERATOR";


    private static Pattern[] scriptPatterns = new Pattern[]{
            // Script fragments
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            // src='...'
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script tags
            Pattern.compile(".*</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*<script(.*?)>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile(".*eval\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile(".*expression\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:...
            Pattern.compile(".*javascript:", Pattern.CASE_INSENSITIVE),
            // vbscript:...
            Pattern.compile(".*vbscript:", Pattern.CASE_INSENSITIVE),
            // onload(...)=...
            Pattern.compile(".*onload(.*?)=",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

    };

    private static Pattern[] sqlPatterns = new Pattern[]{
            Pattern.compile("(?i)(.*)(\\b)+SELECT(\\b)+\\s.*(\\b)+FROM(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+INSERT(\\b)+\\s.*(\\b)+INTO(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+UPDATE(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+DELETE(\\b)+\\s.*(\\b)+FROM(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+UPSERT(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+SAVEPOINT(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+CALL(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+ROLLBACK(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+KILL(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+DROP(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+CREATE(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+ALTER(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+TRUNCATE(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+LOCK(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+UNLOCK(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+RELEASE(\\b)+(\\s)*(" + SQL_TYPES.replaceAll(",", "|") + ")(\\b)+\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+DESC(\\b)+(\\w)*\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(?i)(.*)(\\b)+DESCRIBE(\\b)+(\\w)*\\s.*(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(.*)(/\\*|\\*/|;){1,}(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
            Pattern.compile("(.*)(-){2,}(.*)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.debug( "Validating value with @NoMalitiusCode...");
        if (value == null)
            return true;
        List<Pattern> patterns = new ArrayList<>();
        patterns.addAll(Arrays.asList(scriptPatterns));
        patterns.addAll(Arrays.asList(sqlPatterns));
        for (Pattern scriptPattern : patterns) {
            if (scriptPattern.matcher(value).matches()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "{it.acsoftware.hyperiot.validator.nomalitiuscode.message}")
                        .addConstraintViolation();
                log.debug( "@NoMalitiusCode validation failed...");
                return false;
            }
        }
        return true;
    }

}
