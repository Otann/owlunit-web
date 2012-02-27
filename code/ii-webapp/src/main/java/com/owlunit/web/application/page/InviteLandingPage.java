package com.owlunit.web.application.page;

import com.owlunit.moviesstory.model.InvitedUser;
import com.owlunit.moviesstory.model.MoviesStoryUser;
import com.owlunit.moviesstory.service.MoviesStoryService;
import com.owlunit.moviesstory.service.FacebookIntegrationService;
import com.owlunit.service.exception.NotFoundException;
import com.owlunit.moviesstory.service.MoviesStoryUserService;
import org.apache.click.Page;
import org.apache.click.control.ActionLink;
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
    MoviesStoryUserService moviesStoryUserService;

    @Autowired
    FacebookIntegrationService facebookIntegrationService;

    @Autowired
    MoviesStoryService moviestoryService;

    public String title = "Invite Landing Page";

    /* Page controls */

    private ListPanel inviteInformationPanelList = new ListPanel("inviteInformationPanelList");

    public ActionLink okLink = new ActionLink(this, "onOkClick");

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

        /* Hit —
         http://localhost:8080/ii-weapp/application/page/invite-landing-page.htm?login=hemingway&inviteKey=DUMMYINVITEKEY
         to start from the first state */
        try {
            switch (retrieveState()) {
                case STATE1_FIRST_VISIT: {
                    if (login != null) {
                        InvitedUser invitedUser = moviesStoryUserService.getInvitedUser(login);

                        if (invitedUser.getInvitedUserExtendedData().getInviteKey().equals(inviteKey)) {
                            addModel("predefinedKeywords", invitedUser.getInvitedUserExtendedData().getPredefinedKeywordNames());
                            addModel("facebookAuthenticationLink", facebookIntegrationService.constructApplicationAuthenticaionUrl());
                        } else {
                            error(null);
                        }
                    } else {
                        error(null);
                    }
                }
                break;

                case STATE2_AUTHORIZED_BY_FACEBOOK: {
                    String token = facebookIntegrationService.retrieveAccessToken((String) getContext().getSessionAttribute("code"));

                    getContext().setSessionAttribute("token", token);
                    addModel("token", token);

                    addModel("yourMoviesImportedFromFacebook", moviestoryService.importUserFacebookMovies(token));

                    /* Set this user in the session, it is a good practice to use class names, as most of the time, there can be
                      only one valid instance of the certain class */
                    getContext().setSessionAttribute(MoviesStoryUser.class.getCanonicalName(), moviestoryService.importUserFacebookData(token));
                }
                break;

                case STATE3_FINISH_STATE: {
                    /* We will encounter ourselves in this state,
                       if user will try to open invite link from the letter again */
                    onOkClick();
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

    public boolean onOkClick() {
        setRedirect(ProfilePage.class);
        return false;
    }

    private void error(Exception e) {
        log.error("Failed in the onInit() logic", e);
        addModel("error", "Do you think Marcellus Wallace looks like a bitch?");
    }

}