// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.plugins.validation.validators;

import org.apache.logging.log4j.status.StatusLogger;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Collection;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidator;

public class RequiredValidator implements ConstraintValidator<Required>
{
    private static final Logger LOGGER;
    private Required annotation;
    
    @Override
    public void initialize(final Required anAnnotation) {
        this.annotation = anAnnotation;
    }
    
    @Override
    public boolean isValid(final String name, final Object value) {
        if (value == null) {
            return this.err(name);
        }
        if (value instanceof CharSequence) {
            final CharSequence sequence = (CharSequence)value;
            return sequence.length() != 0 || this.err(name);
        }
        final Class<?> clazz = value.getClass();
        if (clazz.isArray()) {
            final Object[] array = (Object[])value;
            return array.length != 0 || this.err(name);
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            final Collection<?> collection = (Collection<?>)value;
            return collection.size() != 0 || this.err(name);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            final Map<?, ?> map = (Map<?, ?>)value;
            return map.size() != 0 || this.err(name);
        }
        return true;
    }
    
    private boolean err(final String name) {
        RequiredValidator.LOGGER.error(this.annotation.message() + ": " + name);
        return false;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
