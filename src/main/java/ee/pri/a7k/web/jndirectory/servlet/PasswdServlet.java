/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.pri.a7k.web.jndirectory.servlet;

import ee.pri.a7k.web.jndirectory.ldap.LdapCtxClient;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author andrus
 */
public class PasswdServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public String getServletInfo() {
        return getClass().getCanonicalName();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        String uid = parseUid(request.getParameterValues("uid"));
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty username");
        }

        String pass = parsePass(request.getParameterValues("pass"));
        if (pass == null || pass.trim().isEmpty()) {
            throw new IllegalArgumentException("Could not authenticate user '" + uid + "'");
        }

        String newpass = parseNewPasswdParams(request.getParameterValues("newpass"));
        if (newpass == null || newpass.trim().isEmpty()) {
            throw new IllegalArgumentException("New passwords don't match or are empty");
        }

        try {
            updateUserPassword(uid, pass, newpass);
            writeResponse(response, request, uid);
        } catch (IllegalArgumentException iae) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, iae.getMessage());
        } catch (RuntimeException re) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, re.getMessage());
        }

    }

    private void writeResponse(HttpServletResponse response, HttpServletRequest request, String uid) throws IOException {
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PasswdServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>New password set for uid=" + uid + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }

    }

    private void updateUserPassword(String uid, String pass, String newpass) {

        try {

            LdapCtxClient lctx = new LdapCtxClient();

            String uidDn = lctx.lookupUidDn(uid, pass);

            if (uidDn != null) {
            } else {
                Logger.getLogger(PasswdServlet.class.getName()).log(Level.INFO, "DN not found for uid={0}", uid);
                throw new IllegalArgumentException("Could not authenticate user '" + uid + "'");
            }

            lctx.updateLdapPassword(uidDn, newpass);

            lctx.close();

        } catch (NamingException ex) {
            Logger.getLogger(PasswdServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Internal server error", ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PasswdServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Internal server error", ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PasswdServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Internal server error", ex);
        } catch (IOException ex) {
            Logger.getLogger(PasswdServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Internal server error", ex);
        }

    }

    //Parameters
    private String parseUid(String[] _uid) {
        String uid = null;
        if (_uid.length == 1) {
            uid = _uid[0];
        }
        return uid;
    }

    private String parsePass(String[] _pass) {
        return parseUid(_pass);
    }

    private String parseNewPasswdParams(String[] _newpass) {
        String newpass = null;
        Set<String> set = new HashSet<String>();
        if (_newpass.length == 2) {
            set.addAll(Arrays.asList(_newpass));
        }
        if (set.size() == 1) {
            newpass = (String) set.toArray()[0];
        } else {
            Logger.getLogger(PasswdServlet.class.getName()).log(Level.FINE, "passwords don't match");
        }
        return newpass;
    }
}
