package com.manymonkeys.moviesstory.service;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.impl.UserServiceImpl;

import java.util.Collection;

import static com.manymonkeys.service.impl.util.Utils.itemWithMeta;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class InvitedUserServiceImpl extends UserServiceImpl implements InvitedUserService {

    protected static InvitedUser iiToInvitedUser(IiDao dao, Ii item) {
        Ii meta = itemWithMeta(dao, item);
        return new InvitedUser(
                item.getId(),
                meta.getMeta(META_KEY_LOGIN),
                meta.getMeta(META_KEY_PASSWORD),
                InvitedUser.InvitedUserExtendedData.deserialize(
                        meta.getMeta(InvitedUser.InvitedUserExtendedData.class.getCanonicalName())
                )
        );
    }

    @Override
    public InvitedUser createInvitedUser(InvitedUser invitedUser) {
        Ii userIi = createUserIi(invitedUser);
        dao.setMetaUnindexed(userIi,
                InvitedUser.InvitedUserExtendedData.class.getCanonicalName(),
                invitedUser.getInvitedUserExtendedData().serialize());
        return iiToInvitedUser(dao, userIi);
    }

    @Override
    public Boolean isInvitedUser(User user) throws NotFoundException {
        String predefinedKeywordNamesMeta = userToIi(dao, user).getMeta(InvitedUser.InvitedUserExtendedData.class.getCanonicalName());
        return predefinedKeywordNamesMeta == null
                || predefinedKeywordNamesMeta.isEmpty();
    }

    @Override
    public InvitedUser getInvitedUser(String login) throws NotFoundException {
        Collection<Ii> items = getDao().load(META_KEY_LOGIN, login);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("InvitedUser(%s)", login));
        } else {
            return iiToInvitedUser(dao, items.iterator().next());
        }
    }

}
