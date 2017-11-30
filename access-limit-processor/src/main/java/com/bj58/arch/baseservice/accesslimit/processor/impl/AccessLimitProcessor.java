package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.common.MoreElements;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.bj58.arch.baseservice.accesslimit.core.AccessEvent;
import com.bj58.arch.baseservice.accesslimit.core.QpsLimiter;
import com.bj58.arch.baseservice.accesslimit.processor.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableScfSupport;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
//@SupportedAnnotationTypes({
//        "com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit"
//})
//@SupportedSourceVersion(SourceVersion.RELEASE_6)
//@AutoService(Processor.class)
public class AccessLimitProcessor extends AbstractProcessor {
    private static final String GENERATED_CLASS_PREFIX = "AccessLimit_";
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

        for (final Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                final TypeElement typedElement = (TypeElement) element;

                final Element parentElement = typedElement.getEnclosingElement();
                if (ElementKind.PACKAGE != parentElement.getKind()) {
                    error(
                            element,
                            "Annotation \"%s\" should only applied to top level class but found an inner class",
                            EnableAccessLimit.class
                    );
                    return false;
                }

                final TypeSpec.Builder typeBuilder =
                        TypeSpec.classBuilder(GENERATED_CLASS_PREFIX + classNameOf(typedElement));

                try {
                    preprocessTypeElement(typedElement, typeBuilder);
                } catch (Exception e) {
                    return false;
                }

                try {
                    processAccessLimitEnabledClassTypeElement(typedElement, typeBuilder);
                } catch (Exception e) {
                    return false;
                }

                try {
                    processScfSupportEnabledClassTypeElement(typedElement, typeBuilder);
                } catch (Exception e) {
                    return false;
                }

                try {
                    writeAsSourceFile(typedElement, typeBuilder.build());
                } catch (Exception e) {
                    return false;
                }
            } else {
                error(
                        element,
                        "Annotation \"%s\" should only applied to CLASS level but found \"%s\"",
                        EnableAccessLimit.class, element.getKind()
                );

                return false;
            }
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

