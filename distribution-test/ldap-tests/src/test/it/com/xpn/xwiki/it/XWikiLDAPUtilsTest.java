package com.xpn.xwiki.it;

import java.util.Map;

import org.xwiki.component.manager.ComponentManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.cache.api.XWikiCache;
import com.xpn.xwiki.cache.api.XWikiCacheNeedsRefreshException;
import com.xpn.xwiki.cache.api.XWikiCacheService;
import com.xpn.xwiki.cache.impl.OSCacheService;
import com.xpn.xwiki.it.framework.XWikiConfig;
import com.xpn.xwiki.it.framework.XWikiLDAPTestSetup;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConnection;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils;
import com.xpn.xwiki.test.AbstractXWikiComponentTestCase;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiEngineContext;

/**
 * Tests {@link XWikiLDAPUtilsTest}.
 * 
 * @version $Id: $
 */
public class XWikiLDAPUtilsTest extends AbstractXWikiComponentTestCase
{
    /**
     * The name of the group cache.
     */
    public static final String GROUPCACHE_NAME = "groups";

    /**
     * The LDAP connection tool.
     */
    private XWikiLDAPConnection connection = new XWikiLDAPConnection();

    /**
     * The LDAP tool.
     */
    private XWikiLDAPUtils ldapUtils = new XWikiLDAPUtils(connection);

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();
        
        // Statically store the component manager in {@link Utils} to be able to access it without
        // the context.
        // @FIXME : move this initialization in AbstractXWikiComponentTestCase.setUp() when
        // shared-tests will depends on core 1.5 branch
        Utils.setComponentManager((ComponentManager) getContext().get(
            ComponentManager.class.getName()));

        new XWiki(new XWikiConfig(XWikiLDAPTestSetup.CURRENTXWIKICONF), getContext())
        {
            private XWikiCacheService cacheService;

            public void initXWiki(com.xpn.xwiki.XWikiConfig config, XWikiContext context,
                XWikiEngineContext enginecontext, boolean noupdate) throws XWikiException
            {
                context.setWiki(this);
                setConfig(config);
            }

            public XWikiCacheService getCacheService()
            {
                if (this.cacheService == null) {
                    cacheService = new OSCacheService();
                    cacheService.init(this);
                }

                return cacheService;
            }
        };

        this.ldapUtils.setUidAttributeName(XWikiLDAPTestSetup.LDAP_USERUID_FIELD);

        int port = XWikiLDAPTestSetup.getLDAPPort();

        this.connection.open("localhost", port, XWikiLDAPTestSetup.HORATIOHORNBLOWER_DN,
            XWikiLDAPTestSetup.HORATIOHORNBLOWER_PWD, null, false, getContext());
    }

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        this.connection.close();

        super.tearDown();

        // Makes sure tests are independents as Utils's ComponentManager is a static
        // @FIXME : move this initialization in AbstractXWikiComponentTestCase.setUp() when
        // shared-tests will depends on core 1.5 branch
        Utils.setComponentManager(null);
    }

    /**
     * Verify if the user uid attribute name has been correctly set.
     */
    public void testGetUidAttributeName()
    {
        assertSame("Wrong uid attribute name", XWikiLDAPTestSetup.LDAP_USERUID_FIELD,
            this.ldapUtils.getUidAttributeName());
    }

    /**
     * check that the cache is not created each time it's retrieved and correctly handle refresh
     * time.
     * 
     * @throws XWikiException error when getting the cache.
     * @throws XWikiCacheNeedsRefreshException
     * @throws InterruptedException
     */
    public void testCache() throws XWikiException, XWikiCacheNeedsRefreshException,
        InterruptedException
    {
        XWikiCache tmpCache = this.ldapUtils.getCache(GROUPCACHE_NAME, getContext());
        XWikiCache cache = this.ldapUtils.getCache(GROUPCACHE_NAME, getContext());

        assertSame("Cache is recreated", tmpCache, cache);

        cache.putInCache("key", "value");

        String value = (String) cache.getFromCache("key");

        assertEquals("Value retrieved from cache is wrong", "value", value);

        // Wait at least 1 second because the refresh time is provided in seconds in {@link
        // XWikiCache#getFromCache(String, int)}.
        Thread.sleep(1000);

        try {
            value = (String) cache.getFromCache("key", 1);
            fail("Should have thrown " + XWikiCacheNeedsRefreshException.class
                + " exception because object has been added to the cache more than 1 second ago.");
        } catch (XWikiCacheNeedsRefreshException expected) {
            // OK : means the retrieved value is "older" than 1 second.
        }
    }

    /**
     * Test {@link XWikiLDAPUtils#getGroupMembers(String, XWikiContext)}.
     * 
     * @throws XWikiException error when getting group members from cache.
     */
    public void testGetGroupMembers() throws XWikiException
    {
        Map<String, String> members =
            this.ldapUtils.getGroupMembers(XWikiLDAPTestSetup.HMSLYDIA_DN, getContext());

        assertFalse("No member was found", members.isEmpty());

        assertTrue("Wrong members was found", XWikiLDAPTestSetup.HMSLYDIA_MEMBERS.equals(members
            .keySet()));

        Map<String, String> wrongGroupMembers =
            this.ldapUtils.getGroupMembers("cn=wronggroupdn,ou=people,o=sevenSeas", getContext());

        assertNull("Should return null if group does not exists [" + wrongGroupMembers + "]",
            wrongGroupMembers);
    }

    /**
     * Test {@link XWikiLDAPUtils#isUserInGroup(String, String, XWikiContext)}.
     * 
     * @throws XWikiException error when getting group members from cache.
     */
    public void testIsUserInGroup() throws XWikiException
    {
        String userDN =
            this.ldapUtils.isUserInGroup(XWikiLDAPTestSetup.HORATIOHORNBLOWER_UID,
                XWikiLDAPTestSetup.HMSLYDIA_DN, getContext());

        assertNotNull("User " + XWikiLDAPTestSetup.HORATIOHORNBLOWER_UID + " not found", userDN);
        assertEquals(XWikiLDAPTestSetup.HORATIOHORNBLOWER_DN, userDN);

        this.ldapUtils.setUidAttributeName(XWikiLDAPTestSetup.LDAP_USERUID_FIELD_UID);

        userDN =
            this.ldapUtils.isUserInGroup(XWikiLDAPTestSetup.WILLIAMBUSH_UID,
                XWikiLDAPTestSetup.HMSLYDIA_DN, getContext());

        assertNotNull("User " + XWikiLDAPTestSetup.WILLIAMBUSH_UID + " not found", userDN);
        assertEquals(XWikiLDAPTestSetup.WILLIAMBUSH_DN, userDN);

        String wrongUserDN =
            this.ldapUtils.isUserInGroup("wronguseruid", XWikiLDAPTestSetup.HMSLYDIA_DN,
                getContext());

        assertNull("Should return null if user is not in the group", wrongUserDN);
    }
}
