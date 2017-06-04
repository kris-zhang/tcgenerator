/*
 * (C) Copyright ${year} Nuxeo (http://nuxeo.com/) and others.
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
 *
 */
package com.kriszhang.tcgenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kriszhang.tcgenerator.Configuration.ContextConfig;
import com.kriszhang.tcgenerator.Configuration.TransformConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isInterface;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isStrict;
import static java.lang.reflect.Modifier.isSynchronized;
import static java.lang.reflect.Modifier.isTransient;
import static java.lang.reflect.Modifier.isVolatile;

/**
 * @author gongzuo.zy
 * @version $Id: ContextBuilder.java, v0.1 2017-06-02 23:39  gongzuo.zy Exp $
 */
class ContextBuilder {
    private static ContextBuilder INSTANCE = new ContextBuilder();

    static ContextBuilder getInstance () {
        return INSTANCE;
    }

    private ContextBuilder() {}

    @Data
    @EqualsAndHashCode(callSuper = false)
    class Context extends HashMap<String, Object> {
    }

    @Data
    class GlobalContext {
        List<Context> contexts;
    }

    GlobalContext buildGlobalContext(Collection<Class> classes) {
        GlobalContext globalContext = new GlobalContext();
        globalContext.setContexts(classes.stream().map(c->ContextBuilder.getInstance().buildContext(c)).collect(Collectors.toList()));
        return globalContext;
    }

    Context buildContext(Class clazz) {

        Context params = new Context();

        buildMethodInfo(clazz, params);
        buildClassInfo(clazz, params);
        buildPackageInfo(clazz, params);
        buildFieldInfo(clazz, params);
        buildDateTime(params);
        customize(params);

        return params;
    }

    private void buildFieldInfo(Class clazz, Map<String, Object> params) {
        Field[] fields = clazz.getDeclaredFields();

        if (fields == null || fields.length <=0) {
            return;
        }

        List<FieldModel> fieldModels = Lists.newArrayList();

        for (Field field : fields) {
            FieldModel fieldModel = new FieldModel();
            fieldModel.setName(field.getName());
            TypeModel typeModel = new TypeModel();
            typeModel.setName(field.getType().getName());
            typeModel.setSimpleName(field.getType().getSimpleName());
            fieldModel.setType(typeModel);
            buildCommonModel(field.getModifiers(), field.getDeclaredAnnotations(), fieldModel);

            if (Configuration.getTransformConfig() != null
                && !isNullOrEmpty(Configuration.getTransformConfig().getFieldsExpression())) {
                ScriptEngine.getInstance().eval(Configuration.getTransformConfig().getFieldsExpression(), fieldModel);
            }

            fieldModels.add(fieldModel);
        }
        params.put(ContextKey.FIELDS, fieldModels);
    }

    private void customize(Map<String, Object> params) {
        ContextConfig contextConfig = Configuration.getContextConfig();
        if (contextConfig.getCustomize() != null) {
            contextConfig.getCustomize().forEach((key, value) ->
                params.put(key, ScriptEngine.getInstance().eval(value, params)));
        }
    }

    private void buildDateTime(Map<String, Object> params) {
        LocalDateTime localDateTime = LocalDateTime.now();
        params.put(ContextKey.YEAR, normalize(localDateTime.getYear()));
        params.put(ContextKey.MONTH, normalize(localDateTime.getMonthValue()));
        params.put(ContextKey.DAY, normalize(localDateTime.getDayOfMonth()));
        params.put(ContextKey.MINUTE, normalize(localDateTime.getMinute()));
        params.put(ContextKey.SECOND, normalize(localDateTime.getSecond()));
        params.put(ContextKey.HOUR24, normalize(localDateTime.getHour()));
    }

    private String normalize(int a) {
        return a < 10 ? "0" + a:String.valueOf(a);
    }

    private void buildMethodInfo(Class clazz, Map<String, Object> params) {
        Method[] methods = clazz.getDeclaredMethods();

        if (methods.length <= 0) {
            return;
        }

        List<MethodModel> methodObjects = Lists.newArrayList();
        Map<String, Integer> overrideMap = Maps.newHashMap();

        for (Method method : methods) {
            MethodModel methodModel = new MethodModel();

            if (overrideMap.containsKey(method.getName())) {
                overrideMap.put(method.getName(), overrideMap.get(method.getName()) + 1);
                methodModel.setOverrideIndex(overrideMap.get(method.getName()));
            } else {
                overrideMap.put(method.getName(), 0);
            }

            TypeModel retType = new TypeModel();
            retType.setName(method.getReturnType().getName());
            retType.setSimpleName(method.getReturnType().getSimpleName());

            List<TypeModel> paramTypes = Lists.newArrayList();

            Arrays.stream(method.getParameterTypes()).forEach(t->{
                TypeModel typeModel = new TypeModel();
                typeModel.setName(t.getName());
                typeModel.setSimpleName(t.getSimpleName());
                paramTypes.add(typeModel);
            });

            methodModel.setParamTypes(paramTypes);

            methodModel.setRetType(retType);
            methodModel.setName(method.getName());
            methodModel.setIsPublic(isPublic(method.getModifiers()));
            buildCommonModel(method.getModifiers(), method.getDeclaredAnnotations(), methodModel);

            if (Configuration.getTransformConfig() != null
                && !isNullOrEmpty(Configuration.getTransformConfig().getMethodsExpression())) {
                ScriptEngine.getInstance().eval(Configuration.getTransformConfig().getMethodsExpression(), methodModel);
            }

            methodObjects.add(methodModel);
        }

        params.put(ContextKey.METHODS, methodObjects);
    }

