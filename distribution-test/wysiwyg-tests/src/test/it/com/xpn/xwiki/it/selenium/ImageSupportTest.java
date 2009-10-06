/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.it.selenium;

import junit.framework.Test;

import com.xpn.xwiki.it.selenium.framework.AbstractWysiwygTestCase;
import com.xpn.xwiki.it.selenium.framework.AlbatrossSkinExecutor;
import com.xpn.xwiki.it.selenium.framework.XWikiTestSuite;

/**
 * Tests the image insert and edit plugin. For the moment, it does not test the upload new image feature, since it needs
 * special selenium setup.
 * 
 * @version $Id$
 */
public class ImageSupportTest extends AbstractWysiwygTestCase
{
    public static final String MENU_IMAGE = "Image";

    public static final String MENU_INSERT_IMAGE = "Insert Image...";

    public static final String MENU_EDIT_IMAGE = "Edit Image...";

    public static final String MENU_REMOVE_IMAGE = "Remove Image";

    public static final String STEP_SELECTOR = "xSelectorStep";

    public static final String STEP_EXPLORER = "xImagesExplorer";

    public static final String STEP_CONFIG = "xImageConfig";

    public static final String STEP_CURRENT_PAGE_SELECTOR = "xImagesSelector";

    public static final String STEP_UPLOAD = "xUploadPanel";

    public static final String TAB_CURRENT_PAGE = "Current page";

    public static final String TAB_ALL_PAGES = "All pages";

    public static final String BUTTON_SELECT = "Select";

    public static final String BUTTON_INSERT_IMAGE = "Insert Image";

    public static final String INPUT_WIDTH = "//div[contains(@class, \"xSizePanel\")]//input[1]";

    public static final String INPUT_HEIGHT = "//div[contains(@class, \"xSizePanel\")]//input[2]";

    public static final String INPUT_ALT = "//div[contains(@class, \"xAltPanel\")]//input";

    /**
     * Creates the test suite for this test class.
     * 
     * @return the test suite corresponding to this class
     */
    public static Test suite()
    {
        XWikiTestSuite suite = new XWikiTestSuite("Tests insert image feature");
        suite.addTestSuite(ImageSupportTest.class, AlbatrossSkinExecutor.class);
        return suite;
    }

    /**
     * Test adding an image from a page different from the current one.
     */
    public void testInsertImageFromAllPages()
    {
        String imageSpace = "XWiki";
        String imagePage = "AdminSheet";
        String imageFile = "photos.png";
        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // switch to all pages view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage(imageSpace, imagePage, imageFile);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        assertWiki("[[image:" + imageSpace + "." + imagePage + "@" + imageFile + "]]");
    }

    /**
     * Test add and edit an image from a page different from the current one.
     */
    public void testInsertAndEditImageFromAllPages()
    {
        String imageSpace = "XWiki";
        String imagePage = "AdminSheet";
        String imageFile = "blog.png";

        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // switch to all pages view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage(imageSpace, imagePage, imageFile);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        moveCaret("XWE.body", 1);
        typeText(" blogging is cool");

        // cannot select the image otherwise but like this: click won't work, nor push button
        selectNode("XWE.body.firstChild");
        openImageDialog(MENU_EDIT_IMAGE);
        waitForStepToLoad(STEP_EXPLORER);
        // test that the page is the right page
        assertImageSelected(imageSpace, imagePage, imageFile);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        // the image alt text should be now present
        assertEquals(imageFile, getSelenium().getValue(INPUT_ALT));
        clickButtonWithText(BUTTON_INSERT_IMAGE);

        assertWiki("[[image:" + imageSpace + "." + imagePage + "@" + imageFile + "]] blogging is cool");
    }

