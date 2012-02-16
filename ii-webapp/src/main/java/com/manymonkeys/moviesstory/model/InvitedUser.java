package com.manymonkeys.moviesstory.model;

import com.google.gson.Gson;
import com.manymonkeys.model.auth.User;

import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class InvitedUser extends User {

    private InvitedUserExtendedData invitedUserExtendedData;

    public InvitedUser(long id, String login, String password, InvitedUserExtendedData invitedUserExtendedData) {
        super(id, login, password);
        this.invitedUserExtendedData = invitedUserExtendedData;
    }

    public static class InvitedUserExtendedData {

        /* used in validation against invite-squatters-scum */
        private String invitedUserInviteKey;

        private List<String> predefinedKeywordNames;

        public InvitedUserExtendedData(String invitedUserInviteKey, List<String> predefinedKeywordNames) {
            this.invitedUserInviteKey = invitedUserInviteKey;
            this.predefinedKeywordNames = predefinedKeywordNames;
        }

        public List<String> getPredefinedKeywordNames() {
            return predefinedKeywordNames;
        }

        public void setPredefinedKeywordNames(List<String> predefinedKeywordNames) {
            this.predefinedKeywordNames = predefinedKeywordNames;
        }

        public String getInvitedUserInviteKey() {
            return invitedUserInviteKey;
        }

        public void setInvitedUserInviteKey(String invitedUserInviteKey) {
            this.invitedUserInviteKey = invitedUserInviteKey;
        }

        /*-------------------------------------\
        |  J S O N  S E R I A L I Z A T I O N  |
        \=====================================*/

        public static InvitedUserExtendedData deserialize(String value) {
            return new Gson().fromJson(value, InvitedUserExtendedData.class);
        }

        public static String serialize(InvitedUserExtendedData value) {
            return new Gson().toJson(value);
        }

        public String serialize() {
            return serialize(this);
        }
    }

    public InvitedUserExtendedData getInvitedUserExtendedData() {
        return invitedUserExtendedData;
    }

    public void setInvitedUserExtendedData(InvitedUserExtendedData invitedUserExtendedData) {
        this.invitedUserExtendedData = invitedUserExtendedData;
    }

}
