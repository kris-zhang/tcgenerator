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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.kriszhang.tcgenerator.Configuration.FilterConfig;

import static com.google.common.reflect.ClassPath.from;

/**
 * @author gongzuo.zy
 * @version $Id: ClassPathLoader.java, v0.1 2017-06-02 22:50  gongzuo.zy Exp $
 */
class InputLoader {

    private static final InputLoader INSTANCE = new InputLoader();

    static InputLoader getInstance() {
        return INSTANCE;
    }

    private InputLoader() {}

    Collection<Class> loadClass() throws IOException {
        List<String> classPaths = Configuration.getInputConfig().getClasspath();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        if (classPaths != null && !classPaths.isEmpty()) {
            List<URL> urls = Lists.newArrayList();
            classPaths.forEach(p-> urls.addAll(loadJars(p)));
            classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        }

        FilterConfig filter = Configuration.getFilterConfig();
        Collection<ClassInfo> classInfo;

        if (filter.isRecursive()) {
            classInfo = from(classLoader).getTopLevelClassesRecursive(filter.getTargetPackage());
        } else {
            classInfo = from(classLoader).getTopLevelClasses(filter.getTargetPackage());
        }

        if (classInfo == null || classInfo.isEmpty()) {
            throw new GeneratorException("Load Package Error: " + filter.getTargetPackage());
        }

        return InputFilter.getInstance().filter(classInfo);
    }

    private List<URL> loadJars(String path) {
        List<URL> urls = Lists.newArrayList();

        try {
            Files.walk(Paths.get(path))
                .filter(java.nio.file.Files::isRegularFile)
                .filter(p->p.getFileName().toString().lastIndexOf(".jar") != -1)
                .forEach(p-> {
                    try {
                        urls.add(p.toUri().toURL());
                    } catch (MalformedURLException e) {
                        System.err.println(e.getMessage());
                    }
                });
            return urls;
        } catch(Exception ex) {
            throw new GeneratorException(ex);
        }
    }

}
