package com.manymonkeys.web.application.page;

import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.moviesstory.service.FacebookIntegrationService;
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

import java.io.UnsupportedEncodingException;

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

    @Autowired
    FacebookIntegrationService facebookIntegrationService;

    public String title = "Invite Landing Page";

    /* Page controls */

    private ListPanel inviteInformationPanelList = new ListPanel("inviteInformationPanelList");

    /**
     * With this enum we would control interaction between states for current page
     */
    private enum InvitePageState {
        STATE1_FIRST_VISIT, // when user just clicked invite link from the letter
        STATE2_AUTHORIZED_BY_FACEBOOK, // after redirect from facebook
        STATE3_FINISH_STATE // when showing information grabbed from the facebook, before redirect to the profile
    }

    public InviteLandingPage() {
        addControl(inviteInformationPanelList);
        inviteInformationPanelList.add(new Panel("invite-div1", "/application/custom/invite/invite-div1.htm"));
        inviteInformationPanelList.add(new Panel("invite-div2", "/application/custom/invite/invite-div2.htm"));
        inviteInformationPanelList.add(new Panel("invite-div3", "/application/custom/invite/invite-div3.htm"));
    }

    private InvitePageState retrieveState() {
        if (getContext().hasSessionAttribute("token")) {
            return InvitePageState.STATE3_FINISH_STATE;
        } else if (getContext().hasRequestParameter("code")) {
            return InvitePageState.STATE2_AUTHORIZED_BY_FACEBOOK;
        } else {
            return InvitePageState.STATE1_FIRST_VISIT;
        }
    }

    @Override
    public void onInit() {
        try {
            switch (retrieveState()) {
                case STATE1_FIRST_VISIT: {
                    if (login != null) {
                        InvitedUser invitedUser = invitedUserService.getInvitedUser(login);

                        if (invitedUser.getInvitedUserExtendedData().getInviteKey().equals(inviteKey)) {
                            addModel("predefinedKeywords", invitedUser.getInvitedUserExtendedData().getPredefinedKeywordNames());
                            addModel("facebookAuthenticationLink", facebookIntegrationService.constructApplicationAuthenticaionUrl());
                        } else {
                            error(null);
                        }
                    }
                }
                break;

                case STATE2_AUTHORIZED_BY_FACEBOOK: {
                    String token = facebookIntegrationService.retrieveAccessToken((String) getContext().getSessionAttribute("code"));

                    getContext().setSessionAttribute("token", token);
                    addModel("token", token);
                }
                break;

                case STATE3_FINISH_STATE: {

                }
                break;
            }
        } catch (NotFoundException e) {
            error(e);
        } catch (UnsupportedEncodingException e) {
            error(e);
        } catch (Exception e) {
            error(e);
        }
    }

    private void error(Exception e) {
        log.error("Failed in the onInit() logic", e);
        addModel("error", "Do you think Marcellus Wallace looks like a bitch?");
    }

}