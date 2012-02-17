package com.manymonkeys.web.application.page;

import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.moviesstory.service.InvitedUserService;
import com.manymonkeys.service.exception.NotFoundException;
import org.apache.click.Page;
import org.apache.click.control.Panel;
import org.apache.click.extras.panel.ListPanel;
import org.apache.click.util.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InviteLandingPage extends Page {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /* Request parametres */
    @Bindable
    protected String login;

    @Bindable
    protected String inviteKey;

    /* Required services */

    @Autowired
    InvitedUserService invitedUserService;

    public String title = "Invite Landing Page";

    /* Page controls */

    private ListPanel inviteInformationPanelList = new ListPanel("inviteInformationPanelList");

    public InviteLandingPage() {
        addControl(inviteInformationPanelList);
        inviteInformationPanelList.add(new Panel("invite-div1", "/application/custom/invite/invite-div1.htm"));
        inviteInformationPanelList.add(new Panel("invite-div2", "/application/custom/invite/invite-div2.htm"));
        inviteInformationPanelList.add(new Panel("invite-div3", "/application/custom/invite/invite-div3.htm"));
    }

    @Override
    public void onInit() {
        try {
            if (login != null) {
                InvitedUser invitedUser = invitedUserService.getInvitedUser(login);

                if (invitedUser.getInvitedUserExtendedData().getInviteKey().equals(inviteKey)) {
                    addModel("predefinedKeywords", invitedUser.getInvitedUserExtendedData().getPredefinedKeywordNames());
                } else {
                    error();
                }
            }
        } catch (NotFoundException e) {
            error();
            log.error("Failed in the onInit() logic", e);
        }
    }

    private void error() {
        addModel("error", "Do you think Marcellus Wallace looks like a bitch?");
    }

}