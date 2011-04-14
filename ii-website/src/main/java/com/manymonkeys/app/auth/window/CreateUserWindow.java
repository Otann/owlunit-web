package com.manymonkeys.app.auth.window;

import com.manymonkeys.app.AuthManager;
import com.manymonkeys.app.MainApplication;
import com.manymonkeys.app.auth.PasswordValidator;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
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
public class CreateUserWindow extends Window {

    @Autowired
    UserService userService;

    UserCreatedListener listener;

    AuthManager authManager;

    public CreateUserWindow(final UserCreatedListener listener) {
        super("Create User");
        setWidth(200, UNITS_PIXELS);
        setResizable(false);
        this.listener = listener;

        VerDashLayout layout = new VerDashLayout();
        this.setContent(layout);
        layout.setMargin(true);
        layout.setSizeFull();

        // Init fields

        TextField login = new TextField();
        login.setInputPrompt("username");
        login.setRequired(true);

        final PasswordField password = new PasswordField();
        password.setInputPrompt("password");
        password.setRequired(true);
        password.addValidator(PasswordValidator.getValidator());
        password.setValidationVisible(true);

        TextField email = new TextField();
        email.setInputPrompt("your@email.com");
        email.setDescription("This will be used to generate your avatar");
        email.addValidator(new EmailValidator("Enter a valid email"));
        email.setValidationVisible(true);

        // Init form

        final Form form = new Form();
        form.setSizeFull();
        form.setImmediate(true);
        form.setStyleName(Stream.PANEL_LIGHT);

        // Add everything to form

        form.addField("login", login);
        form.addField("password", password);
        form.addField("email", email);

        layout.addComponent(form);
        layout.setExpandRatio(form, 1);
        this.setModal(true);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidth("100%");
        Button commit = new Button("Create", form, "commit");
        footer.addComponent(commit);
        footer.setComponentAlignment(commit, Alignment.MIDDLE_CENTER);
        form.setFooter(footer);

        commit.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if (form.isValid()) {
                    String login = (String) form.getField("login").getValue();
                    String password = (String) form.getField("password").getValue();
                    String email = (String) form.getField("email").getValue();

                    try {
                        InformationItem user = authManager.createUser(login, password);
                        userService.setMeta(user, "email", email);

                        if (listener != null) {
                            listener.userCreated(user);
                        }
                    } catch (AuthManager.AuthException e) {
                        MainApplication.getCurrentNavigableAppLevelWindow().showNotification(e.getMessage());
                        return;
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

    public static interface UserCreatedListener {
        public void userCreated(InformationItem user);
    }

}
