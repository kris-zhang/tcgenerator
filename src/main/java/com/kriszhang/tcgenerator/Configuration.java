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


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

import com.google.common.base.Joiner;
import lombok.Data;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;

/**
 * @author gongzuo.zy
 * @version $Id: Configuration.java, v0.1 2017-06-02 22:52  gongzuo.zy Exp $
 */
class Configuration {

    private static Config config;

    static void loadJsonConfig(String configFile) throws IOException {
        checkConfig(config = parseObject(Joiner.on("").join(
            readLines(new File(configFile).toURI().toURL(), UTF_8)), Config.class));
    }

    private static void checkConfig(Config config) {
        if (config == null) {
            throw new GeneratorException("[config] Error");
        }

        if (config.getMode() == null) {
            throw new GeneratorException("[mode] of config is necessary");
        }

        checkRenderConfig();
        checkOutputConfig();
        checkInputConfig();
    }

    private static void checkRenderConfig() {
        RenderConfig renderConfig = config.getRenderConfig();
        if (renderConfig == null) {
            throw new GeneratorException("[render] config is necessary");
        }

        if (isNullOrEmpty(renderConfig.getTemplatePath())) {
            throw new GeneratorException("[templatePath] of [render] config is necessary");
        }
    }

    private static void checkOutputConfig() {
        OutputConfig outputConfig = config.getOutputConfig();

        if (outputConfig == null) {
            throw new GeneratorException("[output] config is necessary");
        } else{
            if (isNullOrEmpty(outputConfig.getFileNameExpression())) {
                throw new GeneratorException("[fileNameExpression] of [output] config is necessary");
            }
            if (isNullOrEmpty(outputConfig.getPathExpression())) {
                throw new GeneratorException("[pathExpression] of [output] config is necessary");
            }
        }
    }

    private static void checkInputConfig() {
        InputConfig inputConfig = config.getInputConfig();

        if (inputConfig == null) {
            throw new GeneratorException("[input] config is necessary");
        } else {
            if (inputConfig.getClasspath() == null || inputConfig.getClasspath().isEmpty()) {
                throw new GeneratorException("[classPath] of [input] config is necessary");
            }

            FilterConfig filterConfig = inputConfig.getFilter();

            if (filterConfig == null) {
                throw new GeneratorException("[filter] config is necessary");
            } else {
                if (isNullOrEmpty(filterConfig.getTargetPackage())) {
                    throw new GeneratorException("[targetPackage] of [filter] config is necessary");
                }
            }
        }
    }

    static Config getConfig() {
        return config;
    }

    static OutputConfig getOutputConfig() {
        return config.getOutputConfig();
    }

    static InputConfig getInputConfig() {
        return config.getInputConfig();
    }

    static RenderConfig getRenderConfig() {
        return config.getRenderConfig();
    }

    static FilterConfig getFilterConfig() {
        return config.getInputConfig().getFilter();
    }

    static TransformConfig getTransformConfig() {
        return config.getContextConfig().getTransformConfig();
    }

    static ContextConfig getContextConfig() {
        return config.getContextConfig();
    }

    @Data
    public static class Config {
        @JSONField(name = "input")
        InputConfig inputConfig;

        @JSONField(name = "output")
        OutputConfig outputConfig;

        @JSONField(name = "render")
        RenderConfig renderConfig;

        @JSONField(name = "context")
        ContextConfig contextConfig;

        boolean parallel =false;
        boolean skipError = false;
        Mode mode = Mode.SCAN;
    }

    public static enum Mode {
        /**
         * scan all files
         */
        SCAN,

        /**
         * gather files statistics
         */
        STATISTICS
    }

    @Data
    static class ContextConfig {
        @JSONField(name = "transform")
        TransformConfig transformConfig;

        @JSONField(name = "customize")
        Map<String, String> customize;
    }

    @Data
    static class TransformConfig {
        String packageExpression;
        String methodsExpression;
        String classExpression;
        String fieldsExpression;
    }

    @Data
    static class InputConfig {
        @JSONField(name = "classpath")
        List<String> classpath;

        @JSONField(name = "filter")
        FilterConfig filter;
    }

    @Data
    static class OutputConfig {
        boolean overwrite = false;
        String pathExpression;
        String fileNameExpression;
    }

    @Data
    static class RenderConfig {
        String templatePath;
        RenderEngine engine = RenderEngine.MUSTACHE;
    }

    static enum RenderEngine {
        /**
         * velocity
         */
        VELOCITY,

        /**
         * mustache
         */
        MUSTACHE;
    }

    @Data
    static class FilterConfig {
        boolean recursive = false;

        @JSONField(name = "isAnonymousClass")
        Boolean anonymousClass ;
        @JSONField(name = "isLocalClass")
        Boolean localClass;
        @JSONField(name = "isInterface")
        Boolean interfaceClass;
        @JSONField(name = "isPublic")
        Boolean publicClass;

        String targetPackage;
        String namePattern;
        String customize;
    }
}
