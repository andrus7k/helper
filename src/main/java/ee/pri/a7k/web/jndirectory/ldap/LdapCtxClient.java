/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.pri.a7k.web.jndirectory.ldap;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author andrus
 */
public class LdapCtxClient {

    private final static Logger LOGGER = Logger.getLogger(LdapCtxClient.class.getName());
    private final LdapContext lctx;

    public LdapCtxClient() throws NamingException {
        lctx = LdapContextFactory.getContext();
    }

    public String lookupUidDn(String uid, String pass) throws NoSuchAlgorithmException, UnsupportedEncodingException, NamingException {
        String uidDn = null;

        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] attributeFilter = {"cn"};
        sc.setReturningAttributes(attributeFilter);

        NamingEnumeration results = lctx.search("ou=People",
                "(&(objectClass=uidObject)(objectClass=sipleSecurityObject)(uid=" + uid + ")(userPassword=" + hashPassword(pass) + "))", sc);

        while (results.hasMore()) {
            SearchResult sr = (SearchResult) results.next();
            if (uidDn == null) {
                uidDn = sr.getName() + ",ou=People";
            } else {
                LOGGER.log(Level.SEVERE, "Error getting multiple dns for uid: {0}", uid);
                break;
            }

        }

        return uidDn;
    }

    public void updateLdapPassword(String uidDn, String newpass) throws IllegalArgumentException, UnsupportedEncodingException, NoSuchAlgorithmException, NamingException {
        ModificationItem[] mods = {new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userPassword", hashPassword(newpass)))};
        lctx.modifyAttributes(uidDn, mods);
        LOGGER.log(Level.INFO, "Password successfully changed for {0}", uidDn);
    }

    private String hashPassword(String plain) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(plain.getBytes("UTF-8"));
        return "{SHA}" + DatatypeConverter.printBase64Binary(md.digest());
    }

    public void close() {
        if (lctx != null) {
            try {
                lctx.close();
            } catch (NamingException ex) {
                Logger.getLogger(LdapCtxClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
