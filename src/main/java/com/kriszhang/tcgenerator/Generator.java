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
import java.io.Writer;
import java.util.Collection;

import com.google.common.base.Throwables;
import com.kriszhang.tcgenerator.ContextBuilder.Context;
import com.kriszhang.tcgenerator.ContextBuilder.GlobalContext;

/**
 * @author gongzuo.zy
 * @version $Id: TestCaseGenerater.java, v0.1 2017-06-02 17:43  gongzuo.zy Exp $
 */
public class Generator {

    private static void checkArguments(String[] args) {
        if (args == null || args.length <= 0) {
            System.out.println("USAGE: java -jar tcgenertor.jar /path/config.json");
            throw new GeneratorException("No Arguments Found");
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            checkArguments(args);

            Configuration.loadJsonConfig(args[0]);

            switch (Configuration.getConfig().getMode()) {
                case SCAN:
                    scan();break;
                case STATISTICS:
                    statistics();break;
                default:
                    throw new GeneratorException("[mode] of config should be SCAN or STATISTICS");
            }
        } catch(Exception ex) {
            handleException(ex);
        }
    }

    private static void handleException(Exception ex) {
        if (ex instanceof GeneratorException) {
            System.err.println("** ERROR: " + ex.getMessage() + " **");
        } else {
            Throwables.propagate(ex);
        }
    }

    private static void statistics() throws IOException {

        Collection<Class> classes = InputLoader.getInstance().loadClass();

        GlobalContext globalContext = ContextBuilder.getInstance().buildGlobalContext(classes);

        Writer writer = OutputWriter.createWriter(globalContext);

        if (writer == null) {
            return;
        }

        RenderEngine.getInstance().render(writer, globalContext);
    }

    private static void scan() throws IOException {
        if (Configuration.getConfig().isParallel()) {
            InputLoader.getInstance().loadClass().parallelStream().forEach(Generator::scan0);
        } else {
            InputLoader.getInstance().loadClass().forEach(Generator::scan0);
        }
    }

    private static void scan0(Class clazz)  {
        try {
            Context context = ContextBuilder.getInstance().buildContext(clazz);

            if (context == null || context.isEmpty()) {
                return;
            }

            Writer writer = OutputWriter.createWriter(context);

            if (writer == null) {
                return;
            }

            RenderEngine.getInstance().render(writer, context);

        } catch(Exception ex) {
            if (Configuration.getConfig().isSkipError()) {
                System.err.println(ex.getMessage());
            } else {
                Throwables.propagate(ex);
            }
        }
    }

}