    /**
     * Test adding and editing an image with parameters preserves parameters values.
     */
    public void testInsertAndEditImageWithParameters()
    {
        String imageSpace = "XWiki";
        String imagePage = "AdminSheet";
        String imageFile = "rights.png";

        applyStyleTitle1();
        typeText("Attention");
        typeEnter();

        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // switch to all pages view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage(imageSpace, imagePage, imageFile);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);

        getSelenium().type(INPUT_WIDTH, "200");
        getSelenium().type(INPUT_ALT, "No parking sign");
        selectAlignment("CENTER");
        clickButtonWithText(BUTTON_INSERT_IMAGE);

        // Place the caret after the inserted image.
        runScript("XWE.selection.collapseToEnd()");

        typeText("There is no parking on this wiki!");

        assertWiki("= Attention =\n\n[[image:XWiki.AdminSheet@rights.png||alt=\"No parking sign\" "
            + "style=\"margin-right: auto; margin-left: auto; display: block;\" width=\"200\"]]"
            + "There is no parking on this wiki!");

        // now edit
        selectNode("XWE.body.childNodes[1].firstChild");
        openImageDialog(MENU_EDIT_IMAGE);
        waitForStepToLoad(STEP_EXPLORER);
        // test that the page is the right page
        assertImageSelected(imageSpace, imagePage, imageFile);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        assertEquals("200", getSelenium().getValue(INPUT_WIDTH));
        assertEquals("No parking sign", getSelenium().getValue(INPUT_ALT));
        assertTrue(isAlignmentSelected("CENTER"));
        getSelenium().type(INPUT_WIDTH, "");
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();
        assertWiki("= Attention =\n\n[[image:XWiki.AdminSheet@rights.png||alt=\"No parking sign\" "
            + "style=\"margin-right: auto; margin-left: auto; display: block;\"]]There is no parking on this wiki!");
    }

    /**
     * Test that the insert image dialog defaults to the current page, and the page selection is preserved across
     * multiple inserts.
     */
    public void testDefaultSelection()
    {
        String imageSpace = "Main";
        String imagePage = "RecentChanges";
        String imageFile1 = "lquo.gif";
        String imageFile2 = "rquo.gif";

        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // test that the default loaded view is the current page view
        assertElementPresent("//div[contains(@class, \"" + STEP_CURRENT_PAGE_SELECTOR + "\")]");
        // now switch view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage(imageSpace, imagePage, imageFile1);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        moveCaret("XWE.body", 1);

        typeText("Mmmh, cheese!");

        // now second image
        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // test that the default loaded view is the current page view
        assertElementPresent("//div[contains(@class, \"" + STEP_CURRENT_PAGE_SELECTOR + "\")]");
        // now switch view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        // test that the selectors are positioned to the old page
        String imageSpaceSelector = "//div[@class=\"xPageChooser\"]//select[2]";
        waitForCondition("selenium.isElementPresent('" + imageSpaceSelector + "/option[@value=\"" + imageSpace
            + "\"]');");
        assertEquals(imageSpace, getSelenium().getSelectedValue(imageSpaceSelector));
        String imagePageSelector = "//div[@class=\"xPageChooser\"]//select[3]";
        waitForCondition("selenium.isElementPresent('" + imagePageSelector + "/option[@value=\"" + imagePage + "\"]');");
        assertEquals(imagePage, getSelenium().getSelectedValue(imagePageSelector));
        // and select the new one
        selectImage(imageSpace, imagePage, imageFile2);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        assertWiki("[[image:RecentChanges@lquo.gif]]Mmmh, cheese!" + "[[image:RecentChanges@rquo.gif]]");
    }

    /**
     * Test that hitting the previous button in the configuration dialog preserves the image selector selection.
     */
    public void testPreviousPreservesSelection()
    {
        String imageSpace = "Main";
        String imagePage = "RecentChanges";
        String imageFile1 = "rquo.gif";
        String imageFile2 = "lquo.gif";

        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage(imageSpace, imagePage, imageFile1);

        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText("Previous");

        waitForStepToLoad(STEP_EXPLORER);
        assertImageSelected(imageSpace, imagePage, imageFile1);

        selectImage(imageSpace, imagePage, imageFile2);
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        clickButtonWithText(BUTTON_INSERT_IMAGE);

        // test that the correct image has been inserted
        assertWiki("[[image:" + imagePage + "@" + imageFile2 + "]]");
    }

    /**
     * Test that an image has to be selected before advancing to configuration.
     */
    public void testValidationOnImageInsert()
    {
        // edit a page from a different page, and switch to current page to try to add. so, no image will be selected
        setWikiContent("[[image:xwiki:XWiki.AdminSheet@registration.png]]");
        selectNode("XWE.body.firstChild.firstChild");
        openImageDialog(MENU_EDIT_IMAGE);

        waitForStepToLoad(STEP_EXPLORER);
        clickTab(TAB_CURRENT_PAGE);
        waitForStepToLoad(STEP_CURRENT_PAGE_SELECTOR);

        clickButtonWithText(BUTTON_SELECT);

        assertTrue(getSelenium().isAlertPresent());
        assertEquals("No image has been selected", getSelenium().getAlert());

        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);

