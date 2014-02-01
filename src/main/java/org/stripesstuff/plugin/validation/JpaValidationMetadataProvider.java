/* Copyright 2008 Patrick Lightbody
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.stripesstuff.plugin.validation;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.validation.DefaultValidationMetadataProvider;
import net.sourceforge.stripes.validation.TypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationMetadata;

public class JpaValidationMetadataProvider extends DefaultValidationMetadataProvider {

    private static final Log log = Log.getInstance(JpaValidationMetadataProvider.class);

    @Override
    protected Map<String, ValidationMetadata> loadForClass(Class<?> beanType) {
        Map<String, ValidationMetadata> meta = new HashMap<String, ValidationMetadata>(super.loadForClass(beanType));

        try {
            for (Class<?> clazz = beanType; clazz != null; clazz = clazz.getSuperclass()) {
                PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    String propertyName = pd.getName();
                    Field field = null;
                    try {
                        field = clazz.getDeclaredField(propertyName);
                    } catch (NoSuchFieldException e) {
                        continue;
                    }

                    Class<?> fieldClass = field.getType();

                    // Is it annotated with JPA's @Entity?
                    Annotation entity = fieldClass.getAnnotation(javax.persistence.Entity.class);
                    if (entity != null) {
                        // Look at all the fields of this entity for validation rules
                        for (Class<?> entityClass = fieldClass; entityClass != null; entityClass = entityClass
                                .getSuperclass()) {
                            PropertyDescriptor[] entityPds = Introspector.getBeanInfo(entityClass)
                                    .getPropertyDescriptors();
                            for (PropertyDescriptor entityPd : entityPds) {
                                String entityPropertyName = entityPd.getName();
                                Method entityAccessor = entityPd.getReadMethod();
                                Method entityMutator = entityPd.getWriteMethod();
                                try {
                                    field = entityClass.getDeclaredField(entityPropertyName);
                                } catch (NoSuchFieldException e) {
                                    continue;
                                }

                                String vmdPropName = propertyName + "." + entityPropertyName;
                                ValidationMetadata original = meta.get(vmdPropName);
                                StubValidate stub = new StubValidate(original);

                                Size size = findAnnotation(field, entityAccessor, entityMutator, Size.class);
                                if (size != null) {
                                    stub.setMinlength(size.min());
                                    stub.setMaxlength(size.max());
                                }

                                NotNull notNull = findAnnotation(field, entityAccessor, entityMutator, NotNull.class);
                                if (notNull != null) {
                                    stub.setRequired(true);
                                }

                                if (stub.stubTouched) {
                                    meta.put(vmdPropName, new ValidationMetadata(vmdPropName, stub));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.error(e, "Failure checking JPA annotations ", getClass().getName());
            StripesRuntimeException sre = new StripesRuntimeException(e.getMessage(), e);
            sre.setStackTrace(e.getStackTrace());
            throw sre;
        } catch (RuntimeException e) {
            log.error(e, "Failure checking JPA annotations ", getClass().getName());
            throw e;
        }

        return Collections.unmodifiableMap(meta);
    }

    private <T extends Annotation> T findAnnotation(Field field, Method getter, Method setter, Class<T> ann) {
        T result;

        if (getter != null) {
            result = getter.getAnnotation(ann);

            if (result != null) {
                return result;
            }
        }

        if (setter != null) {
            result = setter.getAnnotation(ann);

            if (result != null) {
                return result;
            }
        }

        return field.getAnnotation(ann);
    }

    private class StubValidate implements Validate {
        String field = "";
        boolean encrypted;
        boolean required;
        boolean requiredSet;
        boolean trim = true;
        String[] on = new String[0];
        boolean ignore;
        int minlength = -1;
        boolean minlengthSet;
        int maxlength = -1;
        boolean maxlengthSet;
        double minvalue = Double.MIN_VALUE;
        double maxvalue = Double.MAX_VALUE;
        String mask = "";
        String expression = "";
        @SuppressWarnings("rawtypes")
        Class<? extends TypeConverter> converter = TypeConverter.class;
        String label = "";

        boolean stubTouched = false;

        public StubValidate(ValidationMetadata metadata) {
            if (metadata != null) {
                // field = ???
                encrypted = metadata.encrypted();
                if (metadata.required()) {
                    required = metadata.required();
                    requiredSet = true;
                }
                trim = metadata.trim();
                // on = metadata.on???
                ignore = metadata.ignore();
                if (metadata.minlength() != null && metadata.minlength() != -1) {
                    minlength = metadata.minlength();
                    minlengthSet = true;
                }
                if (metadata.maxlength() != null && metadata.maxlength() != -1) {
                    maxlength = metadata.maxlength();
                    maxlengthSet = true;
                }
                if (metadata.mask() != null) {
                    mask = metadata.mask().pattern();
                }
                if (metadata.expression() != null) {
                    expression = metadata.expression();
                }
                if (metadata.converter() != null) {
                    converter = metadata.converter();
                }
                if (metadata.label() != null) {
                    label = metadata.label();
                }
            }
        }

        public void setField(String field) {
            this.field = field;
            this.stubTouched = true;
        }

        public void setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
            this.stubTouched = true;
        }

        public void setRequired(boolean required) {
            if (!requiredSet) {
                this.required = required;
                this.stubTouched = true;
            }
        }

        public void setTrim(boolean trim) {
            this.trim = trim;
            this.stubTouched = true;
        }

        public void setOn(String[] on) {
            this.on = on;
            this.stubTouched = true;
        }

        public void setIgnore(boolean ignore) {
            this.ignore = ignore;
            this.stubTouched = true;
        }

        public void setMinlength(int minlength) {
            if (!minlengthSet) {
                this.minlength = minlength;
                this.stubTouched = true;
            }
        }

        public void setMaxlength(int maxlength) {
            if (!maxlengthSet) {
                this.maxlength = maxlength;
                this.stubTouched = true;
            }
        }

        public void setMinvalue(double minvalue) {
            this.minvalue = minvalue;
            this.stubTouched = true;
        }

        public void setMaxvalue(double maxvalue) {
            this.maxvalue = maxvalue;
            this.stubTouched = true;
        }

        public void setMask(String mask) {
            this.mask = mask;
            this.stubTouched = true;
        }

        public void setExpression(String expression) {
            this.expression = expression;
            this.stubTouched = true;
        }

        public void setConverter(Class<? extends TypeConverter<?>> converter) {
            this.converter = converter;
            this.stubTouched = true;
        }

        public void setLabel(String label) {
            this.label = label;
            this.stubTouched = true;
        }

        public String field() {
            return field;
        }

        public boolean encrypted() {
            return encrypted;
        }

        public boolean required() {
            return required;
        }

        public boolean trim() {
            return trim;
        }

        public String[] on() {
            return on;
        }

        public boolean ignore() {
            return ignore;
        }

        public int minlength() {
            return minlength;
        }

        public int maxlength() {
            return maxlength;
        }

        public double minvalue() {
            return minvalue;
        }

        public double maxvalue() {
            return maxvalue;
        }

        public String mask() {
            return mask;
        }

        public String expression() {
            return expression;
        }

        @SuppressWarnings("rawtypes")
        public Class<? extends TypeConverter> converter() {
            return converter;
        }

        public String label() {
            return label;
        }

        public Class<? extends Annotation> annotationType() {
            return Validate.class;
        }
    }
}
