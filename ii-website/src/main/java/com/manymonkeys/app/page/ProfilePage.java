package com.manymonkeys.app.page;

import com.manymonkeys.app.AuthManager;
import com.manymonkeys.app.auth.CreateUserButton;
import com.manymonkeys.app.auth.LoginButton;
import com.manymonkeys.app.auth.LogoutButton;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.component.ItemTag;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.gravatar.GravatarResource;
import org.vaadin.navigator7.Page;
import org.vaadin.navigator7.ParamPageLink;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class ProfilePage extends VerDashLayout {

    public ProfilePage() {
        super.setMargin(true);
    }

    @Override
    public void attach() {
        super.attach();
        reloadCurrentUser();
    }

    public void reloadCurrentUser() {
        InformationItem user;

        try {
            user = AuthManager.getCurrent(this).getCurrentUser();
        } catch (AuthManager.AuthException e) {
            user = null;
        }

        if (user == null) {
            this.addComponent(new Label("No user currently logged in"));
        } else {

            Panel generalInfo = new Panel("General User Info");
            this.addComponent(generalInfo);

            Label login = new Label(user.getMeta(UserService.LOGIN));
            login.addStyleName(Stream.LABEL_H1);
            generalInfo.addComponent(login);

            Link uuid = new ParamPageLink(user.getUUID().toString(), ItemPage.class, user.getUUID().toString());
            uuid.addStyleName(Stream.LABEL_H2);
            generalInfo.addComponent(uuid);

            Embedded avatar = new Embedded();
            avatar.setType(Embedded.TYPE_IMAGE);
            avatar.setWidth(100, UNITS_PIXELS);
            avatar.setHeight(100, UNITS_PIXELS);
            try {
                avatar.setSource(new GravatarResource(user.getMeta("email")));
            } catch (NoSuchAlgorithmException ignored) {
            } catch (MalformedURLException ignored) {
            }
            generalInfo.addComponent(avatar);

            Label techLabel = new Label();
            techLabel.setSizeUndefined();
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> entry : user.getMetaMap().entrySet()) {
                sb.append(String.format("%s : %s<br>", entry.getKey(), entry.getValue()));
            }
            techLabel.setValue(sb.toString());
            techLabel.setContentMode(Label.CONTENT_XHTML);
            generalInfo.addComponent(techLabel);

            Panel components = new Panel("User's components");
            this.addComponent(components);
            for (Map.Entry<InformationItem, Double> entry : user.getComponents().entrySet()) {
                components.addComponent(new ItemTag(entry.getKey(), entry.getValue(), 0));
            }

            Panel basket = new Panel("User's basket");
            this.addComponent(basket);
        }
    }

}
