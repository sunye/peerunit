package fr.inria.peerunit.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Meta-annotation Retention indicate that this annotation must be retained in runtime
 * Meta-annotation Target indicates that this annotation type can be used to annotate only method declarations.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterClass {
	int place() default -1;
	int timeout() default 1000;
	int from() default -1;
	int to() default -1;
}
