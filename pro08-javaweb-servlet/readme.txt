1.设置编码
       post：设置编码，防止中文乱码,必须在所有的获取参数动作之前(基于tomcat8)
            request.setCharacterEncoding("UTF-8");
       get：目前不需要设置编码(基于tomcat8)
2.Servlet的继承关系-->重点查看的是（service，服务方法）
  1）继承关系
    javax.servlet.Servlet接口
        javax.servlet.GenericServlet抽象类
            javax.servlet.http.HttpServlet抽象子类
  2）相关方法
    javax.servlet.Servlet接口
        void init(config)--初始化方法
        void service(request,response)--服务方法
        void destroy()--销毁方法
    javax.servlet.GenericServlet抽象类
        void service(request,response)--仍然是抽象方法
    javax.servlet.http.HttpServlet抽象子类
        void service(request,response)--不是抽象方法
        1.String method = req.getMethod();获取请求的方式
        2.各种if判断，根据请求方式的不同，决定去调用不同的do方法
            if (method.equals("GET")) {
                this.doPost(req, resp);
            } else if (method.equals("HEAD")) {
                lastModified = this.getLastModified(req);
                this.maybeSetLastModified(resp, lastModified);
                this.doHead(req, resp);
            } else if (method.equals("POST")) {
                this.doPost(req, resp);
            } else if (method.equals("PUT")) {
                this.doPut(req, resp);
            }
        3.在HttpServlet这个抽象类中，do方法都差不多
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String protocol = req.getProtocol();
            String msg = lStrings.getString("http.method_get_not_supported");
            if (protocol.endsWith("1.1")) {
                resp.sendError(405, msg);
            } else {
                resp.sendError(400, msg);
            }

        }
  3）小结：
  *1* 继承关系：HttpServlet-> GenericServlet->Servlet
  *2* Servlet中的核心方法: init(), service(), destroy()
  *3* service()（服务方法）:当有请求过来时，service方法会自动响应，（其实是tomcat容器调用的）
        在HttpServlet中我们会去分析请求的方式，到底是get,post,head,还是delete
        然后再决定调用的时哪个do开头的方法
        在HttpServlet中这些do方法默认都是405的实现风格，要我们子类去实现相应的方法，否则会报405错误
  *4* 因此，我们在新建Servlet时，才会去考虑请求方法，从而决定重写哪个do方法

3.Servlet的生命周期
  1)生命周期对应Servlet中的三个方法：init(),service(),destroy()
  2)默认情况下：
     *第一次接受请求时，这个Servlet会进行实例化(调用构造方法)，初始化（调用init()），然后服务(调用service())
     *第二次请求开始，每一次都是服务service......
     *当容器关闭时，其中所有的servlet实例会被销毁，调用销毁方法

  3）
     *Servlet实例tomcat只会创建一个，所有的请求都是这个实例去响应。
     *默认情况下：第一次请求时，tomcat才会去实例化，初始化，然后再服务
        -->提高系统的启动速度，
        -->第一次请求时，耗时较长
     结论：如果需要提高系统的启动速度，（当前默认情况）
            如果需要提高响应速度，应该设置Servlet的响应时机
  4）Servlet的初始化时机:
     *默认是第一次接收请求时，实例化，初始化
     *我们可以通过<load-on-startup>来设置servlet启动的先后顺序，数字越小，启动越靠前，最小值0
  5）Servlet在容器中是：单例的，线程不安全的
     *单例：所有的请求都是同一个实例去响应
     *线程不安全：一个线程需要根据这个实例中的某个成员变量值去做逻辑判断。但是在中间某个时机，另一个线程改变了这个成员变量的值，从而导致第一个线程的执行路径发生了变化。
     *我们已经知道了servlet是线程不安全的，启发-->尽量不要在servlet中定义成员变量，那么（I）不要改变该成员变量的值 （II）根据该成员变量的值，做一些逻辑判断

4.Http协议。(Hyper Text Transfer Protocol,超文本传输协议）无状态的.-->服务器无法区分，这两次请求是同一个客户端发过来的，还是不同的客户端发过来的
    1）请求: 请求行，请求消息头，请求主体
        （1）请求行：*1*请求方式，*2*请求的url，*3*请求的协议（http1.1）
        （2）请求消息头：（host:服务器的主机类型），（Accept:声明当前请求能够接受的媒体类型），（Referer：当前请求来源页面的地址），（Content-Length：请求体内容长度）
        （3）请求主体：*get方式，没有请求体，有queryString
                    *post方式，有请求体，form data
                    *json格式，有请求体，request payload
    2）响应:响应行，响应头，响应体
        （1）响应行：*1*协议 *2*响应状态码（200） *3*响应状态（ok）
        （2）响应头：（Content-Length：内容长度）
        （3）响应体：相应的实际内容（比如请求add.html页面时，相应的内容就是<html><head><body><form...>）

5.会话
    1)会话跟踪技术
        --客户端第一次发请求给服务器，服务器获取session，获取不到，创建新的，然后相应给用户
        --下次客户端给服务器发请求时，会把sessionID带给服务器，那么服务器就能判断和上一次
        --常用的api:
            request.getSession()-->获取当前会话，没有则创建一个新的会话
            request.getSession(true)-->效果和不带参数相同
            request.getSession(false)-->获取当前会话，没有则返回null，不会创建新的

            session.getId()-->获取sessionID
            session.isNew()-->判断当前session是否是 新的
            session.getMaxInactiveInterval()-->session的非激活间隔时长,默认1800秒
            session.setMaxInactiveInterval()
            session.invalidate();-->强制性让会话立即失效
    2）session保存作用域
        --session保存作用域是和一个具体的某一个session对应的
        --常用的api：
            void session.setAttribute(k,v)
            Object session.getAttribute(k)
            void removeAttribute(k)

6.服务器内部转发以及客户端重定向
    1）服务器内部转发：request.getRequestDispatcher("......").forward(request,response);
        *一次请求响应的过程，对于客户端而言，内部经过了多少次转发，客户端是不知道的
        *地址栏没有变化
    2）客户端重定向：response.sendRedirect("......");
        *两次请求响应的过程。客户端肯定知道请求url有变化
        *地址栏有变化

7.Thymeleaf--视图模板技术
    1)添加thymeleaf的jar包
    2)新建一个Servlet类ViewBaseServlet
    3）在web.xml文件中添加配置
        -配置视图前缀view-prefix
        -配置视图后缀view-suffix
    4)使得我们的Servlet继承ViewBaseServlet

    5)        //此处视图的名称是 index
              //那么thymeleaf会将这个 逻辑视图名称 对应到 物理视图 名称上去
              //逻辑视图名称:index
              //物理视图名称： view-prefix + 逻辑视图名称 + view-suffix
              //所以真实的视图名称是 / index .html
              super.processTemplate("index",request,response);

    6)使用thymeleaf的标签
    th:if , th:unless , th:each , th:text


//200：正常响应
//404：找不到资源
//405：请求方式不支持
//500： 服务器内部错误