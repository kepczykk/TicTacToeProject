/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Konrad
 */
public class Play extends HttpServlet {

    static List<PointXO> points = new ArrayList<>();
    static String ip1;
    static String ip2;
    static int flag = 0;
    static boolean playing = false;
    static boolean win = false;
    List<PointXO> wonPoints = new ArrayList<>();
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        if(points.isEmpty()){
            points.add(new PointXO());
        }
        
        if (request.getParameter("autoSolver") != null) {
            autoSolver();
        }else{
            if(playing){
                if(ip1 == null && ip2 == null){
                    ip1 = request.getParameter("ip1");
                    ip2 = request.getParameter("ip2");
                }
                if(flag == 0){
                    if(checkIfWin(addPoint(ip1, "O"))){
                        win = true;
                    }
                    flag = 1;
                }else{
                    if(checkIfWin(addPoint(ip2, "X"))){
                        win = true;
                    }
                    flag = 0;
                }
            }
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Play</title>");     
            out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() +  "/style.css' />");
            out.println("</head>");
            out.println("<body>");
            
            
            if(!playing){
                out.println("<form action='/TicTacToeServlet/Play' method='POST'>" +
                "Player 1:\n" +
                "<input type=\"TEXT\" name=\"ip1\"><br/>\n" +
                "Player 2:\n" +
                "<input type=\"TEXT\" name=\"ip2\"><br/>\n" +
                "<input type=\"SUBMIT\" value=\"Start\">" +
                "</form>");
                playing = true;
            }else{
                if(!win){
                    out.println("<a href='/TicTacToeServlet/Play'><button>Next move</button></a>");
                    out.println("<form action='/TicTacToeServlet/Play' method='post'>" +
                    "<input type='submit' name='autoSolver' value='autoSolver' />" +
                    "</form>");
                }
                out.println(getTable());
                
                if(win){
                    out.println("Player " + (!wonPoints.get(0).xo.equals("X") ? 1 : 2) + " wins!");
                    
                    points.clear();
                    flag = 0;
                    playing = false;
                    win = false;
                    wonPoints.clear();
                    reset(ip1);
                    reset(ip2);
                    ip1 = null;
                    ip2 = null;
                }
            }
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    private void autoSolver(){
        while(!win){
            if(flag == 0){
                if(checkIfWin(addPoint(ip1, "O"))){
                    win = true;
                }
                flag = 1;
            }else{
                if(checkIfWin(addPoint(ip2, "X"))){
                    win = true;
                }
                flag = 0;
            }
        }
    }
    
    private String getTable(){
        int maxY = 0;
        int minY = 0;
        int maxX = 0;
        int minX = 0;

        for(PointXO p : points){
            if(maxY<p.y){
                maxY = p.y;
            }
            if(minY>p.y){
                minY = p.y;
            }
            if(maxX<p.x){
                maxX = p.x;
            }
            if(minX>p.x){
                minX = p.x;
            }
        }
        String table = "<table>";
        for(int i=maxY; i>=minY; i--){
            table += "<tr>";
            for(int j=minX; j<=maxX; j++){
                PointXO p = new PointXO(j,i,"X");
                PointXO p2 = new PointXO(j,i,"O");
                if(wonPoints.contains(p) || wonPoints.contains(p2)){
                    table += "<td style=\"background-color:red;\">";
                }else if(p.equals(points.get(0))){
                    table += "<td style=\"background-color:green;\">";
                }else {
                    table += "<td>";
                }
                if(points.contains(p)){
                    table += p.xo;
                } else if(points.contains(p2)){
                    table += p2.xo;
                }
                table += "</td>";
            } 
            table += "</tr>";
        }
        table += "</table>";
        return table;
    }
    
    
    
    private PointXO addPoint(String ip, String xo){
        PointXO p = null;
        try {
            URL url = new URL("http://" + ip + points.get(points.size() - 1).x +"/"+ points.get(points.size() - 1).y);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String[] coords = br.readLine().split(" ");
            p = new PointXO(Integer.valueOf(coords[0]),Integer.valueOf(coords[1]), xo);
            points.add(p);
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
        }
        return p;
    }
    
    
    private void reset(String ip){
        try {
            URL url = new URL("http://" + ip + "reset");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private boolean checkIfWin(PointXO p){
        int cnt;
        int change;
        int i;
        PointXO point = new PointXO();
        for(int type=1;type<5;type++){
            i = 0;
            change = 0;
            cnt = 0;
            wonPoints.clear();
            while(i>-5 && i<5){
                switch(type){
                    case 1:
                        point = new PointXO(p.x,p.y+i,p.xo);
                        break;
                    case 2:
                        point = new PointXO(p.x+i,p.y,p.xo);
                        break;
                    case 3:
                        point = new PointXO(p.x+i,p.y+i,p.xo);
                        break;
                    case 4:
                        point = new PointXO(p.x+i,p.y-i,p.xo);
                        break;
                }
                if(points.contains(point)){
                    cnt++;
                    wonPoints.add(point);
                }else{
                    if(change == 1 && cnt != 5){
                        break;
                    }else{
                        change = 1;
                        i = 0;
                    }
                }
                if(change == 1){
                    i++;
                }else{
                    i--;
                }
                if(cnt == 5){
                    return true;
                }
            }
        }
        wonPoints.clear();
        return false;
    }
    
    public class PointXO{
        int x;
        int y;
        String xo;
        
        PointXO(){
            this.x = 0;
            this.y = 0;
            this.xo = "X";
        }
        
        PointXO(int x, int y, String xo){
            this.x = x;
            this.y = y;
            this.xo = xo;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof PointXO)) {
                return false;
            }
            PointXO other = (PointXO) object;
            if (this.x != other.x) {
                return false;
            }
            if (this.y != other.y) {
                return false;
            }
            if (!(this.xo).equals(other.xo)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.x;
            hash = 53 * hash + this.y;
            hash = 53 * hash + Objects.hashCode(this.xo);
            return hash;
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
