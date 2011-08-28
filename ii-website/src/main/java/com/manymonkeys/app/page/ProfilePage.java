package com.manymonkeys.app.page;

import com.manymonkeys.app.auth.AuthManager;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.gravatar.GravatarResource;
import org.vaadin.navigator7.Page;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class ProfilePage extends ItemPage {

    @Override
    public void attach() {
        try {
            uuid = AuthManager.getCurrent(this).getCurrentUser().getUUID().toString();
        } catch (AuthManager.AuthException e) {
            uuid = "";
        }

        super.attach();
    }

    @Override
    public void refillMeta() {
        meta.removeAllComponents();

        Embedded avatar = new Embedded();
        avatar.setType(Embedded.TYPE_IMAGE);
        avatar.addStyleName(Stream.ITEM_AVATAR);
        avatar.setWidth(100, UNITS_PIXELS);
        avatar.setHeight(100, UNITS_PIXELS);
        try {
            avatar.setSource(new GravatarResource(item.getMeta("email")));
        } catch (NoSuchAlgorithmException ignored) {
        } catch (MalformedURLException ignored) {
        }
        meta.addComponent(avatar);

        Label name = new Label(item.getMeta(UserService.LOGIN));
        name.setWidth(null);
        name.addStyleName(Stream.ITEM_PAGE_NAME);
        name.addStyleName(Stream.ITEM_USER_NAME);
        meta.addComponent(name);

//        meta.addComponent(new Label(item.getMeta("email")));

    }

}
