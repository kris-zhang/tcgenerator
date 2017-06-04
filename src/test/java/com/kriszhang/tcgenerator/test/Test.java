/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package com.kriszhang.tcgenerator.test;

import java.io.IOException;

import com.kriszhang.tcgenerator.Generator;

/**
 * @author gongzuo.zy
 * @version $Id: Test.java, v0.1 2017-06-04 11:14  gongzuo.zy Exp $
 */
public class Test {

    public static void main(String[] args) throws IOException {
        String[] a = new String[] {"/Users/gongzuo.zy/Desktop/test/scan_template_config.json"};
        //String[] a = new String[] {"scan_template_config.json"};

        Generator.main(a);
    }
}
