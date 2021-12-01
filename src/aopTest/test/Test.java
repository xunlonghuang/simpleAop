package aopTest.test;

import aopTest.annotation.aspect;
import aopTest.util.FileUtil;
import aopTest.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;

public class Test {

    private void annotationTest(){
        ClassLoader cl = Test.class.getClassLoader();
        try{
            Class adviceTest = cl.loadClass("aopTest.Advice.AdviceTest");
            Annotation[] annotations = adviceTest.getAnnotations();
            for(Annotation annotation : annotations){
                System.out.println(annotation.toString());
                System.out.println(annotation.annotationType().getSimpleName());
                aspect asp = (aspect) annotation;
                System.out.println(asp.value());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void fileProcessTest(){
        Util util = new Util();
        String projectPath = util.getProjectPath();
        System.out.println(projectPath);
        System.out.println(File.separator);
        String basePage = "src.aopTest";
    }

    public static void main(String[] args){
        Test test = new Test();
//        test.fileProcessTest();
        FileUtil fileUtil = new FileUtil("aopTest");
        fileUtil.getClassNames();
    }
}