        clickButtonWithText(BUTTON_SELECT);
        // no error this time, can close the dialog
        closeDialog();
        waitForDialogToClose();
    }

    /**
     * Tests that an image can be removed from the menu, as well as using the delete key.
     */
    public void testRemoveImage()
    {
        setWikiContent("[[image:xwiki:Main.RecentChanges@lquo.gif]]Mmmh, cheese!"
            + "[[image:xwiki:Main.RecentChanges@rquo.gif]]");
        selectNode("XWE.body.firstChild.childNodes[2]");
        clickMenu(MENU_IMAGE);
        assertTrue(isMenuEnabled(MENU_REMOVE_IMAGE));
        clickMenu(MENU_REMOVE_IMAGE);

        assertWiki("[[image:xwiki:Main.RecentChanges@lquo.gif]]Mmmh, cheese!");

        selectNode("XWE.body.firstChild.firstChild");
        typeDelete();

        assertWiki("Mmmh, cheese!");
    }

    /**
     * Test that selecting the "Upload new image" option leads to the upload file dialog.
     */
    public void testNewImageOptionLoadsFileUploadStep()
    {
        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_CURRENT_PAGE_SELECTOR);

        // wait for the default option to load and then click it
        waitForCondition("selenium.isElementPresent('//div[contains(@class, \"xNewImagePreview\")]')");
        getSelenium().click("//div[contains(@class, \"xNewImagePreview\")]");
        clickButtonWithText(BUTTON_SELECT);

        waitForStepToLoad(STEP_UPLOAD);
        closeDialog();

        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);

        // wait for the default option to show up and then click it
        waitForCondition("selenium.isElementPresent('//div[contains(@class, \"xNewImagePreview\")]')");
        getSelenium().click("//div[contains(@class, \"xNewImagePreview\")]");
        clickButtonWithText(BUTTON_SELECT);

        waitForStepToLoad(STEP_UPLOAD);
        closeDialog();
    }

    /**
     * Test that editing an image and not changing its location preserves a full reference and does not change it to a
     * relative one.
     */
    public void testEditImagePreservesFullReferences()
    {
        setWikiContent("[[image:xwiki:Main.RecentChanges@lquo.gif]] [[image:Main.RecentChanges@rquo.gif]]");

        // edit first image
        selectNode("XWE.body.firstChild.firstChild");
        openImageDialog(MENU_EDIT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        waitForStepToLoad(STEP_EXPLORER);
        assertImageSelected("Main", "RecentChanges", "lquo.gif");

        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);

        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        // edit second image too
        selectNode("XWE.body.firstChild.childNodes[2]");
        openImageDialog(MENU_EDIT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        waitForStepToLoad(STEP_EXPLORER);
        assertImageSelected("Main", "RecentChanges", "rquo.gif");

        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);

        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        assertWiki("[[image:xwiki:Main.RecentChanges@lquo.gif]] [[image:Main.RecentChanges@rquo.gif]]");
    }

    /**
     * Test that, upon editing an image which is the label of a link, the link is preserved.
     * 
     * @see http://jira.xwiki.org/jira/browse/XWIKI-3784
     */
    public void failingTestEditImageWithLink()
    {
        // add all the image & link, otherwise it will not reproduce, it only reproduces if container is body
        openImageDialog(MENU_INSERT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        // switch to all pages view
        clickTab(TAB_ALL_PAGES);
        waitForStepToLoad(STEP_EXPLORER);
        selectImage("XWiki", "AdminSheet", "registration.png");
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);

        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        selectNode("XWE.body.firstChild");

        // add link around the image
        clickMenu("Link");
        clickMenu("Wiki Page...");
        waitForDialogToLoad();
        waitForStepToLoad(STEP_SELECTOR);
        // get the all pages tree
        clickTab("All pages");
        waitForStepToLoad("xExplorerPanel");

        getSelenium().type("//div[contains(@class, 'xExplorerPanel')]/input", "XWiki.Register");
        // wait for the space to get selected
        waitForCondition("selenium.isElementPresent('//td[contains(@class, \"cell\") and nobr=\"" + "XWiki" + "\"]');");
        waitForCondition("selenium.isElementPresent('//td[contains(@class, \"cellSelected\") and nobr=\""
            + "New page...\"]');");
        clickButtonWithText("Select");
        waitForStepToLoad("xLinkConfig");
        assertEquals("registration.png", getInputValue("Label of the created link"));
        // check that the label is readonly
        assertElementPresent("//input[@title=\"Label of the created link\" and @readonly=\"\"]");

        clickButtonWithText("Create Link");
        waitForDialogToClose();

        // edit image
        selectNode("XWE.body.firstChild.firstChild");
        openImageDialog(MENU_EDIT_IMAGE);
        waitForStepToLoad(STEP_SELECTOR);
        waitForStepToLoad(STEP_EXPLORER);
        assertImageSelected("XWiki", "AdminSheet", "registration.png");
        selectImage("Main", "RecentChanges", "lquo.gif");
        clickButtonWithText(BUTTON_SELECT);
        waitForStepToLoad(STEP_CONFIG);
        // clear the alt text
        getSelenium().type(INPUT_ALT, "");
        clickButtonWithText(BUTTON_INSERT_IMAGE);
        waitForDialogToClose();

        assertWiki("[[[[image:RecentChanges@lquo.gif]]>>XWiki.Register]]");
    }

    private void waitForStepToLoad(String stepClass)
    {
        waitForCondition("selenium.isElementPresent('//*[contains(@class, \"" + stepClass + "\")]');");
    }

    private void selectImage(String space, String page, String filename)
    {
        String imageSpaceSelector = "//div[@class=\"xPageChooser\"]//select[2]";
        waitForCondition("selenium.isElementPresent('" + imageSpaceSelector + "/option[@value=\"" + space + "\"]');");
        getSelenium().select(imageSpaceSelector, space);

        String imagePageSelector = "//div[@class=\"xPageChooser\"]//select[3]";
        waitForCondition("selenium.isElementPresent('" + imagePageSelector + "/option[@value=\"" + page + "\"]');");
        getSelenium().select(imagePageSelector, page);

        getSelenium().click("//div[@class=\"xPageChooser\"]//button[text()=\"Update\"]");

        selectImage(filename);
    }

    private void selectImage(String filename)
    {
        String imageItem = "//div[@class=\"xImagesSelector\"]//img[@title=\"" + filename + "\"]";
        waitForCondition("selenium.isElementPresent('" + imageItem + "');");
        getSelenium().click(imageItem);
    }

    private void assertImageSelected(String space, String page, String filename)
    {
        String imageSpaceSelector = "//div[@class=\"xPageChooser\"]//select[2]";
        waitForCondition("selenium.isElementPresent('" + imageSpaceSelector + "/option[@value=\"" + space + "\"]');");
        assertEquals(space, getSelenium().getSelectedValue(imageSpaceSelector));

        String imagePageSelector = "//div[@class=\"xPageChooser\"]//select[3]";
        waitForCondition("selenium.isElementPresent('" + imagePageSelector + "/option[@value=\"" + page + "\"]');");
        assertEquals(page, getSelenium().getSelectedValue(imagePageSelector));

        assertImageSelected(filename);
    }

    private void clickTab(String tabName)
    {
        String tabSelector = "//div[.='" + tabName + "']";
        getSelenium().click(tabSelector);
    }

    private void assertImageSelected(String filename)
    {
        String imageItem =
            "//div[@class=\"xImagesSelector\"]//div[contains(@class, \"xListItem-selected\")]//img[@title=\""
                + filename + "\"]";
        assertElementPresent(imageItem);
    }

    private void selectAlignment(String alignment)
    {
        getSelenium().click(
            "//div[contains(@class, \"AlignPanel\")]//input[@name=\"alignment\" and @value=\"" + alignment + "\"]");
    }

    public boolean isAlignmentSelected(String alignment)
    {
        return getSelenium().isElementPresent(
            "//div[contains(@class, \"AlignPanel\")]//input[@name=\"alignment\" and @value=\"" + alignment
                + "\" and @checked=\"\"]");
    }

    private void openImageDialog(String menuName)
    {
        clickMenu(MENU_IMAGE);
        assertTrue(isMenuEnabled(menuName));
        clickMenu(menuName);
        waitForDialogToLoad();
    }
}