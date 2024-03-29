package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.bj58.arch.baseservice.accesslimit.core.AccessAware;
import com.bj58.arch.baseservice.accesslimit.core.AccessAwares;
import com.bj58.arch.baseservice.accesslimit.core.AccessEvent;
import com.bj58.arch.baseservice.accesslimit.core.AccessGroupContext;
import com.bj58.arch.baseservice.accesslimit.core.AccessMethodContext;
import com.bj58.arch.baseservice.accesslimit.core.QpsGroups;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiter;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiters;
import com.bj58.arch.baseservice.accesslimit.core.QpsManageGroup;
import com.bj58.arch.baseservice.accesslimit.core.QpsManageLeaf;
import com.bj58.arch.baseservice.accesslimit.core.StdQpsManageGroup;
import com.bj58.arch.baseservice.accesslimit.core.StdQpsManageLeaf;
import com.bj58.arch.baseservice.accesslimit.processor.AccessGroup;
import com.bj58.arch.baseservice.accesslimit.processor.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

/**
 * TODO add brief description here
 *
 * Copyright © 2019 wangbo.im. All rights reserved.
 *
 * @author Elvis Wang [mail _AT_ wangbo _DOT_ im]
 */
@SupportedAnnotationTypes({
        "com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit"
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(Processor.class)
public class AccessLimitProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;

    private Map<String, AccessLimitGroupConfig> theGroups = ImmutableMap.of();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateGroupsClass(theGroups.values());
        } else {
            // Process @EnableAccessLimit annotated elements
            final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableAccessLimit.class);
            processEnableAccessLimitAnnotatedElements(elements);
        }

        // never claim annotation, because who knows what other processors want?
        return false;
    }

    private void processEnableAccessLimitAnnotatedElements(final Collection<? extends Element> elements) {
        if (elements.isEmpty()) return;

        for (final Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element,
                        "Annotation \"%s\" should only applied to CLASS level but found \"%s\"",
                        EnableAccessLimit.class, element.getKind());
            }

            final TypeElement typeElement = MoreElements.asType(element);

            // Process @AccessGroup annotated elements
            processAndLoadAccessGroupAnnotatedElement(typeElement);

            generateSubClass(typeElement, theGroups);
        }
    }

    private void processAndLoadAccessGroupAnnotatedElement(final TypeElement typeElement) {
        final Map<String, AccessLimitGroupConfig> groups = Maps.newHashMap(theGroups);

        // Top level type
        {
            final Optional<AccessLimitGroupConfig> newGroup = tryParseAccessLimitGroup(typeElement);
            if (newGroup.isPresent()) {
                final AccessLimitGroupConfig groupConfig = newGroup.get();
                if (groups.containsKey(groupConfig.name())) {
                    error(typeElement, "AccessGroup \"%s\" had been defined already !!!",
                            groupConfig.name());
                } else {
                    groups.put(groupConfig.name(), groupConfig);
                }
            }
        }

        for (final Element element : typeElement.getEnclosedElements()) {
            // Inner level type

            final Optional<AccessLimitGroupConfig> newGroup = tryParseAccessLimitGroup(element);
            if (newGroup.isPresent()) {
                final AccessLimitGroupConfig groupConfig = newGroup.get();
                if (groups.containsKey(groupConfig.name())) {
                    error(element, "AccessGroup \"%s\" had been defined already !!!",
                            groupConfig.name());
                } else {
                    groups.put(groupConfig.name(), groupConfig);
                }
            }
        }

        this.theGroups = ImmutableMap.copyOf(groups);
    }

    private void generateSubClass(
            final TypeElement element,
            final Map<String, AccessLimitGroupConfig> groups
    ) {
        final String packageName = Utils.packageNameOf(element);
        final String combinedClassName = Utils.combinedClassNameOf(element, "_");

        final TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.GENERATED_CLASS_PREFIX + combinedClassName)
                .superclass(ClassName.get(element))
                .addAnnotation(
                        AnnotationSpec.builder(ClassName.get(Generated.class))
                                .addMember("value", "$S", getClass().getCanonicalName())
                                .addMember("date", "$S", DateTime.now().toString(ISODateTimeFormat.dateTime()))
                                .build()
                )
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        // Collect method configs
        List<AccessLimitMethodConfig> methods = ImmutableList.of();

        // Collect all @AccessLimit annotated methods info
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (final Element enclosedElement : enclosedElements) {
            if (ElementKind.METHOD != enclosedElement.getKind()) continue;

            final ExecutableElement methodElement = MoreElements.asExecutable(enclosedElement);

            // Collect all @AccessLimit annotated methods info
            methods = parseAndGenerateAccessLimitMethod(methodElement, builder, methods, groups);
        }

        // Add fields
        generateFields(element, builder);

        // Add Constructor
        generateConstructor(element, builder, methods);

        // Write to output Java file
        final JavaFile javaFile = JavaFile.builder(packageName, builder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(element, "Failed to write generated Java source file \"%s.%s\"", packageName, combinedClassName);
        }
    }

    private void generateGroupsClass(final Collection<AccessLimitGroupConfig> groups) {
        if (groups.isEmpty()) return;

        final TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.GENERATED_GROUPS_CLASS_NAME);
        builder.addAnnotation(
                AnnotationSpec.builder(ClassName.get(Generated.class))
                        .addMember("value", "$S", getClass().getCanonicalName())
                        .addMember("date", "$S", DateTime.now().toString(ISODateTimeFormat.dateTime()))
                        .build())
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);

        // Add static init block
        {
            final CodeBlock.Builder fieldInit = CodeBlock.builder();
            for (final AccessLimitGroupConfig group : groups) {
                fieldInit.addStatement(
                        "$T.register(new $T(\n$T.builder()\n.id($S)\n.maxLimit($LL)\n.periodInMicros($LL)\n.build()))",
                        QpsGroups.class, StdQpsManageGroup.class, AccessGroupContext.class,
                        group.name(), group.maxPermits(), group.micros()
                );
            }

            builder.addStaticBlock(fieldInit.build());
        }

        // Add private constructor
        {
            final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addCode(CodeBlock.builder()
                            .addStatement("throw new $T($S)", AssertionError.class, "Construction forbidden")
                            .build());

            builder.addMethod(constructorBuilder.build());
        }

        // Add static method
        {
            final String varName = "key";
            final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("get")
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .returns(ClassName.get(QpsManageGroup.class))
                    .addParameter(ParameterSpec.builder(ClassName.get(String.class), varName, Modifier.FINAL).build())
                    .addCode(
                            CodeBlock.builder()
                                    .addStatement("return $T.get($L)", QpsGroups.class, varName)
                                    .build()
                    );

            builder.addMethod(methodBuilder.build());
        }

        // Write to output Java file
        final TypeSpec typeSpec = builder.build();
        final JavaFile javaFile = JavaFile.builder(Constants.GENERATED_FALLBACK_PACKAGE, typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(null, "Failed to write generated Java source file \"%s.%s\"", Constants.GENERATED_FALLBACK_PACKAGE, typeSpec.name);
        }
    }

    private void generateConstructor(
            final TypeElement element,
            final TypeSpec.Builder typeBuilder,
            final List<AccessLimitMethodConfig> methods
    ) {
        final CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("super()");

        final String accessAwares = "accessAwares";
        final String groupPrefix = "group";
        final String leafPrefix = "leaf";

        codeBuilder.addStatement("$L = new $T()", Constants.GENERATED_ADAPTEE_VAR_NAME, ClassName.get(element));

        final int methodsCount = methods.size();

        codeBuilder.addStatement(
                "$L = new $T[$L]",
                Constants.GENERATED_QPS_LIMITER_VAR_NAME,
                ClassName.get(QpsLimiter.class),
                methodsCount
        );

        // Local var: accessAwares
        {
            final TypeName typeName = ParameterizedTypeName.get(HashMap.class, String.class, AccessAware.class);
            codeBuilder.addStatement(
                    "final $T $L = new $T($L)",
                    typeName, accessAwares, typeName, methodsCount
            );
        }

        for (int i = 0; i < methodsCount; i++) {
            AccessLimitMethodConfig methodConfig = methods.get(i);

            final String groupName = groupPrefix + methodConfig.methodIndex();
            final String leafName = leafPrefix + methodConfig.methodIndex();

            codeBuilder.add("\n").addStatement(
                    "final $T $L = $T.get($S)",
                    QpsManageGroup.class, groupName, QpsGroups.class,
                    methodConfig.groupName()
            ).addStatement(
                    "final $T $L = new $T($L, \n$T.builder()\n.id($S)\n.limit($LL, $LL)\n.weight($L)\n.build())",
                    QpsManageLeaf.class, leafName, StdQpsManageLeaf.class,
                    groupName, AccessMethodContext.class,
                    methodConfig.uuid(),
                    methodConfig.maxPermits(),
                    methodConfig.minPermits(),
                    methodConfig.weight()
            ).addStatement(
                    "$L.put($S, $T.create($L.context(), $L))",
                    accessAwares, methodConfig.uuid(), AccessAwares.class, groupName, leafName
            ).addStatement(
                    "$L = $T.create($L.context(), $L)",
                    Constants.qpsLimiterVarName(i), QpsLimiters.class, groupName, leafName
            ).add("\n");
        }

        codeBuilder.addStatement(
                "$L = $T.composed($L)",
                Constants.GENERATED_ACCESS_AWARE_VAR_NAME,
                AccessAwares.class,
                accessAwares
        );

        typeBuilder.addMethod(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(codeBuilder.build())
                        .build()
        );
    }

    private void generateFields(final TypeElement element, final TypeSpec.Builder builder) {
        builder.addField(FieldSpec.builder(ClassName.get(element),
                Constants.GENERATED_ADAPTEE_VAR_NAME, Modifier.FINAL, Modifier.PRIVATE).build()
        ).addField(FieldSpec.builder(ClassName.get(AccessAware.class),
                Constants.GENERATED_ACCESS_AWARE_VAR_NAME, Modifier.FINAL, Modifier.PRIVATE).build()
        ).addField(FieldSpec.builder(ArrayTypeName.of(QpsLimiter.class),
                Constants.GENERATED_QPS_LIMITER_VAR_NAME, Modifier.PRIVATE, Modifier.FINAL
        ).build());
    }

    private List<AccessLimitMethodConfig> parseAndGenerateAccessLimitMethod(
            final ExecutableElement element, final TypeSpec.Builder typeBuilder,
            final List<AccessLimitMethodConfig> handled,
            final Map<String, AccessLimitGroupConfig> groups
    ) {
        final AccessLimit anno = element.getAnnotation(AccessLimit.class);
        if (null == anno) {
            return handled;
        }

        final String groupName = anno.group();

        if (! groups.containsKey(anno.group())) {
            error(element, "No group named \"%s\" defined", groupName);
            return handled;
        }

        // Do not add access limit protection for method with forbidden modifiers
        final Set<Modifier> modifiers = element.getModifiers();
        {
            final ImmutableList<Modifier> UNACCEPTABLE_MODIFIERS = ImmutableList.of(
                    Modifier.ABSTRACT, Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC
            );
            for (final Modifier modifier : UNACCEPTABLE_MODIFIERS) {
                if (modifiers.contains(modifier)) {
                    error(element,
                            "Annotation \"%s\" can not be applied to method \"%s\" with modifier \"%s\"",
                            modifier);
                }
            }
        }

        final AccessLimitMethodConfig methodConfig = AccessLimitMethodConfig.builder()
                .methodName(element.getSimpleName().toString())
                .methodIndex(handled.size())
                .groupName(groupName)
                .maxPermits(anno.max())
                .minPermits(anno.min())
                .weight(anno.weight())
                .build();

        final String curMethodName = element.getSimpleName().toString();

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(curMethodName)
                .addAnnotation(Override.class);

        // Method level modifiers
        methodBuilder.addModifiers(modifiers);

        // Returning type
        methodBuilder.returns(TypeName.get(element.getReturnType()));

        // Parameter types
        for (final TypeParameterElement typeParameterElement : element.getTypeParameters()) {
            final TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        // Parameter args
        {
            final List<ParameterSpec> result = Lists.newArrayList();
            for (final VariableElement parameter : element.getParameters()) {
                final ParameterSpec.Builder builder = ParameterSpec.builder(
                        TypeName.get(parameter.asType()), parameter.getSimpleName().toString()
                );

                for (final AnnotationMirror annotationMirror : parameter.getAnnotationMirrors()) {
                    builder.addAnnotation(AnnotationSpec.get(annotationMirror));
                }

                builder.addModifiers(parameter.getModifiers());

                result.add(builder.build());
            }
            methodBuilder.addParameters(result);
        }

        // VarArg
        methodBuilder.varargs(element.isVarArgs());

        // Throwing exceptions
        for (TypeMirror thrownType : element.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        // Implementation code block
        {
            // Line 1
            methodBuilder.addStatement(
                    "$L.onAccessed(\n$T.builder()\n.sourceId($S)\n.timeStampInMicros($T.NANOSECONDS.toMicros($T.nanoTime()))\n.count($L)\n.build())",
                    Constants.GENERATED_ACCESS_AWARE_VAR_NAME, AccessEvent.class,
                    methodConfig.uuid(), TimeUnit.class, System.class,
                    methodConfig.weight()
            );

            // Line 2
            {
                methodBuilder.addStatement(
                        "$L.acquire($L)",
                        Constants.qpsLimiterVarName(methodConfig.methodIndex()),
                        methodConfig.weight()
                );
            }

            // Block 3
            {
                final List<? extends Element> parameters = element.getParameters();
                final List<String> parameterNames = Lists.newArrayListWithCapacity(parameters.size());
                for (final Element parameter : parameters) {
                    parameterNames.add(parameter.getSimpleName().toString());
                }

                final String argsStr = Joiner.on(", ").join(parameterNames);

                methodBuilder.beginControlFlow("try")
                        .addStatement("$L.$L(" + argsStr + ")", Constants.GENERATED_ADAPTEE_VAR_NAME, curMethodName)
                        .endControlFlow()
                        .beginControlFlow("finally")
                        .addStatement(
                                "$L.release($L)",
                                Constants.qpsLimiterVarName(methodConfig.methodIndex()),
                                methodConfig.weight())
                        .endControlFlow();
            }
        }

        final MethodSpec methodSpec = methodBuilder.build();
        typeBuilder.addMethod(methodSpec);

        return ImmutableList.<AccessLimitMethodConfig>builder()
                .addAll(handled).add(methodConfig)
                .build();
    }

    private Optional<AccessLimitMethodConfig> tryParseAccessLimitMethod(final Element element) {
        final AccessLimit anno = element.getAnnotation(AccessLimit.class);
        return (null != anno) ? Optional.of(
                AccessLimitMethodConfig.builder()
                        .methodName(element.getSimpleName().toString())
                        .methodIndex(0)
                        .groupName(anno.group())
                        .maxPermits(anno.max())
                        .minPermits(anno.min())
                        .weight(anno.weight())
                        .build()
        ) : Optional.<AccessLimitMethodConfig>absent();
    }

    private Optional<AccessLimitGroupConfig> tryParseAccessLimitGroup(final Element element) {
        final AccessGroup anno = element.getAnnotation(AccessGroup.class);
        return (null != anno) ? Optional.of(
                AccessLimitGroupConfig.builder()
                        .name(anno.name())
                        .maxPermits(anno.max())
                        .micros(TimeUnit.SECONDS.toMicros(anno.seconds()))
                        .build()
        ) : Optional.<AccessLimitGroupConfig>absent();
    }

    private boolean checkAccessLimitMethodAnnotation(final Element element) {
        final AccessLimit anno = element.getAnnotation(AccessLimit.class);
        if (null != anno && element.getKind() != ElementKind.METHOD) {
            error(element,
                    "Annotation \"%s\" should only applied to METHOD level but found \"%s\"",
                    AccessLimit.class, element.getKind());

            return false;
        }

        return true;
    }
    private boolean checkAccessLimitGroupAnnotation(final Element element) {
        final AccessGroup anno = element.getAnnotation(AccessGroup.class);
        if (null != anno && (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.METHOD)) {
            error(element,
                    "Annotation \"%s\" should only applied to CLASS/METHOD level but found \"%s\"",
                    AccessGroup.class, element.getKind());

            return false;
        }

        return true;
    }

    private void error(final Element element, final String format, final Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(format, args),
                element
        );
    }
}
