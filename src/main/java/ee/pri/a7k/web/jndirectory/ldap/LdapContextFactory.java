/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.pri.a7k.web.jndirectory.ldap;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapContextFactory {

    private static final Hashtable<String, String> env = new Hashtable<String, String>();

    static {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:389/dc=teepub");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=admin,dc=teepub");
        env.put(Context.SECURITY_CREDENTIALS, "admin");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
    }

    protected static LdapContext getContext() throws NamingException {
        try {
            return new InitialLdapContext();
        } catch (NamingException ex) {
            Logger.getLogger(LdapContextFactory.class.getName()).log(Level.SEVERE, "Failed creating context", ex);
            throw ex;
        }
    }
}