    private void buildClassInfo(Class clazz, Map<String, Object> params) {
        ClassModel classModel = new ClassModel();
        classModel.setName(clazz.getName());
        classModel.setSimpleName(clazz.getSimpleName());

        buildCommonModel(clazz.getModifiers(), clazz.getDeclaredAnnotations(), classModel);

        if (Configuration.getTransformConfig() != null
            && !isNullOrEmpty(Configuration.getTransformConfig().getClassExpression())) {
            ScriptEngine.getInstance().eval(Configuration.getTransformConfig().getClassExpression(), classModel);
        }
        params.put(ContextKey.CLASS, classModel);
    }

    private void buildPackageInfo(Class clazz, Map<String, Object> params) {
        PackageModel packageModel = new PackageModel();
        Package classPackage = clazz.getPackage();
        if(classPackage != null) {
            packageModel.setName(classPackage.getName());

            if (Configuration.getTransformConfig() != null
                && !isNullOrEmpty(Configuration.getTransformConfig().getPackageExpression())) {
                ScriptEngine.getInstance().eval(Configuration.getTransformConfig().getPackageExpression(), packageModel);
            }

            params.put(ContextKey.PACKAGE, packageModel);
        } else {
            System.out.println("Class Default Package:" + clazz.getName());
        }
    }

    private void buildCommonModel(int modifier, Annotation[] annotations, CommonModel modifierModel) {
        modifierModel.setIsAbstract(isAbstract(modifier));
        modifierModel.setIsFinal(isFinal(modifier));
        modifierModel.setIsInterface(isInterface(modifier));
        modifierModel.setIsNative(isNative(modifier));
        modifierModel.setIsPrivate(isPrivate(modifier));
        modifierModel.setIsProtected(isProtected(modifier));
        modifierModel.setIsPublic(isPublic(modifier));
        modifierModel.setIsStatic(isStatic(modifier));
        modifierModel.setIsStrict(isStrict(modifier));
        modifierModel.setIsSynchronized(isSynchronized(modifier));
        modifierModel.setIsTransient(isTransient(modifier));
        modifierModel.setIsVolatile(isVolatile(modifier));

        List<AnnotationModel> annotationModels = Lists.newArrayList();
        Arrays.stream(annotations).forEach(a->{
            AnnotationModel annotationModel = new AnnotationModel();
            annotationModel.setName(a.annotationType().getSimpleName());
            annotationModels.add(annotationModel);
        });
        modifierModel.setAnnotations(annotationModels);
    }

    private interface ContextKey {
        String PACKAGE  = "packageModel";
        String METHODS  = "methodModels";
        String CLASS    = "classModel";
        String FIELDS   = "fieldModels";
        String YEAR     = "yyyy";
        String MONTH    = "MM";
        String DAY      = "dd";
        String HOUR24   = "HH";
        String MINUTE   = "mm";
        String SECOND   = "ss";
    }

    @Data
    public class PackageModel {
        String name;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ClassModel extends CommonModel {
        String name;
        String simpleName;
        List<AnnotationModel> annotations;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class MethodModel extends CommonModel {
        String name;
        Integer overrideIndex;
        TypeModel retType;
        List<TypeModel> paramTypes;
        List<AnnotationModel> annotations;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class FieldModel extends CommonModel {
        String name;
        TypeModel type;
        List<AnnotationModel> annotations;
    }

    @Data
    public class CommonModel {
        Boolean isPublic;
        Boolean isPrivate;
        Boolean isProtected;
        Boolean isStatic;
        Boolean isFinal;
        Boolean isSynchronized;
        Boolean isVolatile;
        Boolean isTransient;
        Boolean isNative;
        Boolean isInterface;
        Boolean isAbstract;
        Boolean isStrict;
        List<AnnotationModel> annotations;
    }

    @Data
    public class AnnotationModel {
        String name;
    }

    @Data
    public class TypeModel {
        String name;
        String simpleName;
    }

}

