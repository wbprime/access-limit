package com.bj58.arch.baseservice.accesslimit.processor.impl;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.bj58.arch.baseservice.accesslimit.core.QpsLimiter;
import com.bj58.arch.baseservice.accesslimit.processor.AccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit;
import com.bj58.arch.baseservice.accesslimit.processor.EnableScfSupport;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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

import javax.annotation.Generated;
import javax.annotation.Nullable;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.google.common.base.Preconditions.checkState;

/**
 * TODO add brief description here
 *
 * Copyright © 2016 58ganji Beijing spat team. All rights reserved.
 *
 * @author Elvis Wang [wangbo12 -AT- 58ganji -DOT- com]
 */
@SupportedAnnotationTypes({
        "com.bj58.arch.baseservice.accesslimit.processor.EnableAccessLimit",
        "com.bj58.arch.baseservice.accesslimit.processor.AccessLimit"
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(Processor.class)
public class AccessLimitProcessor extends AbstractProcessor {
    private static final String GENERATED_CLASS_PREFIX = "AccessLimit_";
    private Messager messager;
    private Filer filer;

    private Elements elements;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(EnableAccessLimit.class);

        for (final Element element : elements) {
            checkState(
                    ElementKind.CLASS == element.getKind(),
                    "Annotation \"%s\" should only applied to Class level but found on %s",
                    EnableAccessLimit.class, element.getKind()
            );

            final TypeElement typedElement = (TypeElement) element;

            final TypeSpec typeSpec = processAccessLimitEnabledClassTypeElement(typedElement);

            writeAsSourceFile(packageNameOf(typedElement), typeSpec);
        }

        return true;
    }

    private void writeAsSourceFile(final String packageName, final TypeSpec type) {
        final JavaFile javaFile = JavaFile.builder(packageName, type).build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write out Java source file", e);
        }
    }

    private TypeSpec processAccessLimitEnabledClassTypeElement(final TypeElement element) {
        final String originClassName = classNameOf(element);
        final TypeSpec.Builder typeBuilder =
                TypeSpec.classBuilder(GENERATED_CLASS_PREFIX + originClassName);

        final List<AccessLimitMethodConfig> methodConfigs = Lists.newArrayList();

        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (final Element enclosedElement : enclosedElements) {
            final AccessLimit accessLimitAnno = enclosedElement.getAnnotation(AccessLimit.class);
            if (null == accessLimitAnno) continue;

            if (ElementKind.METHOD == enclosedElement.getKind()) {
                final AccessLimitMethodConfig methodConfig = AccessLimitMethodConfig.builder()
                        .index(methodConfigs.size())
                        .limit(accessLimitAnno.limit())
                        .seconds(accessLimitAnno.seconds())
                        .weight(accessLimitAnno.weight())
                        .build();

                final MethodSpec methodSpec = processAccessLimitOnMethodTypeElement(
                        MoreElements.asExecutable(enclosedElement), methodConfig
                );

                if (null != methodSpec) {
                    typeBuilder.addMethod(methodSpec);

                    methodConfigs.add(methodConfig);
                }
            } else {
                throw new IllegalStateException(
                        String.format(
                                "Annotation \"%s\" should only applied to Method level but found on %s",
                                AccessLimit.class, enclosedElement.getKind()
                        )
                );
            }
        }

        final String originPackageName = packageNameOf(element);
        final ClassName originClass = ClassName.get(originPackageName, originClassName);

        // Check @EnableScfSupport annotation
        {
            final EnableScfSupport scfSupportAnno = element.getAnnotation(EnableScfSupport.class);
            if (null != scfSupportAnno) {
                typeBuilder.addAnnotation(
                        AnnotationSpec.builder(ClassName.get("com.bj58.spat.scf.server.contract.annotation", "ServiceContract")).build()
                );
            }
        }

        typeBuilder.superclass(originClass)
                .addAnnotation(
                        AnnotationSpec.builder(ClassName.get(Generated.class))
                                .addMember("value", "$S", getClass().getCanonicalName())
                                .addMember("date", "$S", DateTime.now().toString(ISODateTimeFormat.dateTime()))
                                .build()
                )
                .addField(
                        FieldSpec.builder(originClass, adapteeVarName(), Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("new $T()", originClass)
                                .build()
                );
        for (final AccessLimitMethodConfig methodConfig : methodConfigs) {
            typeBuilder.addField(
                    FieldSpec.builder(
                            ClassName.get(QpsLimiter.class),
                            accessLimiterVarName(methodConfig.index()),
                            Modifier.PRIVATE, Modifier.FINAL
                    ).initializer(
                            "new $T($L, $L)",
                            QpsLimiter.class,
                            methodConfig.seconds(),
                            methodConfig.limit()
                    ).build()
            );
        }

        return typeBuilder.build();
    }

    @Nullable
    private MethodSpec processAccessLimitOnMethodTypeElement(
            final ExecutableElement element, final AccessLimitMethodConfig methodConfig
    ) {
        final String curMethodName = element.getSimpleName().toString();

        // Do not add access limit protection for unnecessary modified method
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.containsAll(ImmutableSet.of(Modifier.ABSTRACT, Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC))) {
            return null;
        }

        final String argsStr;
        {
            final List<? extends Element> parameters = element.getParameters();
            final List<String> parameterNames = Lists.newArrayListWithCapacity(parameters.size());
            for (final Element parameter : parameters) {
                parameterNames.add(parameter.getSimpleName().toString());
            }
            argsStr = Joiner.on(", ").join(parameterNames);
        }

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(curMethodName);

        // Method level modifiers
        {
            modifiers = Sets.newLinkedHashSet(modifiers);
            modifiers.remove(Modifier.ABSTRACT);
            methodBuilder.addModifiers(modifiers);
        }

        // Returning
        {
            methodBuilder.returns(TypeName.get(element.getReturnType()));
        }

        // Parameter types
        {
            for (TypeParameterElement typeParameterElement : element.getTypeParameters()) {
                TypeVariable var = (TypeVariable) typeParameterElement.asType();
                methodBuilder.addTypeVariable(TypeVariableName.get(var));
            }
        }

        // Parameters
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
        {
            methodBuilder.varargs(element.isVarArgs());
        }

        // Throwing exceptions
        {
            for (TypeMirror thrownType : element.getThrownTypes()) {
                methodBuilder.addException(TypeName.get(thrownType));
            }
        }

        // Implementation code block
        methodBuilder.addAnnotation(Override.class)
                .addStatement("$L.acquire($L)", accessLimiterVarName(methodConfig.index()), methodConfig.weight())
                .beginControlFlow("try")
                .addStatement("$L.$L(" + argsStr + ")", adapteeVarName(), curMethodName)
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("$L.release($L)", accessLimiterVarName(methodConfig.index()), methodConfig.weight())
                .endControlFlow();

        return methodBuilder.build();
    }

    private String adapteeVarName() {
        return "adaptee";
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
