package com.manymonkeys.web.application.control;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Ilya Pimenov
 *         Xaton Proprietary
 */
public class OwlSearchPanelTests {

    @Test
    public void testDefaultControllerOutput() {
        OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

        String output = owlSearchPanel.toString();

        System.out.println(output);

        assertTrue(output.contains("div"));
    }

    @Test
    public void testToBeXmlComplientOutput() {
        OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

        String output = owlSearchPanel.toString();

        // Todo To be implemented (and this is important as hell)
    }

    @Test
    public void testSaveRestoreControllerState() {
        /* to be implemented with MockContext */
    }
}
