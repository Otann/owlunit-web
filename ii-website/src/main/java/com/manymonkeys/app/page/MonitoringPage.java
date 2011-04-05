package com.manymonkeys.app.page;


import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Table;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class MonitoringPage extends VerDashLayout {

//    @Autowired
//    IndexService indexService

//    @Autowired
//    GraphDatabaseService graphDb;

    @Override
    public void attach() {
        super.attach();

        setMargin(true);

        Table table = new Table();
        addComponent(table);
        table.setSelectable(false);
        table.setWidth("100%");
        table.addContainerProperty("Parameter", String.class, "");
        table.addContainerProperty("Value", Object.class, null);
        table.setColumnExpandRatio(2, 1);

        Object[] item = {"Nodes in Graph Database", new Long(239)};
        table.addItem(item, 1);
    }
}
