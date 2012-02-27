package com.owlunit.moviesstory.environment;

import com.owlunit.moviesstory.model.InvitedUser;
import com.owlunit.moviesstory.service.MoviesstoryUserServiceImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/beans/owl-beans.xml", "/beans/owl-neo4j.xml"})
public class EnvironmentTests {

    @Autowired
    MoviesstoryUserServiceImpl invitedUserService;

    @Test
    public void inviteHemingwayToOwlMoviesService() {
        InvitedUser hemingway = new InvitedUser(
                666,
                "hemingway",
                "4th-infantry",
                new InvitedUser.InvitedUserExtendedData(
                        "DUMMYINVITEKEY",
                        Arrays.asList("Mark Twain", "Hunting", "Cuba", "Africa", "Corrida")
                )
        );

        invitedUserService.createInvitedUser(hemingway);
    }
}
