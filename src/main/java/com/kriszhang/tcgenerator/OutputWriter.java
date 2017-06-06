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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.google.common.io.Files;

/**
 * @author gongzuo.zy
 * @version $Id: OutputWriter.java, v0.1 2017-06-03 10:17  gongzuo.zy Exp $
 */
class OutputWriter {

    static Writer createWriter(Object context) throws IOException {
        String filePath = resolveFullPath(context);

        File file = new File(filePath);

        if (file.exists()) {
            if (Configuration.getOutputConfig().isOverwrite()) {
                if (!file.delete()) {
                    throw new GeneratorException("Delete File Error：" + filePath);
                }
                System.out.println("Overwrite " + filePath);
            } else {
                System.out.println("Skip " + filePath);
                return null;
            }
        } else {
            System.out.println("Create " + filePath);
        }

        Files.createParentDirs(file);

        if (!file.createNewFile()) {
            throw new GeneratorException("Create File Error：" + filePath);
        }

        return new FileWriter(file);
    }

    private static String resolveFullPath(Object context) {
        String fileName = ScriptEngine.getInstance().eval(Configuration.getOutputConfig().getFileNameExpression(), context);
        String pathName = ScriptEngine.getInstance().eval(Configuration.getOutputConfig().getPathExpression(), context);

        if (fileName.lastIndexOf(File.separator) == fileName.length() - 1) {
            return pathName + fileName;
        } else {
            return pathName + File.separator + fileName;
        }
    }

}
