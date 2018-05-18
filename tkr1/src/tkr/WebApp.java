package tkr;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class WebApp
 */
//@WebServlet("/WebApp")
@WebServlet("/")
public class WebApp extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebApp() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// GitHubへの登録
        //Query Stringからパラメータを取得
        String strQueryData01 = request.getParameter("Data01");

        //JavaBeanオブジェクトの変数宣言
        JavaBean javaBean = null;

        //SessionオブジェクトからJavaBean取得。無ければ生成。
        HttpSession session = request.getSession();
        javaBean = (JavaBean)session.getAttribute("sessionObject_JavaBean01");
        if (javaBean == null){
            javaBean = new JavaBean();
            session.setAttribute("sessionObject_JavaBean01", javaBean);
        }

        //JavaBeanに、パラメータとして受け取ったデータをセット
        if (strQueryData01 != null) {
            javaBean.setStrData(strQueryData01);
        }

        
        try {
            //オブジェクトを取得
            InetAddress ia = InetAddress.getLocalHost();
            String ip = ia.getHostAddress();       //IPアドレス
            String hostname = ia.getHostName();    //ホスト名
            request.setAttribute("IPaddr", ip);
            request.setAttribute("Hostname", hostname);
            
            //画面表示
            System.out.println("IPaddr: " + ip);
            System.out.println("Hostname: " + hostname);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //JSP呼び出し
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("top.jsp");
        requestDispatcher.forward(request, response);		
				
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
