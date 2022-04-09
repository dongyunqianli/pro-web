package com.atguigu.servlets;

import com.atguigu.fruit.dao.FruitDAO;
import com.atguigu.fruit.dao.impl.FruitDAOImpl;
import com.atguigu.fruit.pojo.Fruit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddServlets extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //post方式下：设置编码，防止中文乱码,必须在所有的获取参数动作之前
        //get方式，目前不需要设置编码(基于tomcat8)
        request.setCharacterEncoding("UTF-8");
        String fname = request.getParameter("fname");

        String priceStr = request.getParameter("price");
        Integer price = Integer.parseInt(priceStr);

        String fcountStr = request.getParameter("fcount");
        int fcount = Integer.parseInt(fcountStr);

        String remark = request.getParameter("remark");

        FruitDAO fruitDAO=new FruitDAOImpl();
        boolean flag = fruitDAO.addFruit(new Fruit(0, fname, price, fcount, remark));
        System.out.println(flag?"添加成功":"添加失败");


    }
}
