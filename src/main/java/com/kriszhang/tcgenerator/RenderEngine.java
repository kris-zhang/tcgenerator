/*
 * (C) Copyright 2017 kriszhang (http://kriszhang.com/) and others.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Maps;
import com.kriszhang.tcgenerator.ContextBuilder.Context;
import com.kriszhang.tcgenerator.ContextBuilder.GlobalContext;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * @author gongzuo.zy
 * @version $Id: RenderManager.java, v0.1 2017-06-03 09:57  gongzuo.zy Exp $
 */
class RenderEngine {
    private final static RenderEngine INSTANCE = new RenderEngine();

    static RenderEngine getInstance() {
        return INSTANCE;
    }

    private RenderEngine() {}

    private static VelocityEngine ve;

    void render(Writer writer, Context context) throws IOException {
        render0(writer, context);
    }

    void render(Writer writer, GlobalContext globalContext) throws IOException {
        render0(writer, globalContext);
    }

    private void render0(Writer writer, Object context) throws IOException {
        switch (Configuration.getRenderConfig().getEngine()) {
            case VELOCITY:
                if (context instanceof GlobalContext) {
                    renderWithVelocity(writer, (GlobalContext)context);
                } else {
                    renderWithVelocity(writer, (Context)context);
                }
                break;
            case MUSTACHE:
                if (context instanceof GlobalContext) {
                    renderWithMustache(writer, (GlobalContext)context);
                } else {
                    renderWithMustache(writer, (Context)context);
                }
                break;
            default: throw new GeneratorException("Render Engine Not Support:"
                + Configuration.getRenderConfig().getEngine());
        }
    }

    private void renderWithVelocity(Writer writer, Context context) throws IOException {
        if (ve == null) {
            initVelocity();
        }

        Template t = ve.getTemplate(Configuration.getRenderConfig().getTemplatePath());
        VelocityContext ctx = new VelocityContext(context);
        t.merge(ctx, writer);
        writer.flush();
    }

    private void renderWithVelocity(Writer writer, GlobalContext globalContext) throws IOException {
        if (ve == null) {
            initVelocity();
        }

        Template t = ve.getTemplate(Configuration.getRenderConfig().getTemplatePath());
        VelocityContext ctx = new VelocityContext();
        ctx.put("contexts", globalContext.getContexts());
        t.merge(ctx, writer);
        writer.flush();
    }

    private void renderWithMustache(Writer writer, Context context) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(Configuration.getRenderConfig().getTemplatePath());
        mustache.execute(new PrintWriter(writer), context).flush();
    }

    private void renderWithMustache(Writer writer, GlobalContext globalContext) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(Configuration.getRenderConfig().getTemplatePath());
        Map<String, Object> context = Maps.newHashMap();
        context.put("contexts", globalContext.getContexts());
        mustache.execute(new PrintWriter(writer), context).flush();
    }

    private static void initVelocity() {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
    }
}
