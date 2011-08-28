package com.manymonkeys.app.auth.window;

import com.manymonkeys.app.auth.AuthManager;
import com.manymonkeys.app.MainApplication;
import com.manymonkeys.app.auth.PasswordValidator;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class LoginWindow extends Window {

    @Autowired
    UserService userService;

    UserLoggedInListener listener;

    AuthManager authManager;

    public LoginWindow(final UserLoggedInListener listener) {
        super("Welcome");
        setWidth(200, UNITS_PIXELS);
        setResizable(false);
        this.listener = listener;

        // Init fields

        TextField login = new TextField();
        login.setInputPrompt("username");
        login.setRequired(true);

        final PasswordField password = new PasswordField();
        password.setInputPrompt("password");
        password.setRequired(true);
        password.addValidator(PasswordValidator.getValidator());
        password.setValidationVisible(true);

        // Init form

        final Form form = new Form();
        form.setSizeFull();
        form.setImmediate(true);
        form.setStyleName(Stream.PANEL_LIGHT);

        // Add everything to form

        form.addField("login", login);
        form.addField("password", password);

        this.addComponent(form);
        this.setModal(true);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidth("100%");
        Button commit = new Button("Sign In", form, "commit");
        footer.addComponent(commit);
        footer.setComponentAlignment(commit, Alignment.MIDDLE_CENTER);
        form.setFooter(footer);

        commit.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (form.isValid()) {
                    String login = (String) form.getField("login").getValue();
                    String password = (String) form.getField("password").getValue();

                    try {
                        Ii user = authManager.authenticate(login, password);
                        if (user == null)
                            getApplication().getMainWindow().showNotification("Authentication failed, User is null");

                        if (listener != null) {
                            listener.userLoggedIn(user);
                        }
                    } catch (AuthManager.AuthException e) {
                        MainApplication.getCurrentNavigableAppLevelWindow().showNotification(e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void attach() {
        super.attach();
        authManager = AuthManager.getCurrent(this);
    }

    public static interface UserLoggedInListener {
        public void userLoggedIn(Ii user);
    }

}
