# tcgenerator

一个代码生成小工具，可以用来生成测试用例框架。实现基本原理是，通过指定class_path和filter，加载需要处理的类，然后通过反射拿到相关信息，在通过模板引擎将其渲染出来，之后输出文件。`input->context->context.tranform->context.customize->render->output`

## 基本模块

主要由以下几个模块组成：

1. input，输入模块，用来指定需要处理的类的包路径以及classpath，同时可以指定过滤方式
2. context，上下文，将类信息抽取录入到上下文中，作为SpEL和模板引擎的上下文
3. render，渲染引擎，可指定不同渲染引擎，如velocity、mustache等
4. output，将处理结果输出文件

## 如何开始

> java -jar tcgenerator.jar config.json

config与tcgenerator.jar([点击下载](https://github.com/kris-zhang/tcgenerator/files/1051049/tcgenerator.jar.zip))在同一个路径下。

## 配置解释

如下是一个配置实例：

```json
{
  "skipError": false,
  "parallel": false,
  "mode": "SCAN",
  "input": { 
    "classpath":[
      "/Users/gongzuo.zy/alibaba/git/mobilecommunity/target/mobilecommunity.ace/",
      "/Users/gongzuo.zy/etc/ce/"
    ],
    "filter": {
      "targetPackage" : "com.kriszhang.tcgenerator",
      "recursive" : true,
      "isPublic" : true,
      "isLocalClass" : false,
      "isAnonymousClass" : false,
      "isInterface" : false,
      "namePattern": "^String.*$",
      "customize":"true"
    }
  },
  "context" : {
    "transform" : {
      "classExpression" : "setName(name+'_test')",
      "packageExpression" : "setName(name+'_test')",
      "methodsExpression": "setName(name+'_test')",
      "fieldsExpression": "setName(name+'_test')"
    },
    "customize": {
      "k1": "'v1'",
      "k2": "'v2'"
    }
  },
  "render": {
    "templatePath" : "scan_template.vm",
    "engine" : "VELOCITY"
  },
  "output": {
    "pathExpression" : "'/Users/gongzuo.zy/Desktop/tmp'",
    "overwrite": true,
    "fileNameExpression" : "get('classModel').simpleName+'.txt'"
  }
}
```

配置解释如下：

模块 | 配置 | 解释
--- | --- | ---
global | skipError | 在SCAN模式下生效，如果有一个文件处理失败则跳过(true)否则直接退出(false)
global | parallel | 在SCAN模式下生效，用来多线程处理
global | mode | 分为SCAN模式和STATISTICS模式，扫描模式是扫描每一个类，然后处理再迭代输出；统计模式则只渲染模板一次，输出一次。模式的上下文不同
input | classpath | classpath路径，可指定多个，在路径下的所有jar都会加载到系统
input.filter | targetPackage | 需要处理的包名字
input.filter | recursive | 是否递归的扫描targetPackage
input.filter | isPublic | 是否是public class
input.filter | isLocalClass | 是否是 local calss
input.filter | isAnonymousClass | 是否是匿名类
input.filter | isInterface | 是否是接口
input.filter | namePattern | 类名字的pattern正则匹配
input.filter | customize | 用户自定义SpEL
context | transform.classExpression | 类模型变换SpEL
context | transform.packageExpression | 包模型变换SpEL
context | transform.methodsExpression | 方法模型变换SpEL
context | transform.fieldsExpression| 字段模型变换SpEL
context | customize| 自定义k-v v为SpEL表达式
render | engine | VELOCITY or MUSTACHE
render | templatePath | 模板引擎路径
output| pathExpression | 输出路径的SpEL
output | overwrite | 如果存在，是否覆盖
output | fileNameExpression | 文件名SpEL

## 使用示例

本实例将生成`com.alipay.xxx.biz.admin.service`包下所有类的public方法的测试用例junit框架。

配置如下：

```json
{
  "input": {
    "classpath":[
      "/Users/gongzuo.zy/alibaba/git/xxx/target/xxx.ace/",
      "/Users/gongzuo.zy/etc/ce/"
    ],
    "filter": {
      "targetPackage" : "com.alipay.xxx.biz.admin.service",
      "recursive" : false, 
      "isPublic" : true,
      "isLocalClass" : false,
      "isAnonymousClass" : false,
      "isInterface" : false,
    }
  },
  "context" : {
    "customize": {
      "newPackageName": "get('packageModel').name.replace('com.alipay.xxx','com.alipay.xxx.test')",
      "newClassName": "get('classModel').simpleName + 'Test'"
    }
  },
  "render": {
    "templatePath" : "test_template.mustache",
  },
  "output": {
    "pathExpression" : "'/Users/gongzuo.zy/alibaba/git/xxx/app/test/src/test/java/'+get('newPackageName').replace('.', T(java.io.File).separator)",
    "fileNameExpression" : "get('newClassName') + '.java'",
    "overwrite":false
  },
  "skipError":true
}

```

模板如下：

```java

package {{newPackageName}};

import {{classModel.name}};
import static org.assertj.core.api.Assertions.*;
import static com.alipay.xxx.test.TestConstants.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * test of {@link {{classModel.name}}}
 *
 * @author gongzuo.zy
 * @version $Id: {{newClassName}}.java, v0.1 {{yyyy}}-{{MM}}-{{dd}} {{HH}}:{{mm}}:{{ss}} gongzuo.zy Exp $
 */
public class {{newClassName}} {
 
    @Before
    public void init() {

    }

    @After
    public void destroy() {
        
    }

    {{#methodModels}}
    {{#isPublic}}
    /**
     * test of {{name}}
     */
    @Test
    public void test_{{name}}{{overrideIndex}}() {
    }
    
    {{/isPublic}}
    {{/methodModels}}

}

```

> java -jar tcgenerator.jar config.json

## 上下文

### 类信息上下文

key: classModel

```java
{
  "name":"com.xxx.xx.XxxYyy",
  "simpleName":"XxxYyy",
  "annotations":[
    {
      "name":"NotNull"
    }
  ],
  "isPublic":true,
  "isProtected":false,
  "isStatic":false,
  "isFinal":false,
  "isSynchronized":false,
  "isVolatile":false,
  "isTransient":false,
  "isNative":false,
  "isInterface":false,
  "isAbstract":false,
  "isStrict":false
}
```

### 方法信息上下文

key: methodModels

```json
{
  "name":"put",
  "annotations":[
    {
      "name":"NotNull"
    }
  ],
  "retType": {
    "name":"java.lang.String",
    "simpleName":"String"
  },
  "paramTypes": [
    {
      "name":"java.lang.String",
      "simpleName":"String"
    }
  ],
  "isPublic":true,
  "isProtected":false,
  "isStatic":false,
  "isFinal":false,
  "isSynchronized":false,
  "isVolatile":false,
  "isTransient":false,
  "isNative":false,
  "isInterface":false,
  "isAbstract":false,
  "isStrict":false
}
```

### 包信息上下文

key:packageModel

```
{
  "name":"com.xxx.xx"
}

```

### 字段信息上下文

key:fieldModels

```json
{
  "name":"myField",
  "annotations":[
    {
      "name":"NotNull"
    }
  ],
  "type": {
    "name":"java.lang.String",
    "simpleName":"String"
  },
  "isPublic":true,
  "isProtected":false,
  "isStatic":false,
  "isFinal":false,
  "isSynchronized":false,
  "isVolatile":false,
  "isTransient":false,
  "isNative":false,
  "isInterface":false,
  "isAbstract":false,
  "isStrict":false

}
```

### 时间上下文

无key，直接在root下
 
```json
{
  "yyyy":"2017",
  "month":"03",
  "dd":"02",
  "HH":"23",
  "mm":"14",
  "ss":"59"
}
```

### root上下文

上下文分为两种，一种是SpEL的上下文，一种是模板引擎的上下文。在不同的模式下，上下文root都不一样。

扫描模式下：

全局上下文为：

```json
{
  "classModel": {
    "name": "com.xxx.xx.XxxYyy",
    "simpleName": "XxxYyy",
    "annotations": [
      {
        "name": "NotNull"
      }
    ],
    "isPublic": true,
    "isProtected": false,
    "isStatic": false,
    "isFinal": false,
    "isSynchronized": false,
    "isVolatile": false,
    "isTransient": false,
    "isNative": false,
    "isInterface": false,
    "isAbstract": false,
    "isStrict": false
  },
  "methodModels": {
    "name": "put",
    "annotations": [
      {
        "name": "NotNull"
      }
    ],
    "retType": {
      "name": "java.lang.String",
      "simpleName": "String"
    },
    "paramTypes": [
      {
        "name": "java.lang.String",
        "simpleName": "String"
      }
    ],
    "isPublic": true,
    "isProtected": false,
    "isStatic": false,
    "isFinal": false,
    "isSynchronized": false,
    "isVolatile": false,
    "isTransient": false,
    "isNative": false,
    "isInterface": false,
    "isAbstract": false,
    "isStrict": false
  },
  "packageModel": {
    "name": "com.xxx.xx"
  },
  "fieldModels": {
    "name": "myField",
    "annotations": [
      {
        "name": "NotNull"
      }
    ],
    "type": {
      "name": "java.lang.String",
      "simpleName": "String"
    },
    "isPublic": true,
    "isProtected": false,
    "isStatic": false,
    "isFinal": false,
    "isSynchronized": false,
    "isVolatile": false,
    "isTransient": false,
    "isNative": false,
    "isInterface": false,
    "isAbstract": false,
    "isStrict": false
  },
  "yyyy": "2017",
  "month": "03",
  "dd": "02",
  "HH": "23",
  "mm": "14",
  "ss": "59"
}
```

- 每个模板的上下文则就是全局上下文。
- customize配置：全局上下文作为root
- transform.classExpression: classModel作为root，直接引用属性
- transform.packageExpression: packageModel作为root，直接应用属性
- transform.fieldsExpression: fieldModels作为root，直接引用属性
- transform.methodsExpression: methodModels作为root，直接引用属性
- output.pathExpression: 全局上下文作为root，作为map，需要get('')方法
- output.fileNameExpression: 全局上下文作为root，作为map，需要get('')方法

统计模式下

全局上下文为：

```json
{
  "contexts": [
    {
      "classModel": {
        "name": "com.xxx.xx.XxxYyy",
        "simpleName": "XxxYyy",
        "annotations": [
          {
            "name": "NotNull"
          }
        ],
        "isPublic": true,
        "isProtected": false,
        "isStatic": false,
        "isFinal": false,
        "isSynchronized": false,
        "isVolatile": false,
        "isTransient": false,
        "isNative": false,
        "isInterface": false,
        "isAbstract": false,
        "isStrict": false
      },
      "methodModels": {
        "name": "put",
        "annotations": [
          {
            "name": "NotNull"
          }
        ],
        "retType": {
          "name": "java.lang.String",
          "simpleName": "String"
        },
        "paramTypes": [
          {
            "name": "java.lang.String",
            "simpleName": "String"
          }
        ],
        "isPublic": true,
        "isProtected": false,
        "isStatic": false,
        "isFinal": false,
        "isSynchronized": false,
        "isVolatile": false,
        "isTransient": false,
        "isNative": false,
        "isInterface": false,
        "isAbstract": false,
        "isStrict": false
      },
      "packageModel": {
        "name": "com.xxx.xx"
      },
      "fieldModels": {
        "name": "myField",
        "annotations": [
          {
            "name": "NotNull"
          }
        ],
        "type": {
          "name": "java.lang.String",
          "simpleName": "String"
        },
        "isPublic": true,
        "isProtected": false,
        "isStatic": false,
        "isFinal": false,
        "isSynchronized": false,
        "isVolatile": false,
        "isTransient": false,
        "isNative": false,
        "isInterface": false,
        "isAbstract": false,
        "isStrict": false
      },
      "yyyy": "2017",
      "month": "03",
      "dd": "02",
      "HH": "23",
      "mm": "14",
      "ss": "59"
    }
  ]
}

```

- 每个模板的上下文则就是全局上下文。
- customize配置：扫描模式下的全局上下文
- transform.classExpression: classModel作为root，直接引用属性
- transform.packageExpression: packageModel作为root，直接引用属性
- transform.fieldsExpression: fieldModels作为root，直接引用属性
- transform.methodsExpression: methodModels作为root，直接引用属性
- output.pathExpression: 全局上下文作为root，是一个列表
- output.fileNameExpression: 全局上下文作为root，是一个列表
