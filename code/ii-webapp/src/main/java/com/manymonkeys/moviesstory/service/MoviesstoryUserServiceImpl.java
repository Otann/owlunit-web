package com.manymonkeys.moviesstory.service;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.moviesstory.model.MoviesStoryUser;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.impl.UserServiceImpl;

import java.util.Collection;

import static com.manymonkeys.service.impl.util.Utils.itemWithMeta;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MoviesstoryUserServiceImpl extends UserServiceImpl implements MoviesStoryUserService {

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

    protected static MoviesStoryUser iiToMoviesStoryUser(IiDao dao, Ii item) {
        Ii meta = itemWithMeta(dao, item);
        return new MoviesStoryUser(
                item.getId(),
                meta.getMeta(META_KEY_LOGIN),
                meta.getMeta(META_KEY_PASSWORD),
                MoviesStoryUser.FacebookUserExtendedData.deserialize(
                        meta.getMeta(MoviesStoryUser.FacebookUserExtendedData.class.getCanonicalName())
                ),
                MoviesStoryUser.MoviesStoryUserExtendedData.deserialize(
                        meta.getMeta(MoviesStoryUser.MoviesStoryUserExtendedData.class.getCanonicalName())
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

    @Override
    public MoviesStoryUser createMoviesStoryUser(MoviesStoryUser moviesStoryUser) {
        Ii userIi = createUserIi(moviesStoryUser);
        dao.setMetaUnindexed(userIi,
                MoviesStoryUser.FacebookUserExtendedData.class.getCanonicalName(),
                moviesStoryUser.getFacebookUserExtendedData().serialize());
        dao.setMetaUnindexed(userIi,
                MoviesStoryUser.MoviesStoryUserExtendedData.class.getCanonicalName(),
                moviesStoryUser.getMoviesStoryUserExtendedData().serialize());
        return iiToMoviesStoryUser(dao, userIi);
    }

    @Override
    public Boolean isMoviesStoryUser(User user) throws NotFoundException {
        String moviesstoryExtendedData = userToIi(dao, user).getMeta(MoviesStoryUser.MoviesStoryUserExtendedData.class.getCanonicalName());
        return moviesstoryExtendedData == null
                || moviesstoryExtendedData.isEmpty();
    }

    @Override
    public MoviesStoryUser getMoviesStoryUser(String login) throws NotFoundException {
        Collection<Ii> items = getDao().load(META_KEY_LOGIN, login);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("InvitedUser(%s)", login));
        } else {
            return iiToMoviesStoryUser(dao, items.iterator().next());
        }
    }

}