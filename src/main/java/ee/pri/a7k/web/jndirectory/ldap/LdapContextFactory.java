/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.pri.a7k.web.jndirectory.ldap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapContextFactory {

    private final Properties env = new Properties();
    private static LdapContextFactory ldapCtxFactory = null;

    private LdapContextFactory() throws IOException {

        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("jndi.properties");
            env.load(resourceAsStream);

        } catch (IOException ex) {
            Logger.getLogger(LdapContextFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private LdapContext get() throws NamingException {
        final InitialLdapContext initialDirContext = new InitialLdapContext(env, null);
        if (initialDirContext instanceof LdapContext) {
            return (LdapContext) initialDirContext;
        }
        return null;
    }

    public static LdapContext getContext() throws NamingException, IOException {
        if (ldapCtxFactory == null) {
            ldapCtxFactory = new LdapContextFactory();
        }
        return ldapCtxFactory.get();
    }
}
