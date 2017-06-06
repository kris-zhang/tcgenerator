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

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author richardzhang
 * @version : SpringExpressionEngine.java, v0.1 2017-05-05 13:40  richardzhang Exp $
 */
class ScriptEngine {

    private final static ScriptEngine INSTANCE = new ScriptEngine();

    public static ScriptEngine getInstance() {
        return INSTANCE;
    }

    private ScriptEngine() {}

    private final SpelExpressionParser    parser        = new SpelExpressionParser();

    private final Map<Long, Expression> expressionMap = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    <T> T eval(String script, Object context) {
        long expressionId = Hashing.md5().hashUnencodedChars(script).asLong();

        StandardEvaluationContext springContext = new StandardEvaluationContext(context);

        if (!expressionMap.containsKey(expressionId)) {
            Expression expression = parser.parseExpression(script);
            expressionMap.put(expressionId, expression);
        }

        Expression expression = expressionMap.get(expressionId);
        return (T) expression.getValue(springContext);
    }

}
