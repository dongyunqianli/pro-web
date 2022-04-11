package com.atguigu.myssm.myspringmvc;

import com.atguigu.myssm.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("*.do")
public class DispatcherServlet extends ViewBaseServlet{
    private Map<String ,Object> beanMap=new HashMap<>();
    public DispatcherServlet(){
    }
    public void init() throws ServletException {
        super.init();
        try {
            InputStream inputStream=getClass().getClassLoader().getResourceAsStream("applicationContext.xml");
            //1.创建DocumentBuilderFactory
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //2.创建DocumentBuilder对象
            DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();
            //3.创建Document对象
            Document document=documentBuilder.parse(inputStream);
            //4.获取所有的bean节点
            NodeList beanNodeList=document.getElementsByTagName("bean");
            for (int i = 0; i < beanNodeList.getLength(); i++) {
                Node beanNode=beanNodeList.item(i);
                if(beanNode.getNodeType()==Node.ELEMENT_NODE){
                    Element beanElement=(Element)beanNode;
                    String beanId=beanElement.getAttribute("id");
                    String className=beanElement.getAttribute("class");
                    Class controllerBeanClass=Class.forName(className);
                    Object beanObj=controllerBeanClass.newInstance();
                    beanMap.put(beanId,beanObj);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //1.设置编码
        request.setCharacterEncoding("utf-8");
        //http://localhost:8080/pro15/hello.do
        //-->servletPath是： /hello.do
        // 从 /hello.do --> hello
        //  -> HelloController
        String servletPath=request.getServletPath();
        servletPath=servletPath.substring(1);
        int lastDotIndex=servletPath.lastIndexOf(".do");
        servletPath=servletPath.substring(0,lastDotIndex);

        System.out.println("servletPath= "+servletPath);


        Object controllerBeanObj=beanMap.get(servletPath);


        System.out.println("controllerBeanObj= "+controllerBeanObj);


        String operate=request.getParameter("operate");

        System.out.println("operate= "+operate);

        if(StringUtil.isEmpty(operate)){
            operate="index";
        }
        try {
            Method[] methods=controllerBeanObj.getClass().getDeclaredMethods();
            for(Method method :methods){
                if(operate.equals(method.getName())){

                    System.out.println("method= "+method);
                    //1.同意获取请求参数
                    //1.1 获取当前方法的参数，返回参数数组
                    Parameter[] parameters=method.getParameters();
                    //1.2 parameterValues 用来承载参数的值
                    Object[] parameterValues=new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter parameter =parameters[i];
                        String parameterName = parameter.getName();
                        //如果是request，response，session 那么就不是通过请求中获取参数的方式了
                        if("request".equals(parameterName)){
                            parameterValues[i]=request;
                        }else if("response".equals(parameterName)){
                            parameterValues[i]=response;
                        }else if("session".equals(parameterName)){
                            parameterValues[i]=request.getSession();
                        }else{
                            //从请求中获取参数值
                            String parameterValue=request.getParameter(parameterName);
                            String typeName=parameter.getType().getName();

                            Object parameterObj=parameterValue;
                            if(parameterObj!=null){
                                if("java.lang.Integer".equals(typeName)){
                                    parameterObj=Integer.parseInt(parameterValue);
                                }
                            }
                            parameterValues[i]=parameterObj;
                        }
                    }
                    //2.controller组件中的方法调用
                    method.setAccessible(true);
                    Object returnObj = method.invoke(controllerBeanObj,parameterValues);
                    //3.视图处理
                    String methodReturnStr=(String) returnObj;
                    if(methodReturnStr.startsWith("redirect:")){ // 比如： redirect:fruit.do
                        String redirctStr=methodReturnStr.substring("redirect:".length());
                        response.sendRedirect(redirctStr);
                    }else{
                        super.processTemplate(methodReturnStr,request,response);// 比如： "edit"
                    }
                }
            }
//            }else{
//                throw new RuntimeException("operate 值非法");
//            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}


//    java.lang.IllegalArgumentException: argument type mismatch



















