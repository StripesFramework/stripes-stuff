package javax.annotation.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;


/**
 * EJB annotation used by {@link org.stripesstuff.plugin.security.J2EESecurityManager}.
 *
 * @author <a href="mailto:kindop@xs4all.nl">Oscar Westra van Holthe - Kind</a>
 * @version $Id:$
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0 specification</a>
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface RolesAllowed
{
	String[] value();
}
