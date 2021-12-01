package aopTest.containerUtil;

import aopTest.ProxyFactory;
import aopTest.annotation.*;
import aopTest.util.FileUtil;
import aopTest.util.Util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AnalysisAnnotation {
    private List<String> classPaths;
    private List<Class> classes;
    private ClassLoader cl;
    private Map<String,Class> annotationMap;
    private Map<Class,Class> serviceMap;
    private Map<String,Class> nameServiceMap;
    private Map<String,Class> urlMap;
    private Map<Class,Class> proxyMap;
    private FileUtil util;
    private List<Class> service;

    private List<Class> controller;
    private Map<String,Class> aspects;

    private Map<String,Object> url2Controller;
    private Map<String,Method> url2Method;
    private Map<String,Class> url2ControllerClass;
    private ProxyFactory proxyFactory;

    public AnalysisAnnotation(List<String> classPath){
        this.classPaths = classPath;
        this.cl = AnalysisAnnotation.class.getClassLoader();
        this.init();
    }

    public AnalysisAnnotation(){
        this.cl = AnalysisAnnotation.class.getClassLoader();
        this.init();
    }

    public AnalysisAnnotation(List<String> classPath,ClassLoader cl){
        this.classPaths = classPath;
        this.cl = cl;
        this.init();
    }
    private void init(){
        this.annotationMap = new HashMap<>();
        this.serviceMap = new HashMap<>();
        this.urlMap = new HashMap<>();
        this.proxyMap = new HashMap<>();
        this.util = new FileUtil();
        this.classes = new ArrayList<>();
        this.url2Method = new HashMap<>();
        this.url2Controller = new HashMap<>();
        this.aspects = new HashMap<>();
        this.url2ControllerClass = new HashMap<>();
        this.controller = new ArrayList<>();
        this.service = new ArrayList<>();
        this.nameServiceMap = new HashMap<>();
    }

    /**
     * 分析项目路径下的所有类。
     * @param basePage 项目包路径 在这里的值应该是springSample
     */
    public void classification(String basePage){
        List<String> classNames = getClassNames(basePage);
        for(String className : classNames){
            Class clazz = this.loadClass(className);
            this.classes.add(clazz);
            // 筛选出Service类
            if(clazz.isAnnotationPresent(Service.class)){
//                List<Class> classes = new ArrayList<>(Arrays.asList(clazz.getInterfaces()));
//                this.service.addAll(classes);
                this.service.add(clazz);
            }
            // 筛选出增强类
            if(clazz.isAnnotationPresent(aspect.class)){
                aspect asp = (aspect) clazz.getAnnotation(aspect.class);
                String value = asp.value();
                this.annotationMap.put(value,clazz);
            }
            // 筛选出Controller类
            if(clazz.isAnnotationPresent(Controller.class)){
                this.controller.add(clazz);
            }
            if(clazz.isAnnotation()){
                String annotationName = clazz.getSimpleName();
                this.aspects.put(annotationName,clazz);
            }
        }
//        Set<String> aspectNames = this.aspects.keySet();
        // 获得增强注解和增强类的映射
//        for(Class clazz : this.classes){
//            List<Annotation> annotations = new ArrayList<>(Arrays.asList(clazz.getDeclaredAnnotations()));
//            for(Annotation annotation : annotations){
//                String annotationName = annotation.getClass().getSimpleName();
//                for(String aspectName : aspectNames){
//                    if(aspectName.equals(annotationName)){
//                        //表名clazz这个类引用了aspectName这个注解。
//                        List<Class> aspectClass = this.aspects.computeIfAbsent(aspectName, k -> new ArrayList<>());
//                        aspectClass.add(clazz);
//                    }
//                }
//            }
//        }
    }

    public void analysisController(){
        for(Class controller : this.controller){
            Field[] fields = controller.getDeclaredFields();
            Method[] methods = controller.getDeclaredMethods();
            for(Method method : methods){
                if(method.isAnnotationPresent(Mapping.class)){
                    Mapping mapping = method.getAnnotation(Mapping.class);
                    String url = mapping.value();
                    try{
                        this.url2Controller.put(url,controller.newInstance());
                        this.url2ControllerClass.put(url,controller);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                    method.setAccessible(true);
                    this.url2Method.put(url,method);
                }
            }
//            for(Field field : fields){
//                if(field.isAnnotationPresent(Autowrited.class)){
//                    //TODO 注入变量
//                }
//            }
        }
    }

    /**
     * 分析service
     */
    public void analysisService(){
//        for(Map.Entry<Class,Class> entry : this.serviceMap.entrySet()){
//            Class interfaceService = entry.getKey();
//            Class implService = entry.getValue();
//            this.serviceMap.put(interfaceService,implService);
//            Field[] fields = implService.getDeclaredFields();
//            for(Field field : fields){
//                if(field.isAnnotationPresent(Autowrited.class)){
//                    //TODO 自动注入
//                }
//            }
//        }
        for(Class clazz : this.service){
            List<Class> interfaceServices = Arrays.asList(clazz.getInterfaces());
            for(Class interfaceService : interfaceServices){
                this.serviceMap.put(interfaceService,clazz);
                String name = interfaceService.getName();
                this.nameServiceMap.put(name,clazz);
            }
        }
    }

    public void main(String basePage){
        this.classification(basePage);
        this.analysisController();
        this.analysisService();
        ProxyFactory.seteMap(annotationMap,serviceMap,aspects);
//        this.proxyFactory = new ProxyFactory();
    }

    public Map<String,Class> getAspects(){
        return this.aspects;
    }

    public Map<String,Class> getAnnotationMap(){
        return this.annotationMap;
    }

    public  Map<String,Class> getNameServiceMap(){
        return this.nameServiceMap;
    }

    public ProxyFactory getProxyFactory(){
        return this.proxyFactory;
    }

    public Map<String,Method> getUrl2Method(){
        return url2Method;
    }

    public Map<String,Object> getUrl2Controller(){
        return url2Controller;
    }

    public Map<String,Class> getUrl2ControllerClass(){
        return this.url2ControllerClass;
    }

    public Map<Class,Class> getServiceMap(){
        return this.serviceMap;
    }

    public Class loadClass(String classPath){
        Class clazz = null;
        try{
            clazz = this.cl.loadClass(classPath);
        } catch (ClassNotFoundException e) {
            System.out.println(String.format("未找到%s类",classPath));
        }
        return clazz;
    }

    /**
     * 筛选连接点中的切点
     * @param methods
     * @return
     */
    public List<Method> getPointCut(List<Method> methods){
        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();
            for(Annotation annotation : annotations){
                //遍历方法上的注解
                String anname = annotation.annotationType().getSimpleName();
                if(anname.equalsIgnoreCase("Before"))
                    continue;
                else if(anname.equalsIgnoreCase("After"))
                    continue;
            }
        }
        return null;
    }

    public Method getAdvice(Class clazz,boolean needBefore){
        List<Method> methods = Util.getMethods(clazz);
        Method before = null;
        Method after = null;
        for(Method method : methods){
            Annotation[] annotations = method.getAnnotations();
            for(Annotation annotation : annotations){
                //遍历方法上的注解
                String anname = annotation.annotationType().getSimpleName();
                if(anname.equalsIgnoreCase("Before")){
                    before = method;
                    break;
                }
                else if(anname.equalsIgnoreCase("After")){
                    after = method;
                    break;
                }
            }
            if(before != null && needBefore)
                return before;
            else if(after != null && !needBefore)
                return after;
        }
        return null;
    }

    public Method getBefore(Class clazz){
        return getAdvice(clazz,true);
    }

    public Method getAfter(Class clazz){
        return getAdvice(clazz,false);
    }

//    public void analysisClass(Class clazz){
//        boolean isAnn = clazz.isAnnotationPresent(aspect.class);
//        //如果有aspect注解，说明此类是通知类，也就是增强方法类
//        if(isAnn){
//            aspect asp = (aspect) clazz.getAnnotation(aspect.class);
//            String value = asp.value();
//            this.annotationMap.put(value,clazz);
//        }
//        else{
//            //得到所有连接点。
//            List<Method> methods = Util.getAllMethod(clazz);
//        }
//    }

    /**
     * 获得包下所有类的类名
     * @param basePage
     * @return
     */
    public List<String> getClassNames(String basePage){
        util.setBasePath(basePage);
        return util.getClassNames();
    }

//    public void analysisController(String basePage){
//        List<String> classNames = getClassNames(basePage);
//        //分析controller
//        for(String className : classNames){
//            Class clazz = this.loadClass(className);
//            clazz.isAnnotation();
//        }
//    }


    /**
     * 分析Service包，检索路径下所有类的路径并分析成类名。
     * 然后遍历类名集合，先确定Service、Advice
     * @param basePage
     */
    public void analysisService(String basePage){
        List<String> classNames = getClassNames(basePage);
        // 分析service包下的类。 构造<ServiceInterface,ServiceInterfaceImpl>的集合
        for(String className : classNames){
            Class service = this.loadClass(className);
            boolean isInter = service.isInterface();
            //如果是不是实现类的话。跳过。
            if(!isInter){
                List<Class> classes = new ArrayList<>(Arrays.asList(service.getInterfaces()));
                for(Class clazz : classes){
                    this.serviceMap.put(clazz,service);
                }
            }
        }
    }

}
