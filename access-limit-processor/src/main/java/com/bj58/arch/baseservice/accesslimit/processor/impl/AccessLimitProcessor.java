package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.bj58.arch.baseservice.accesslimit.core.AccessGroupContext;
import com.bj58.arch.baseservice.accesslimit.core.QpsGroups;
import com.bj58.arch.baseservice.accesslimit.core.QpsManageGroup;
import com.bj58.arch.baseservice.accesslimit.core.StdQpsManageGroup;
import com.bj58.arch.baseservice.accesslimit.processor.AccessGroup;
import com.bj58.arch.baseservice.accesslimit.processor.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@SupportedAnnotationTypes({
        "com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit"
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(Processor.class)
public class AccessLimitProcessor extends AbstractProcessor {
    private static final String GENERATED_CLASS_PREFIX = "AccessLimit_";
    private static final String GENERATED_FALLBACK_PACKAGE = "com.bj58.spat.arch.baseservice.accesslimit.generated";
    private static final String GENERATED_GROUPS_CLASS = "AccessLimit_Groups";

    private Messager messager;
    private Filer filer;

    private final AtomicInteger methodIndex = new AtomicInteger(0);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableAccessLimit.class);

        final Map<String, AccessLimitGroupConfig> groups = Maps.newHashMap();

        final List<AccessLimitMethodConfig> methods = Lists.newArrayList();

        for (final Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                error(element,
                        "Annotation \"%s\" should only applied to CLASS level but found \"%s\"",
                        EnableAccessLimit.class, element.getKind());

                return false;
            }

            // Collect all @AccessGroup annotated classes
            {
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

            // Process all methods in target CLASS element
            final List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (final Element enclosedElement : enclosedElements) {
                if (ElementKind.METHOD != enclosedElement.getKind()) {
                    if (! checkAccessLimitGroupAnnotation(element) || ! checkAccessLimitMethodAnnotation(element))
                        return false;

                    /* Skipped */
                    continue;
                }

                final ExecutableElement methodElement = MoreElements.asExecutable(enclosedElement);

                // Collect all @AccessGroup annotated methods info
                {
                    final Optional<AccessLimitGroupConfig> newGroup = tryParseAccessLimitGroup(methodElement);
                    if (newGroup.isPresent()) {
                        final AccessLimitGroupConfig groupConfig = newGroup.get();
                        if (groups.containsKey(groupConfig.name())) {
                            error(methodElement,
                                    "AccessGroup \"%s\" had been defined already !!!",
                                    groupConfig.name());
                        } else {
                            groups.put(groupConfig.name(), groupConfig);
                        }
                    }
                }

                // Collect all @AccessLimit annotated methods info
                {
                    final Optional<AccessLimitMethodConfig> newMethod = tryParseAccessLimitMethod(methodElement);
                    if (newMethod.isPresent()) {
                        methods.add(newMethod.get());
                    }
                }
            }
        }

        generateGroupsClass(groups.values());

        for (final Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                generateSubClass(element, methods, groups);
            }
        }

        return true;
    }

    private void generateSubClass(
            final Element element,
            final List<AccessLimitMethodConfig> methods,
            final Map<String, AccessLimitGroupConfig> groups
    ) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(GENERATED_GROUPS_CLASS);
        builder.addAnnotation(
                AnnotationSpec.builder(ClassName.get(Generated.class))
                        .addMember("value", "$S", getClass().getCanonicalName())
                        .addMember("date", "$S", DateTime.now().toString(ISODateTimeFormat.dateTime()))
                        .build())
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC);


        // Process all methods in target CLASS element
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (final Element enclosedElement : enclosedElements) {
            if (ElementKind.METHOD != enclosedElement.getKind()) continue;

            // Collect all @AccessLimit annotated methods info
            final Optional<AccessLimitMethodConfig> newMethod = tryParseAccessLimitMethod(enclosedElement);
            if (newMethod.isPresent()) {

            }

        }
        
        // Write to output Java file
        final JavaFile javaFile = JavaFile.builder(GENERATED_FALLBACK_PACKAGE, builder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(null, "Failed to write generated Java source file");
            throw new IllegalStateException("Failed to write generated Java source file", e);
        }
    }

    private void generateGroupsClass(final Iterable<AccessLimitGroupConfig> groups) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(GENERATED_GROUPS_CLASS);
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
                        "$T.register(new $T($T.builder().id($S).maxLimit($LL).periodInMicros($LL).build()))",
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
        final JavaFile javaFile = JavaFile.builder(GENERATED_FALLBACK_PACKAGE, builder.build()).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(null, "Failed to write generated Java source file");
            throw new IllegalStateException("Failed to write generated Java source file", e);
        }
    }

    private Optional<AccessLimitMethodConfig> tryParseAccessLimitMethod(final Element element) {
        final AccessLimit anno = element.getAnnotation(AccessLimit.class);
        return (null != anno) ? Optional.of(
                AccessLimitMethodConfig.builder()
                        .methodName(element.getSimpleName().toString())
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

    private String adapteeVarName() {
        return "adaptee";
    }

    private String managerVarName() {
        return "qpsManager";
    }

    private String accessLimiterVarName(final int idx) {
        return "accessLimiter4Method" + idx;
    }

    private static String packageNameOf(final TypeElement type) {
        final PackageElement packageElement = MoreElements.getPackage(type);
        return packageElement.getQualifiedName().toString();
    }

    private static String classNameOf(final TypeElement type) {
        final String name = type.getQualifiedName().toString();
        final String pkgName = packageNameOf(type);
        return pkgName.isEmpty() ? name : name.substring(pkgName.length() + 1);
    }
}