    private void writeAsSourceFile(final TypeElement element, final TypeSpec type) {
        final JavaFile javaFile = JavaFile.builder(packageNameOf(element), type).build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            error(element, "Failed to write generated Java source file");
            throw new IllegalStateException("Failed to write generated Java source file", e);
        }
    }

    private void preprocessTypeElement(
            final TypeElement element, final TypeSpec.Builder typeBuilder
    ) {
        typeBuilder.superclass(ClassName.get(element))
                .addAnnotation(
                        AnnotationSpec.builder(ClassName.get(Generated.class))
                                .addMember("value", "$S", getClass().getCanonicalName())
                                .addMember("date", "$S", DateTime.now().toString(ISODateTimeFormat.dateTime()))
                                .build()
                );
    }

    private void processScfSupportEnabledClassTypeElement(
            final TypeElement element, final TypeSpec.Builder typeBuilder
    ) {
        // Check @EnableScfSupport annotation
        final EnableScfSupport scfSupportAnno = element.getAnnotation(EnableScfSupport.class);
        if (null != scfSupportAnno) {
            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(ClassName.get("com.bj58.spat.scf.server.contract.annotation", "ServiceContract"))
                            .build()
            );
        }
    }

    private void processAccessLimitEnabledClassTypeElement(
            final TypeElement element, final TypeSpec.Builder typeBuilder
    ) {
        final ClassName originClass = ClassName.get(element);
//        typeBuilder.addField(
//                        FieldSpec.builder(originClass, adapteeVarName(), Modifier.PRIVATE, Modifier.FINAL)
//                                .initializer("new $T()", originClass)
//                                .build()
//                )
//                .addField(
//                        FieldSpec.builder(ClassName.get(QpsManager.class), managerVarName(), Modifier.PRIVATE, Modifier.FINAL)
//                                .initializer("$T.std()", AccessManagers.class)
//                                .build()
//                );

        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (final Element enclosedElement : enclosedElements) {
            if (ElementKind.METHOD == enclosedElement.getKind()) {
                processAccessLimitOnMethodTypeElement(
                        MoreElements.asExecutable(enclosedElement), typeBuilder
                );
            } else {
                /* Do nothing */
            }
        }
    }

    private void processAccessLimitOnMethodTypeElement(
            final ExecutableElement element, final TypeSpec.Builder typeBuilder
    ) {
        // Only process @AccessLimit annotated methods
        final AccessLimit accessLimitAnno = element.getAnnotation(AccessLimit.class);
        if (null == accessLimitAnno) return;

        // Do not add access limit protection for method with forbidden modifiers
        final Set<Modifier> modifiers = element.getModifiers();
        {
            final ImmutableList<Modifier> UNACCEPTABLE_MODIFIERS = ImmutableList.of(
                    Modifier.ABSTRACT, Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC
            );
            for (final Modifier modifier : UNACCEPTABLE_MODIFIERS) {
                if (modifiers.contains(modifier)) {
                    error(element, "Unable to override an method with \"%s\" modifier", modifier);
                    throw new IllegalStateException("Unable to override an method with \"" + modifier + "\" modifier");
                }
            }
        }

        final String curMethodName = element.getSimpleName().toString();
        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(curMethodName);

        // Method level modifiers
        methodBuilder.addModifiers(modifiers);

        // Returning type
        methodBuilder.returns(TypeName.get(element.getReturnType()));

        // Parameter types
        for (final TypeParameterElement typeParameterElement : element.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
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
            final String argsStr;
            {
                final List<? extends Element> parameters = element.getParameters();
                final List<String> parameterNames = Lists.newArrayListWithCapacity(parameters.size());
                for (final Element parameter : parameters) {
                    parameterNames.add(parameter.getSimpleName().toString());
                }
                argsStr = Joiner.on(", ").join(parameterNames);
            }

            final AccessLimitMethodConfig methodConfig = AccessLimitMethodConfig.builder()
                    .index(methodIndex.getAndIncrement())
                    .minLimit(accessLimitAnno.min())
                    .maxLimit(accessLimitAnno.max())
                    .seconds(accessLimitAnno.seconds())
                    .weight(accessLimitAnno.weight())
                    .build();

            final String accessLimiterName = accessLimiterVarName(methodConfig.index());

            // Add field named $accessLimiterName
            typeBuilder.addField(
                    FieldSpec.builder(
                            ClassName.get(QpsLimiter.class), accessLimiterName,
                            Modifier.PRIVATE, Modifier.FINAL
                    ).initializer(
                            "new $T($L, $L)",
                            QpsLimiter.class,
                            methodConfig.seconds(),
                            methodConfig.maxLimit(),
                            methodConfig.minLimit()
                    ).addJavadoc("$T instance for method $S", QpsLimiter.class, curMethodName).build()
            );

            // Add static init block
            typeBuilder.addInitializerBlock(
                    CodeBlock.builder()
//                            .add("/* Register $T instance $S to $T $S */\n", QpsLimiter.class, accessLimiterName, QpsManager.class, managerVarName())
                            .addStatement("$L.register($S, $N)", managerVarName(), accessLimiterName, accessLimiterName)
                            .build()
            );

            methodBuilder.addAnnotation(Override.class)
                    .addStatement("$L.onAccessed(new $T($S, $T.NANOSECONDS.toMicros($T.nanoTime()), $L))",
                            managerVarName(), AccessEvent.class, accessLimiterName, TimeUnit.class, System.class, methodConfig.weight())
                    .addStatement("$L.acquire($L)", accessLimiterVarName(methodConfig.index()), methodConfig.weight())
                    .beginControlFlow("try")
                    .addStatement("$L.$L(" + argsStr + ")", adapteeVarName(), curMethodName)
                    .endControlFlow()
                    .beginControlFlow("finally")
                    .addStatement("$L.release($L)", accessLimiterName, methodConfig.weight())
                    .endControlFlow();
        }

        final MethodSpec methodSpec = methodBuilder.build();
        typeBuilder.addMethod(methodSpec);
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
