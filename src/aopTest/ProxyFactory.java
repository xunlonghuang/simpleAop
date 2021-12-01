package aopTest;

import aopTest.util.Util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static sun.net.www.protocol.http.AuthCacheValue.Type.Proxy;

/**
 * 该代理类不仅仅要实现Service接口到Service实现类的代理。
 * 还要实现其他增强方法的代理。
 * 值得一提的是，级联代理。
 */
public class ProxyFactory implements InvocationHandler {
    //注解 -> 增强类
    private static Map<String,Class> annotationMap;
    //serviceInterface -> serviceImpl
    private static Map<Class,Class> serviceMap;
    private static Map<String,Class> aspects;
    private Object target;
    public ProxyFactory(Object object){
        this.target = object;
    }

    public static boolean isService(Class clazz){
        String className = clazz.getSimpleName();
        for(Map.Entry<Class,Class> entry : serviceMap.entrySet()){
            String serviceName = entry.getKey().getSimpleName();
            if(serviceName.equals(className))
                return true;
        }
        return false;
    }

    public static void seteMap(Map<String,Class> map,Map<Class,Class> serviceMap,Map<String,Class> aspects){
        ProxyFactory.annotationMap = map;
        ProxyFactory.serviceMap = serviceMap;
        ProxyFactory.aspects = aspects;
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
        Method before = getAdvice(clazz,true);
        before.setAccessible(true);
        return before;
    }

    public Method getAfter(Class clazz){
        Method after = getAdvice(clazz,false);
        after.setAccessible(true);
        return after;
    }

    private static Class getAdviceClass(Method method){
        for(Map.Entry<String,Class> entry : ProxyFactory.annotationMap.entrySet()){
            String annotationName = entry.getKey();
            Class clazz = aspects.get(annotationName);
            Annotation[] annotations = method.getDeclaredAnnotations();
            if(method.isAnnotationPresent(clazz)){
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class[] parame = method.getParameterTypes();
        Method realMethod = this.target.getClass().getDeclaredMethod(methodName,parame);
        Class adviceClass = getAdviceClass(realMethod);
        if(adviceClass == null){
            method.setAccessible(true);
            method.invoke(target,args);
        }
        else{
            //增强方法
            Method before = getBefore(adviceClass);
            Method after = getAfter(adviceClass);
            Object advice = adviceClass.newInstance();
            int paramCount = before.getParameterCount();
            if(paramCount != 0)
                before.invoke(advice,args);
            else
                before.invoke(advice);
            method.invoke(target,args);
            paramCount = after.getParameterCount();
            if(paramCount != 0)
                after.invoke(advice,args);
            else
                after.invoke(advice);
        }
        return null;
    }

}
