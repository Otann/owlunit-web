package com.manymonkeys.app.button.common;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.component.ItemTag;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.util.Iterator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public abstract class FilterTagButton extends OpenDialogButton {

    public static final int SEARCH_LIMIT = 30;
    private TagService service;

    private Validator searchFieldValidator;
    private Label searchFieldErrorMessage;
    private ListSelect tagSelect;

    protected FilterTagButton(Window topWindow, String buttonName, String windowName, String applyTitle, TagService service) {
        super(topWindow, buttonName, windowName, applyTitle);
        this.service = service;
    }

    /**
     * Called when user selected item and pressed button
     *
     * @param item that was selected by user and needed to process
     */
    public abstract void processItem(InformationItem item);

    @Override
    public Layout getDialogContent() {
        subWindow.setWidth(300, Sizeable.UNITS_PIXELS);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        TextField searchField = new TextField();
        searchField.setWidth("100%");
        searchField.setInputPrompt("Start typing to begin search");
        searchField.addListener(new FilterChangeListener(this));
        searchFieldValidator = new RegexpValidator("[\\p{Alnum}\\p{Space}]+", "Only alphanumeric allowed");
        layout.addComponent(searchField);

        searchFieldErrorMessage = new Label("Only alphanumerics with spaces allowed");
        searchFieldErrorMessage.setStyleName("ii-label-error");
        searchFieldErrorMessage.setVisible(false);
        layout.addComponent(searchFieldErrorMessage);

        tagSelect = new ListSelect();
        tagSelect.setWidth("100%");
        tagSelect.setRows(20);
        layout.addComponent(tagSelect);

        return layout;
    }

    @Override
    public void onSubmit() {
        ItemTag selected = (ItemTag) tagSelect.getValue();
        if (selected == null) {
            topWindow.showNotification("Select tag to load", Window.Notification.TYPE_ERROR_MESSAGE);
        } else {
            processItem(selected.getItem());
        }
    }

    private static class FilterChangeListener implements FieldEvents.TextChangeListener {

        private final FilterTagButton button;

        private FilterChangeListener(FilterTagButton button) {
            this.button = button;
        }

        public void textChange(FieldEvents.TextChangeEvent event) {
            String text = event.getText() == null ? "" : event.getText().trim();
            if (text.isEmpty()) {
                button.searchFieldErrorMessage.setVisible(false);
                button.submitButton.setEnabled(false);
                button.tagSelect.removeAllItems();
                return;
            }

            if (button.searchFieldValidator.isValid(text)) {
                button.searchFieldErrorMessage.setVisible(false);
                button.submitButton.setEnabled(true);
                button.tagSelect.removeAllItems();

                int limit = SEARCH_LIMIT;
                Iterator<InformationItem> iterator = button.service.getByMeta(TagService.NAME, String.format("%s*", text.trim()));
                while (iterator.hasNext() && limit > 0) {
                    --limit;
                    button.tagSelect.addItem(new ItemTag(iterator.next(), null));
                }
            } else {
                button.searchFieldErrorMessage.setVisible(true);
            }
        }
    }
}
